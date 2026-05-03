#!/usr/bin/env python3
"""
AllergyTrack — Open Food Facts → Supabase Import
=================================================
Stáhne denní delta export z Open Food Facts a uloží filtrovaná data do Supabase.

Použití:
    python import_off.py                    # denní delta, výchozí filtr zemí (CORE)
    python import_off.py --full             # plný import, výchozí filtr zemí (CORE)
    python import_off.py --full --region=eu # plný import, všechny EU země
    python import_off.py --full --region=all # plný import, celý svět (velká DB!)

Regiony (--region):
    core  — výchozí: CZ, SK, DE, AT, PL, HU (nejmenší DB, doporučeno pro free tier)
    eu    — všechny EU+EEA země (střední DB, ~2x větší než core)
    all   — celý svět (největší DB, nutný Supabase Pro nebo jiný hosting)

Proměnné prostředí (GitHub Actions Secrets):
    SUPABASE_URL          — URL tvého Supabase projektu
    SUPABASE_SERVICE_KEY  — service_role klíč (ne anon!)
"""

import argparse
import gzip
import json
import logging
import os
import sys
import time
from datetime import datetime, timezone
from typing import Optional

import requests

# ── Konfigurace ────────────────────────────────────────────────────────────────

SUPABASE_URL = os.environ["SUPABASE_URL"]
SUPABASE_KEY = os.environ["SUPABASE_SERVICE_KEY"]

# Open Food Facts vyžaduje identifikaci aplikace v User-Agent hlavičce.
# Bez ní server vrátí 403 Forbidden.
# POZOR: HTTP hlavičky musí být čisté ASCII — žádná diakritika!
# Formát: AppName/verze (odkaz-na-projekt)
OFF_USER_AGENT = "AllergyTrack/1.0 (github.com/RuzickaOnd/AllergyTrack)"

# Open Food Facts URL — plný JSONL export (komprimovaný gzip, ~7 GB)
OFF_FULL_EXPORT_URL = (
    "https://static.openfoodfacts.org/data/openfoodfacts-products.jsonl.gz"
)

# Delta soubory: produkty změněné za posledních ~24 hodin
OFF_DELTA_BASE_URL = "https://static.openfoodfacts.org/data/delta/"

# ── Filtry zemí ────────────────────────────────────────────────────────────────
#
# CORE — střední Evropa (výchozí, nejmenší DB — doporučeno pro Supabase free tier)
# Přibližná velikost: 80–150 MB
COUNTRIES_CORE = {
    "en:czech-republic",
    "en:slovakia",
    "en:germany",
    "en:austria",
    "en:poland",
    "en:hungary",
}

# EU — celá Evropská unie + Švýcarsko a Velká Británie
# Přibližná velikost: 250–400 MB (stále v limitu Supabase free 500 MB)
COUNTRIES_EU = COUNTRIES_CORE | {
    "en:france", "en:italy", "en:spain",
    "en:netherlands", "en:belgium", "en:sweden", "en:denmark",
    "en:finland", "en:portugal", "en:romania", "en:greece",
    "en:switzerland", "en:united-kingdom", "en:ireland",
    "en:croatia", "en:slovenia", "en:bulgaria", "en:estonia",
    "en:latvia", "en:lithuania", "en:luxembourg", "en:malta",
    "en:cyprus", "en:norway", "en:iceland",
}

# ALL — celý svět (nutný Supabase Pro nebo jiný hosting s >500 MB)
# Přibližná velikost: 500 MB+ (překročí free tier)
COUNTRIES_ALL = None  # None = žádný filtr, importuje vše

# Mapování název regionu → sada zemí (pro --region argument)
REGION_MAP: dict[str, set | None] = {
    "core": COUNTRIES_CORE,
    "eu":   COUNTRIES_EU,
    "all":  COUNTRIES_ALL,
}

# Výchozí region — změň zde pokud chceš jiný default bez parametru
DEFAULT_REGION = "core"

# Aktivní filtr — nastavuje se při startu z --region argumentu
# Neměň přímo, používej argument příkazové řádky nebo workflow_dispatch input
ACTIVE_COUNTRY_FILTER: set | None = COUNTRIES_CORE

# Pole která importujeme z OFF JSONL exportu.
# Přidání nového pole = přidat sem + do extract_fields() + ALTER TABLE v Supabase.
FIELDS_TO_IMPORT = [
    "code",
    "product_name",
    "product_name_cs",
    "allergens_tags",
    "ingredients_text",
    "ingredients_text_cs",
    "brands",
    "categories_tags",
    "countries_tags",
    "nutriscore_grade",
    "nova_group",
    "image_url",
    "last_modified_t",
]

# Kolik produktů upsertovat najednou (Supabase max ~1000, bezpečný limit 500)
BATCH_SIZE = 500

# ── Logging ────────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)

# ── HTTP sessions ──────────────────────────────────────────────────────────────

# Session pro Open Food Facts — s povinným User-Agentem
# HTTP hlavičky musí být čisté ASCII — encode/decode odstraní případnou diakritiku
def _to_ascii(s: str) -> str:
    return s.encode("ascii", errors="ignore").decode("ascii")

off_session = requests.Session()
off_session.headers.update({
    "User-Agent": _to_ascii(OFF_USER_AGENT),
    # Doporučuje se také uvést e-mail pro případný kontakt ze strany OFF
    "From": "allergytrack-import@example.com",
})

# Session pro Supabase — s autentizačními hlavičkami
supabase_session = requests.Session()
supabase_session.headers.update({
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Content-Type": "application/json",
})

# ── Supabase operace ───────────────────────────────────────────────────────────

def supabase_upsert(records: list[dict]) -> tuple[int, int]:
    """Vloží nebo aktualizuje záznamy. Vrací (upserted, errors)."""
    if not records:
        return 0, 0

    response = supabase_session.post(
        f"{SUPABASE_URL}/rest/v1/food_products",
        headers={"Prefer": "return=minimal,resolution=merge-duplicates"},
        json=records,
        timeout=60,
    )

    if response.status_code in (200, 201):
        return len(records), 0

    log.error(f"Supabase upsert error {response.status_code}: {response.text[:300]}")
    return 0, len(records)


def log_import_start(import_type: str) -> Optional[int]:
    """Zapíše zahájení importu. Vrací ID řádku pro pozdější aktualizaci."""
    response = supabase_session.post(
        f"{SUPABASE_URL}/rest/v1/import_log",
        headers={"Prefer": "return=representation"},
        json={
            "import_type": import_type,
            "started_at": datetime.now(timezone.utc).isoformat(),
            "status": "running",
        },
        timeout=30,
    )
    if response.status_code in (200, 201):
        data = response.json()
        return data[0]["id"] if data else None
    return None


def log_import_finish(log_id: Optional[int], stats: dict):
    """Finalizuje záznam v import_log."""
    if log_id is None:
        return
    supabase_session.patch(
        f"{SUPABASE_URL}/rest/v1/import_log?id=eq.{log_id}",
        json={"finished_at": datetime.now(timezone.utc).isoformat(), **stats},
        timeout=30,
    )

# ── Zpracování produktů ────────────────────────────────────────────────────────

def matches_country_filter(product: dict) -> bool:
    """
    True pokud produkt prochází aktivním filtrem zemí.
    ACTIVE_COUNTRY_FILTER = None znamená žádný filtr (region=all).
    """
    if ACTIVE_COUNTRY_FILTER is None:
        return True  # region=all — importuj vše
    return bool(set(product.get("countries_tags", [])) & ACTIVE_COUNTRY_FILTER)


def extract_fields(product: dict) -> Optional[dict]:
    """
    Extrahuje potřebná pole. Vrátí None pro produkty bez názvu nebo kódu.
    Pro přidání nového pole: přidej sem řádek a do FIELDS_TO_IMPORT.
    """
    code = (product.get("code") or "").strip()
    if not code:
        return None

    product_name    = product.get("product_name")    or None
    product_name_cs = product.get("product_name_cs") or None

    # Přeskoč produkty bez jakéhokoliv názvu
    if not product_name and not product_name_cs:
        return None

    return {
        "code":                code,
        "product_name":        product_name,
        "product_name_cs":     product_name_cs,
        "allergens_tags":      product.get("allergens_tags")  or [],
        "ingredients_text":    product.get("ingredients_text") or None,
        "ingredients_text_cs": product.get("ingredients_text_cs") or None,
        "brands":              product.get("brands")          or None,
        "categories_tags":     product.get("categories_tags") or [],
        "countries_tags":      product.get("countries_tags")  or [],
        "nutriscore_grade":    product.get("nutriscore_grade") or None,
        "nova_group":          product.get("nova_group")      or None,
        "image_url":           product.get("image_url")       or None,
        "last_modified_t":     product.get("last_modified_t") or None,
        "updated_at":          datetime.now(timezone.utc).isoformat(),
    }

# ── Stream zpracování ──────────────────────────────────────────────────────────

def process_stream(url: str, label: str) -> tuple[int, int, int]:
    """
    Streamově stáhne a zpracuje JSONL.gz soubor.
    Nikdy nestahuje celý soubor do RAM — vhodné pro 7GB plný export.
    Vrací (processed, upserted, skipped).
    """
    log.info(f"Stahuji: {label}")
    log.info(f"URL: {url}")

    processed = upserted = skipped = 0
    batch: list[dict] = []
    start = time.time()

    # Použij off_session — obsahuje správný User-Agent
    with off_session.get(url, stream=True, timeout=300) as response:
        response.raise_for_status()

        # Streamový gzip decoder
        with gzip.open(response.raw, mode="rt", encoding="utf-8", errors="ignore") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue

                try:
                    product = json.loads(line)
                except json.JSONDecodeError:
                    skipped += 1
                    continue

                processed += 1

                if not matches_country_filter(product):
                    skipped += 1
                    continue

                # Filtr 2: extrakce polí a validace
                record = extract_fields(product)
                if record is None:
                    skipped += 1
                    continue

                batch.append(record)

                # Batch upsert
                if len(batch) >= BATCH_SIZE:
                    u, _ = supabase_upsert(batch)
                    upserted += u
                    batch = []

                    # Progress log každých 10 000 produktů
                    if processed % 10_000 == 0:
                        rate = processed / (time.time() - start)
                        log.info(
                            f"  {processed:>8,} zpracováno | "
                            f"{upserted:>7,} upsertováno | "
                            f"{rate:>5.0f} prod/s"
                        )

        # Zbývající batch
        if batch:
            u, _ = supabase_upsert(batch)
            upserted += u

    elapsed = time.time() - start
    log.info(
        f"Hotovo: {processed:,} zpracováno, "
        f"{upserted:,} upsertováno, "
        f"{skipped:,} přeskočeno, "
        f"čas: {elapsed:.0f}s"
    )
    return processed, upserted, skipped

# ── Delta soubory ──────────────────────────────────────────────────────────────

def get_delta_urls() -> list[str]:
    """Vrátí URL delta souborů z posledních 24 hodin."""
    response = off_session.get(OFF_DELTA_BASE_URL, timeout=30)
    response.raise_for_status()

    cutoff = int(time.time()) - 86400
    urls = []

    for line in response.text.split("\n"):
        if "products_" not in line or ".json.gz" not in line:
            continue

        start = line.find("products_")
        end   = line.find(".json.gz", start) + len(".json.gz")
        if start == -1 or end == -1:
            continue

        filename = line[start:end]
        parts = filename.replace("products_", "").replace(".json.gz", "").split("_")
        if len(parts) != 2:
            continue

        try:
            if int(parts[1]) >= cutoff:
                urls.append(OFF_DELTA_BASE_URL + filename)
        except ValueError:
            continue

    log.info(f"Nalezeno {len(urls)} delta souborů za posledních 24 h")
    return urls

# ── Hlavní funkce ──────────────────────────────────────────────────────────────

def run_full_import():
    """Plný import — spouštěj jednou při inicializaci (60–90 min)."""
    log.info("=== PLNÝ IMPORT — může trvat 60–90 minut ===")
    log_id = log_import_start("full")
    try:
        p, u, s = process_stream(OFF_FULL_EXPORT_URL, "OFF plný export")
        log_import_finish(log_id, {
            "products_processed": p,
            "products_upserted": u,
            "products_skipped": s,
            "status": "success",
        })
    except Exception as e:
        log.error(f"Plný import selhal: {e}")
        log_import_finish(log_id, {"status": "failed", "error_message": str(e)})
        raise


def run_delta_import():
    """Denní delta import — spouštěj každý den přes cron (2–10 min)."""
    log.info("=== DELTA IMPORT ===")
    log_id = log_import_start("delta")
    total_p = total_u = total_s = 0

    try:
        urls = get_delta_urls()

        if not urls:
            log.warning("Žádné delta soubory — možná ještě nejsou k dispozici, zkus znovu za hodinu")
            log_import_finish(log_id, {
                "status": "success",
                "products_processed": 0,
                "products_upserted": 0,
                "products_skipped": 0,
            })
            return

        for url in urls:
            p, u, s = process_stream(url, url.split("/")[-1])
            total_p += p
            total_u += u
            total_s += s

        log_import_finish(log_id, {
            "products_processed": total_p,
            "products_upserted": total_u,
            "products_skipped": total_s,
            "status": "success",
        })

    except Exception as e:
        log.error(f"Delta import selhal: {e}")
        log_import_finish(log_id, {"status": "failed", "error_message": str(e)})
        raise

# ── Entry point ────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="AllergyTrack — OFF import",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Příklady:
  python import_off.py                       # delta, region=core
  python import_off.py --full                # plný import, region=core
  python import_off.py --full --region=eu    # plný import, všechny EU země
  python import_off.py --full --region=all   # plný import, celý svět
        """
    )
    parser.add_argument(
        "--full",
        action="store_true",
        help="Plný import celé databáze (pouze při první inicializaci)",
    )
    parser.add_argument(
        "--region",
        choices=["core", "eu", "all"],
        default=DEFAULT_REGION,
        help=(
            "Filtr zemí: core=střední Evropa (výchozí, free tier), "
            "eu=celá EU, all=celý svět (nutný Pro hosting)"
        ),
    )
    args = parser.parse_args()

    # Zkontroluj env proměnné
    if not SUPABASE_URL or not SUPABASE_KEY:
        log.error("Chybí SUPABASE_URL nebo SUPABASE_SERVICE_KEY")
        sys.exit(1)

    # Nastav aktivní filtr podle zvoleného regionu
    import importlib, builtins
    ACTIVE_COUNTRY_FILTER = REGION_MAP[args.region]
    # Přepíše globální proměnnou pro celý běh skriptu
    import sys as _sys
    _this = _sys.modules[__name__]
    _this.ACTIVE_COUNTRY_FILTER = ACTIVE_COUNTRY_FILTER

    region_label = args.region.upper()
    country_count = len(ACTIVE_COUNTRY_FILTER) if ACTIVE_COUNTRY_FILTER else "vše"
    log.info(f"Region: {region_label} ({country_count} zemí)")

    if args.full:
        run_full_import()
    else:
        run_delta_import()