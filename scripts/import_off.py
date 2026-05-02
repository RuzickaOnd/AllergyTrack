#!/usr/bin/env python3
"""
AllergyTrack — Open Food Facts → Supabase Import
=================================================
Stáhne denní delta export z Open Food Facts a uloží filtrovaná data do Supabase.

Při prvním spuštění použij --full pro import celé databáze (filtrované na EU).
Každý další den spouštěj bez argumentů pro delta update.

Použití:
    python import_off.py           # denní delta
    python import_off.py --full    # první inicializační import

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

# Open Food Facts URL — JSONL export (komprimovaný gzip)
OFF_FULL_EXPORT_URL = "https://static.openfoodfacts.org/data/openfoodfacts-products.jsonl.gz"

# Delta soubory: https://static.openfoodfacts.org/data/delta/
# Formát názvu: products_{unix_od}_{unix_do}.json.gz
OFF_DELTA_BASE_URL = "https://static.openfoodfacts.org/data/delta/"

# Filtr zemí — pouze EU produkty (snižuje databázi ~60 %)
# Přidej/odeber podle potřeby
EU_COUNTRY_TAGS = {
    "en:czech-republic", "en:slovakia", "en:germany", "en:austria",
    "en:france", "en:italy", "en:spain", "en:poland", "en:hungary",
    "en:netherlands", "en:belgium", "en:sweden", "en:denmark",
    "en:finland", "en:portugal", "en:romania", "en:greece",
    "en:switzerland", "en:united-kingdom",
}

# Pole která importujeme — přidej sem jakékoliv nové pole
# Klíče jsou názvy polí v Open Food Facts JSONL exportu
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

# Kolik produktů upsertovat najednou (Supabase limit je 1000)
BATCH_SIZE = 500

# ── Logging ────────────────────────────────────────────────────────────────────

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
log = logging.getLogger(__name__)

# ── Supabase client (bez SDK — čisté requests) ─────────────────────────────────

HEADERS = {
    "apikey": SUPABASE_KEY,
    "Authorization": f"Bearer {SUPABASE_KEY}",
    "Content-Type": "application/json",
    "Prefer": "resolution=merge-duplicates",  # upsert chování
}


def supabase_upsert(records: list[dict]) -> tuple[int, int]:
    """
    Vloží nebo aktualizuje záznamy v Supabase.
    Vrací (upserted_count, error_count).
    """
    if not records:
        return 0, 0

    url = f"{SUPABASE_URL}/rest/v1/food_products"
    response = requests.post(
        url,
        headers={**HEADERS, "Prefer": "return=minimal,resolution=merge-duplicates"},
        json=records,
        timeout=60,
    )

    if response.status_code in (200, 201):
        return len(records), 0
    else:
        log.error(f"Supabase upsert failed: {response.status_code} — {response.text[:200]}")
        return 0, len(records)


def log_import_start(import_type: str) -> Optional[int]:
    """Zapíše zahájení importu do import_log tabulky. Vrací ID záznamu."""
    url = f"{SUPABASE_URL}/rest/v1/import_log"
    response = requests.post(
        url,
        headers={**HEADERS, "Prefer": "return=representation"},
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
    """Aktualizuje záznam v import_log po dokončení."""
    if log_id is None:
        return
    url = f"{SUPABASE_URL}/rest/v1/import_log?id=eq.{log_id}"
    requests.patch(
        url,
        headers=HEADERS,
        json={
            "finished_at": datetime.now(timezone.utc).isoformat(),
            **stats,
        },
        timeout=30,
    )


# ── Zpracování produktu ────────────────────────────────────────────────────────

def is_eu_product(product: dict) -> bool:
    """Vrací True pokud produkt pochází alespoň z jedné EU země."""
    countries = set(product.get("countries_tags", []))
    return bool(countries & EU_COUNTRY_TAGS)


def extract_fields(product: dict) -> Optional[dict]:
    """
    Extrahuje pouze potřebná pole z produktového záznamu.
    Vrací None pokud produkt nemá ani jeden z názvů.
    """
    code = product.get("code", "").strip()
    if not code:
        return None

    product_name = product.get("product_name", "") or None
    product_name_cs = product.get("product_name_cs", "") or None

    # Přeskoč produkty bez jakéhokoliv názvu
    if not product_name and not product_name_cs:
        return None

    return {
        "code": code,
        "product_name": product_name,
        "product_name_cs": product_name_cs,
        "allergens_tags": product.get("allergens_tags", []) or [],
        "ingredients_text": product.get("ingredients_text", "") or None,
        "ingredients_text_cs": product.get("ingredients_text_cs", "") or None,
        "brands": product.get("brands", "") or None,
        "categories_tags": product.get("categories_tags", []) or [],
        "countries_tags": product.get("countries_tags", []) or [],
        "nutriscore_grade": product.get("nutriscore_grade", "") or None,
        "nova_group": product.get("nova_group") or None,
        "image_url": product.get("image_url", "") or None,
        "last_modified_t": product.get("last_modified_t") or None,
        "updated_at": datetime.now(timezone.utc).isoformat(),
    }


# ── Stažení a stream zpracování ────────────────────────────────────────────────

def process_stream(url: str, label: str) -> tuple[int, int, int]:
    """
    Streamově stáhne a zpracuje JSONL.gz soubor.
    Vrací (processed, upserted, skipped).

    Stream zpracování = nikdy nestahuje celý soubor do RAM.
    Ideální pro 7GB soubory v GitHub Actions s 7GB RAM limitem.
    """
    log.info(f"Stahuji: {label}")
    log.info(f"URL: {url}")

    processed = 0
    upserted = 0
    skipped = 0
    batch = []
    start_time = time.time()

    with requests.get(url, stream=True, timeout=300) as response:
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

                # Filtr 1: pouze EU produkty
                if not is_eu_product(product):
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
                        elapsed = time.time() - start_time
                        rate = processed / elapsed
                        log.info(
                            f"Progress: {processed:,} zpracováno, "
                            f"{upserted:,} upsertováno, "
                            f"{rate:.0f} produktů/s"
                        )

        # Zbývající batch
        if batch:
            u, _ = supabase_upsert(batch)
            upserted += u

    elapsed = time.time() - start_time
    log.info(
        f"Hotovo: {processed:,} zpracováno, "
        f"{upserted:,} upsertováno, "
        f"{skipped:,} přeskočeno, "
        f"čas: {elapsed:.0f}s"
    )

    return processed, upserted, skipped


# ── Delta export ────────────────────────────────────────────────────────────────

def get_delta_urls() -> list[str]:
    """
    Stáhne seznam delta souborů za posledních 24 hodin.
    Delta soubory mají formát: products_{unix_od}_{unix_do}.json.gz
    """
    # Stáhni index delta souborů
    response = requests.get(OFF_DELTA_BASE_URL, timeout=30)
    response.raise_for_status()

    # Parsuj HTML index (jednoduchý přístup)
    cutoff_time = int(time.time()) - 86400  # 24 hodin zpět
    urls = []

    for line in response.text.split("\n"):
        if "products_" in line and ".json.gz" in line:
            # Extrahuj název souboru z HTML
            start = line.find("products_")
            end = line.find(".json.gz", start) + len(".json.gz")
            if start != -1 and end != -1:
                filename = line[start:end]
                # Extrahuj unix timestamp konce souboru (druhé číslo v názvu)
                parts = filename.replace("products_", "").replace(".json.gz", "").split("_")
                if len(parts) == 2:
                    try:
                        file_end_time = int(parts[1])
                        if file_end_time >= cutoff_time:
                            urls.append(OFF_DELTA_BASE_URL + filename)
                    except ValueError:
                        pass

    log.info(f"Nalezeno {len(urls)} delta souborů za posledních 24h")
    return urls


# ── Hlavní logika ──────────────────────────────────────────────────────────────

def run_full_import():
    """
    Import celé databáze (filtrované na EU).
    Spouštěj jednou při inicializaci.
    Očekávaný čas: 45–90 minut (7GB soubor).
    """
    log.info("=== SPOUŠTÍM PLNÝ IMPORT ===")
    log.info("Toto může trvat 45–90 minut.")

    log_id = log_import_start("full")

    try:
        processed, upserted, skipped = process_stream(
            OFF_FULL_EXPORT_URL,
            "Open Food Facts — plný export"
        )

        log_import_finish(log_id, {
            "products_processed": processed,
            "products_upserted": upserted,
            "products_skipped": skipped,
            "status": "success",
        })

    except Exception as e:
        log.error(f"Plný import selhal: {e}")
        log_import_finish(log_id, {
            "status": "failed",
            "error_message": str(e),
        })
        raise


def run_delta_import():
    """
    Import denního delta exportu.
    Spouštěj každý den (GitHub Actions cron).
    Očekávaný čas: 2–10 minut.
    """
    log.info("=== SPOUŠTÍM DELTA IMPORT ===")

    log_id = log_import_start("delta")
    total_processed = 0
    total_upserted = 0
    total_skipped = 0

    try:
        delta_urls = get_delta_urls()

        if not delta_urls:
            log.warning("Žádné delta soubory k importu — možná ještě nejsou k dispozici")
            log_import_finish(log_id, {
                "status": "success",
                "products_processed": 0,
                "products_upserted": 0,
                "products_skipped": 0,
            })
            return

        for url in delta_urls:
            filename = url.split("/")[-1]
            processed, upserted, skipped = process_stream(url, filename)
            total_processed += processed
            total_upserted += upserted
            total_skipped += skipped

        log_import_finish(log_id, {
            "products_processed": total_processed,
            "products_upserted": total_upserted,
            "products_skipped": total_skipped,
            "status": "success",
        })

    except Exception as e:
        log.error(f"Delta import selhal: {e}")
        log_import_finish(log_id, {
            "status": "failed",
            "error_message": str(e),
        })
        raise


# ── Entry point ────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="AllergyTrack — OFF import skript")
    parser.add_argument(
        "--full",
        action="store_true",
        help="Spustí plný import celé databáze (první inicializace)",
    )
    args = parser.parse_args()

    # Zkontroluj env proměnné
    if not SUPABASE_URL or not SUPABASE_KEY:
        log.error("Chybí SUPABASE_URL nebo SUPABASE_SERVICE_KEY")
        sys.exit(1)

    if args.full:
        run_full_import()
    else:
        run_delta_import()
