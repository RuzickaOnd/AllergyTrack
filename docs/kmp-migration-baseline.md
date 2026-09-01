# Baseline před KMP migrací

Zaznamenáno: 2026-09-01
Zařízení: emulátor `sdk_gphone64_arm64`, Android 16 (API 36), locale **en**
Build: versionName 1.0.3, versionCode 1000302, commit `2a5b46a`
Větev: `feature/AT_migrace_do_KMP`
Záloha DB: `baseline/allergy_track.db` (necommitovaná, viz .gitignore)

## Stav vyplnění

| Část | Stav |
|---|---|
| Charakterizační testy | ✅ hotovo — 29 testů, viz níže |
| Záloha databáze | ✅ hotovo — viz níže |
| Smoke průchod | ◐ částečně — ověřen Dashboard, Analýza, Grafy |
| Zaznamenané hodnoty | ✅ hotovo |

**Pozor na povahu dat.** Aplikace na emulátoru byla prázdná (WAL 53 kB obsahoval
jen schéma, nula řádků). Data jsou proto **syntetická**, vložená přímo do SQLite:
8× jogurt s alergenem `Milk`, po každém do 4 h ekzém; 4× rýže bez alergenu jako
kontrolní vzorek; 8 environment záznamů. Schéma i migrační cesta jsou skutečné,
takže pro ověření Room KMP ve fázi 6 to slouží dobře — ale nejde o reálná
uživatelská data.

## Charakterizační testy (hotovo)

Spuštění: `./gradlew :app:testDebugUnitTest`

| Třída | Testů | Co hlídá |
|---|---|---|
| `CorrelationEngineCharacterizationTest` | 10 | práh MIN_SYMPTOMS, skóre korelací, `isExpected`, řazení, `evaluateReadiness` |
| `FoodAllergenHelperCharacterizationTest` | 7 | heuristika klíčových slov, case-insensitivita, obsah EU_14 |
| `FoodDtoMapperCharacterizationTest` | 12 | lokalizace názvů a ingrediencí, čištění alergenů, `nameIsLocalized` |

**Celkem 29 testů.** Ve fázích 5–7 se přesunou do `shared/src/commonTest/` a musí tam dát identické výsledky.

### Zjištění zapsaná do testů

1. **`isExpected` neporovnává klíč alergenu.** `CorrelationEngine:89` testuje
   `"FOOD" in knownAllergies`, tedy název enumu `AllergyType` z onboardingu.
   Příznak proto dostanou všechny potravinové korelace naráz, ne jen odpovídající.
   Zapsáno včetně negativního případu (`knownAllergies = ["Milk"]` → `isExpected = false`).
2. **Heuristika alergenů hledá podřetězec, takže trefí víc alergenů naráz.**
   „Arašídové máslo" vrací `Peanuts` i `Milk` (kvůli slovu „máslo").
3. **Diakritika se neohýbá.** „Sójová omáčka" nenavrhne `Soybeans`, protože
   `"sójová"` neobsahuje `"sója"` ani `"sójový"`. Trefí až „Sójový nápoj".

## Obsah databáze

| Tabulka | Počet záznamů |
|---|---|
| `food_logs` | 12 |
| `symptom_logs` | 8 |
| `environment_logs` | 8 |

`PRAGMA user_version` = **2**

**Past při tahání zálohy:** data byla ve WAL, ne v hlavním souboru
(`allergy_track.db` 4 kB, `-wal` 53 kB). Kopírovat samotný `.db` vrátí prázdnou
databázi. Buď zkopíruj všechny tři soubory (`.db`, `-shm`, `-wal`), nebo nejdřív
proveď `PRAGMA wal_checkpoint(TRUNCATE)`.

**Cesty k DataStore souborům** (ověřeno na zařízení, klíčové pro fázi 6):

```
files/datastore/user_preferences.preferences_pb
files/datastore/onboarding_preferences.preferences_pb
```

Soubor onboardingu se jmenuje **`onboarding_preferences`**, ne `onboarding_prefs`,
jak spec předpokládal. `debug_preferences.preferences_pb` zatím neexistuje —
vznikne až při prvním zápisu z Debug menu.

Onboarding profil v době záznamu:
`selected_allergy_types = POLLEN,DUST,FOOD,ANIMAL`,
`selected_symptom_types = EYE_ITCHING,ITCHING,ECZEMA,DIGESTIVE`

## Smoke průchod

Každý bod projít a zaškrtnout. Opakuje se po každé fázi migrace.

- [ ] Onboarding (nová instalace) — 4 kroky až na Dashboard
- [ ] Záznam jídla vyhledáváním v Supabase → potvrzení → uložení
- [ ] Záznam jídla čárovým kódem
- [ ] Záznam jídla ručním zadáním + tlačítko „Navrhnout alergeny"
- [ ] Záznam příznaku včetně změny data a času
- [x] Dashboard — načte data z DB, zobrazí AQI, premium upsell, clinical insight
- [x] Grafy — vykreslí AQI vs. symptomy, filtr příznaků, premium brány
- [x] Analýza — korelace i AI shrnutí
- [ ] Export PDF a sdílení
- [ ] Nastavení — tmavý režim, notifikace, přepnutí jazyka
- [ ] Profil — změna avataru a jména

Nezaškrtnuté body vyžadují ruční interakci (zadávání textu, kamera, systémové
dialogy). Zaškrtnuté byly ověřeny screenshotem přes `adb`.

## Zaznamenané konkrétní hodnoty

Slouží ke srovnání po migraci — musí zůstat identické.

| Hodnota | Baseline |
|---|---|
| Nejvyšší korelace v Analýze | **Milk → Eczema, 100 %, 8/8** |
| Druhý spouštěč v seznamu | Rýže basmati (zamčený, premium) |
| Readiness disclaimer | „zaznamenej ještě 2 symptomů" (8 symptomů = Partial) |
| Grafy, 7denní rozsah | 2 symptomy, průměr 3.5, nejčastější Eczema |
| Dashboard — AQI | 49 |
| Dashboard — dnešní záznamy | „No records today yet" (data jsou 6–13 dní stará) |
| AI shrnutí, první věta | „Na základě tvých záznamů jsem identifikoval hlavní spouštěč: **Milk** koreluje s Eczema v 100 % případů" |

### Zjištění: hardcoded české řetězce mimo resources

UI běželo v angličtině, ale disclaimer i AI shrnutí byly česky. Jsou zadrátované
přímo v kódu, ne ve `strings.xml`:

- `AnalysisReadiness.kt:26` — `"Zaznamenej alespoň $requiredCount symptomy pro první analýzu"`
- `AiSummaryService.kt:90` — `"Na základě tvých záznamů jsem identifikoval hlavní spouštěč: "`

**Dopad na fázi 7:** migrace `strings.xml` do Compose Resources je nezachytí.
Navíc `AnalysisReadiness` je doménový model, který jde do `commonMain` už ve
fázi 3 — český text tam půjde s ním. Není to blokující, ale patří to do seznamu
technického dluhu.

---

## Po fázi 2 (Hilt → Koin)

Ověřeno: 2026-09-01, na commitu předcházejícím `3ad2c40`
Zařízení: emulátor `sdk_gphone64_arm64`, Android 16 (API 36)

- [x] Aplikace startuje bez `NoDefinitionFoundException`
- [x] Nejvyšší korelace v Analýze: **Milk → Eczema, 100 %, 8/8** — shodné s baseline
- [x] AI shrnutí a readiness disclaimer — shodný text
- [x] Dashboard, Grafy, Profil, Nastavení, Food log se vykreslují správně
- [x] Recently logged pills rozlišují alergeny barvou (Jogurt červeně, Rýže šedě)
- [x] `SettingsViewModel` se 14 závislostmi se rozřeší
- [x] Release build s R8 funkční — **bez jakýchkoli keep pravidel pro Koin**
- [x] Release build ověřen i za běhu, Analýza vrací shodnou hodnotu
- [x] `:shared` — všechny tři iOS targety kompilují, `allTests` zelené
- [x] 31 unit testů zelených

### Poznámky k prostředí

**Instalace na emulátoru vyžaduje omezení ABI.** `/data` je z 93 % plné a debug
APK má 98 MB (60 MB z toho jsou nativní knihovny pro čtyři ABI). Řešení bez
zásahu do build souboru:

```
./gradlew :app:installDebug -Pandroid.injected.build.abi=arm64-v8a
```

**Release build není podepsaný.** Projekt nemá `signingConfig` pro release,
vzniká `app-release-unsigned.apk`. Pro ověření R8 na zařízení je potřeba ho
podepsat — pro test stačí debug keystore:

```
zipalign -f -p 4 app-release-unsigned.apk out.aligned
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android \
  --ks-key-alias androiddebugkey --key-pass pass:android --out out.apk out.aligned
```

**KoinModuleGraphTest.** Graf závislostí hlídá unit test s Koin `verify()`.
Při každé změně Koin modulů musí seznam v testu zůstat shodný se seznamem
v `AllergyTrackApplication.startKoin`.

---

## Fáze 5 — Retrofit → Ktor

### Ktor 3.2.0 nelze použít s minSdk < 35

Build selže při dexování s chybou:

```
D8: Space characters in SimpleName 'use streaming syntax' are not allowed
prior to DEX version 040 (field name `use streaming syntax`
on class io.ktor.client.plugins.Messages)
```

Ktor 3.2.0 má pole pojmenované přes backticky s mezerami. DEX to povoluje
až od verze 040, což odpovídá API 35+. Projekt má `minSdk = 26`.

**Projekt je tím omezen na Ktor 3.1.x**, dokud se buď nezvýší minSdk,
nebo to Ktor neopraví. Použitá verze: **3.1.3**.

### Hranice modulu: Ktor zůstává uvnitř :shared

`:app` nemá Ktor v produkčních závislostech. Klienti se vytvářejí
továrnami `createAirQualityApi()` a `createSupabaseFoodApi()`
v `shared/data/remote/ApiFactory.kt`, takže do DI v `:app` se
žádný Ktor typ nedostane.

První pokus přidat `implementation(libs.ktor.client.core)` do `:app`
kvůli jednomu nedosažitelnému catch bloku vedl přesně na chybu dexování
popsanou výše. Místo toho vznikl v `:shared` typ `ApiHttpException`,
který platformní vrstvě umožní reagovat na HTTP stavy bez Ktoru
na classpath.

`testImplementation(libs.ktor.client.core)` v `:app` zůstává — testy
běží na JVM, do APK nejdou, a `KoinModuleGraphTest` potřebuje vidět
typ `HttpClient`.

### Co bylo ověřeno

- [x] **Supabase přes Ktor** — vyhledávání „jogurt" vrátilo výsledky
      s českými názvy a alergenovými odznaky. Prokazuje funkční Ktor
      klient, hlavičky `apikey`/`Authorization`, RPC endpoint
      `search_products` i lokalizační mappery.
- [x] **Open-Meteo kontrakt** — přímé volání URL, jaké staví
      `AirQualityApi`, vrací pole přesně odpovídající `AirQualityDto`
      (`european_aqi`, `pm2_5`, `pm10`, `nitrogen_dioxide`, `ozone`
      + pylová pole).
- [x] Analýza vrací shodnou hodnotu Milk → Eczema 100 % (8/8)
- [x] iOS targety kompilují, `:shared:allTests` zelené

### Co ověřeno NEBYLO

**Celý řetězec EnvironmentWorker → Open-Meteo → databáze na zařízení.**
Worker je periodický (30 min) a nepodařilo se ho vynutit — JobScheduler
joby aplikace nevidí a Debug menu umí jen vygenerovat falešná data,
živý fetch nespouští. Ověřen tedy transport (přes Supabase) a kontrakt
(přímým voláním), ne však jejich spojení za běhu.

`AirQualityApi.getHourlyHistory()` je mrtvý kód — nevolá se nikde
a nevolal se ani v Retrofit verzi. Přenesen beze změny.
