# AllergyTrack — Kontext pro Claude Code

Tento dokument předej Claude Code na začátku práce.
Obsahuje vše potřebné pro pokračování vývoje bez ztráty kontextu.

---

## PROJEKT

Android aplikace **AllergyTrack** — tracker alergií propojující jídlo, prostředí
a alergické symptomy. AI korelační analýza odhaluje spouštěče.

```
Package:     com.allergytrack.app
Min SDK:     26 (Android 8.0)
Target SDK:  35
Jazyk:       Kotlin (100%)
UI jazyk:    Čeština
```

---

## TECHNICKÝ STACK

| Oblast | Technologie | Verze |
|--------|-------------|-------|
| UI | Jetpack Compose + Material 3 | BOM 2024.12.01 |
| Architektura | Clean Architecture + MVVM | — |
| DI | Hilt | 2.53.1 |
| DB | Room | 2.6.1 |
| Async | Kotlin Coroutines + StateFlow | 1.9.0 |
| Navigace | Navigation Compose | 2.8.5 |
| Síť | Retrofit 2 + Kotlin Serialization | 2.11.0 |
| Background | WorkManager | 2.10.0 |
| Kamera | CameraX | 1.4.1 |
| Grafy | Vico | 2.0.1 |
| Barcode | ML Kit Barcode Scanning | 17.3.0 |
| Build | Gradle Kotlin DSL + Version Catalog | — |

---

## ADRESÁŘOVÁ STRUKTURA

```
com.allergytrack.app/
├── AllergyTrackApplication.kt
├── MainActivity.kt
├── di/
│   ├── AppModule.kt           ← Hilt bindings (Repository interfaces)
│   ├── DatabaseModule.kt      ← Room DAOs
│   └── NetworkModule.kt       ← Retrofit instances (openmeteo, openfoodfacts)
├── ui/
│   ├── theme/                 ← Color, Theme, Type
│   ├── navigation/
│   │   ├── Screen.kt          ← Sealed class všech routes (viz Food sub-flow níže)
│   │   ├── NavGraph.kt        ← AllergyNavGraph + MainAppScaffold
│   │   └── MainAppScaffold.kt
│   ├── components/
│   │   ├── CommonUI.kt        ← AppCard, CardTitle(icon), Pill(onClick), PrimaryButton,
│   │   │                         SecondaryButton, SectionHeading, AiBadge
│   │   ├── BottomNavBar.kt
│   │   ├── PremiumGate.kt     ← Třístavový wrapper (hidden/upsell/content)
│   │   ├── PremiumUpsellCard.kt
│   │   ├── PremiumBadge.kt
│   │   └── PermissionsHandler.kt
│   ├── onboarding/
│   │   ├── OnboardingFlow.kt
│   │   ├── OnboardingViewModel.kt
│   │   ├── OnboardingComponents.kt
│   │   ├── OnboardingWelcomeScreen.kt
│   │   ├── OnboardingProfileScreen.kt
│   │   ├── OnboardingPermissionsScreen.kt
│   │   └── OnboardingReadyScreen.kt
│   └── screens/
│       ├── dashboard/
│       │   ├── DashboardScreen.kt
│       │   └── DashboardViewModel.kt
│       ├── food/
│       │   ├── FoodLogScreen.kt          ← Hub: foto/čárový kód/hlas/vyhledávání + recent pills
│       │   ├── FoodLogViewModel.kt       ← Sdílený VM pro celý food sub-flow
│       │   ├── CameraScreen.kt           ← mode="photo" nebo mode="barcode" (ML Kit)
│       │   ├── FoodConfirmScreen.kt      ← Potvrzení + uložení záznamu
│       │   ├── FoodManualSearchScreen.kt ← Vyhledávání v OFF, info banner, 503 handling
│       │   └── FoodManualEntryScreen.kt  ← Ruční zadání názvu + výběr alergenů ze 14 EU
│       ├── symptom/
│       │   ├── SymptomLogScreen.kt
│       │   └── SymptomLogViewModel.kt
│       ├── charts/
│       │   ├── ChartsScreen.kt
│       │   ├── ChartsViewModel.kt
│       │   └── ChartDataMapper.kt
│       ├── analysis/
│       │   ├── AnalysisScreen.kt
│       │   └── AnalysisViewModel.kt
│       ├── profile/
│       │   ├── ProfileScreen.kt
│       │   ├── ProfileViewModel.kt
│       │   └── AboutScreen.kt
│       └── settings/
│           ├── SettingsScreen.kt         ← Obsahuje FoodLanguageDialog
│           ├── SettingsViewModel.kt      ← FOOD_LANGUAGE_OPTIONS, setFoodSearchLanguage()
│           ├── DebugMenuScreen.kt        ← Jen v BuildConfig.DEBUG
│           └── DebugMenuViewModel.kt
├── domain/
│   ├── model/
│   │   ├── FoodLog.kt
│   │   ├── SymptomLog.kt
│   │   ├── EnvironmentLog.kt
│   │   ├── SymptomType.kt
│   │   ├── Correlation.kt             ← TriggerType, CorrelationSeverity
│   │   ├── PremiumConfig.kt           ← PremiumVisibilityMode, PremiumStatus, PremiumFeature
│   │   ├── DataAvailability.kt        ← EnvironmentRegion, isEurope()
│   │   └── OnboardingModels.kt        ← AllergyType, SymptomTypeOnboarding, OnboardingStep
│   ├── repository/
│   │   ├── FoodRepository.kt
│   │   ├── SymptomRepository.kt
│   │   ├── EnvironmentRepository.kt
│   │   └── PremiumRepository.kt
│   ├── premium/
│   │   └── PremiumHelper.kt           ← Centrální správce přístupu k funkcím
│   ├── analysis/
│   │   └── CorrelationEngine.kt       ← Pearsonova korelace, time window 2–72h
│   └── usecase/
│       ├── SaveFoodLogUseCase.kt
│       ├── SaveSymptomLogUseCase.kt
│       ├── SearchFoodUseCase.kt
│       ├── GetTodayLogsUseCase.kt
│       ├── GetCorrelationsUseCase.kt
│       └── CheckPremiumAccessUseCase.kt
├── data/
│   ├── local/
│   │   ├── database/
│   │   │   ├── AllergyDatabase.kt     ← Room DB, version 2
│   │   │   ├── entity/
│   │   │   │   ├── FoodLogEntity.kt
│   │   │   │   ├── SymptomLogEntity.kt
│   │   │   │   └── EnvironmentLogEntity.kt
│   │   │   └── dao/
│   │   │       ├── FoodLogDao.kt
│   │   │       ├── SymptomLogDao.kt
│   │   │       └── EnvironmentLogDao.kt
│   │   └── datastore/
│   │       ├── OnboardingDataStore.kt       ← isCompleted, allergyProfile
│   │       ├── UserPreferencesDataStore.kt  ← food_search_language ("" = auto)
│   │       └── DebugPreferences.kt          ← debug_preferences, DebugPremiumOverride
│   ├── remote/
│   │   ├── airquality/
│   │   │   ├── AirQualityApiService.kt ← Open-Meteo, zdarma, bez klíče
│   │   │   └── AirQualityDto.kt
│   │   └── food/
│   │       ├── OpenFoodFactsApiService.kt  ← @Query("lc"), @Query("cc") jako parametry
│   │       ├── FoodDto.kt                  ← RecognizedFood, OpenFoodProduct (cs fallback)
│   │       └── FoodAllergenHelper.kt       ← EU_14 definice + suggestFromName() heuristika
│   ├── repository/
│   │   ├── FoodRepositoryImpl.kt      ← resolvedLocale(), lokalizované názvy, 503 propagace
│   │   ├── SymptomRepositoryImpl.kt
│   │   ├── EnvironmentRepositoryImpl.kt
│   │   └── PremiumRepositoryImpl.kt   ← MVP: vždy FREE
│   ├── permissions/
│   │   └── PermissionsHelper.kt       ← GPS, kamera, notifikace, mikrofon
│   └── worker/
│       ├── EnvironmentWorker.kt       ← HiltWorker, GPS + AQI každých 30 min
│       └── WorkManagerScheduler.kt
```

---

## KLÍČOVÉ ARCHITEKTONICKÉ PRINCIPY

### 1. Food Log — celý flow zadávání jídla

Vstupní bod je `FoodLogScreen`, sdílený ViewModel je `FoodLogViewModel`. Všechny pod-obrazovky food flow používají **stejnou instanci** `FoodLogViewModel` (Hilt scope = NavBackStackEntry).

#### Navigation routes (Screen.kt):
```
camera/{mode}                    ← mode = "photo" | "barcode"
food_confirm/{foodName}/{allergens}
food_manual_search
food_manual_entry/{foodName}
```

Parametry `foodName` a `allergens` jsou **vždy URL-enkódované** pomocí `Uri.encode()` před navigací.
`allergens` je `|`-separovaný řetězec alergenu klíčů (např. `"Milk|Gluten|Eggs"`).

#### Cesty jak uživatel může zadat jídlo:

**A) Foto (Premium)**
`FoodLogScreen` → `CameraScreen(mode=photo)` → ML Kit image labeling → `SearchFoodUseCase` → `FoodConfirmScreen`

**B) Čárový kód**
`FoodLogScreen` → `CameraScreen(mode=barcode)` → ML Kit BarcodeScanning → `getFoodByBarcode()` → `FoodConfirmScreen`

**C) Hlas**
`FoodLogScreen` → `SpeechRecognizer` dialog → rozpoznaný text → `onSearchQueryChange()` → navigace na `FoodManualSearchScreen` s předvyplněným dotazem

**D) Vyhledávání (Open Food Facts)**
`FoodLogScreen` (tap na search card) → `FoodManualSearchScreen` → debounced search (400ms, min 2 znaky) → `SearchFoodUseCase` → OFF API → výsledky → `FoodConfirmScreen`

**E) Ruční zadání (nová obrazovka)**
`FoodManualSearchScreen` (tlačítko "Přidat ručně") → `FoodManualEntryScreen` → název + výběr alergenů → `FoodConfirmScreen`

#### Recently Logged Pills (FoodLogScreen):
- Zobrazí posledních 10 záznamů z `foodRepository.getAllLogs().map { it.take(10) }`
- Červené = jídlo s alergeny, šedé = bez alergenů
- Klikatelné → navigace přímo na `FoodConfirmScreen` s předvyplněnými daty

#### FoodManualSearchScreen — důležité detaily:
- Info banner vysvětluje zdroj: "Open Food Facts = balené produkty s čárovým kódem"
- Tlačítko "Zadat ručně" je **vždy viditelné** (ne jen při prázdných výsledcích)
- Styl tlačítka: **Primary** = žádné výsledky nebo prázdné pole; **Secondary (Outlined)** = výsledky nalezeny
- **HTTP 503 handling**: `FoodSearchState.isServiceUnavailable = true` → zobrazí specifickou zprávu o přetížení OFF serveru místo "Nic nebylo nalezeno"

#### FoodManualEntryScreen — detaily:
- Editovatelné textové pole pro název jídla (předvyplněno z `foodName` argumentu)
- `FlowRow` s `FilterChip` pro všech 14 EU povinných alergenů (multi-select)
- Tlačítko "Navrhnout alergeny" → spustí `viewModel.suggestAllergens(name)`
  - **Krok A (okamžitý)**: `FoodAllergenHelper.suggestFromName()` — lokální heuristika přes ~80 CZ/EN klíčových slov
  - **Krok B (síť)**: `SearchFoodUseCase(name)` → alergeny z prvního výsledku OFF sloučeny s lokálními
- Navržené alergeny se automaticky předvyberou v chipech; uživatel může cokoliv doladit
- Potvrzení → `FoodConfirmScreen` s vybraným seznamem alergenů

#### FoodAllergenHelper (object, `data/remote/food/`):
```kotlin
FoodAllergenHelper.EU_14  // List<AllergenDefinition(key, displayRes)>
FoodAllergenHelper.suggestFromName(name: String): Set<String>
```
Klíče alergenů jsou shodné s hodnotami vracenými OFF API po `removePrefix("en:").capitalize()`:
`"Gluten"`, `"Milk"`, `"Eggs"`, `"Fish"`, `"Crustaceans"`, `"Peanuts"`, `"Soybeans"`,
`"Nuts"`, `"Celery"`, `"Mustard"`, `"Sesame-seeds"`, `"Sulphur-dioxide-and-sulphites"`,
`"Lupin"`, `"Molluscs"`

### 2. Open Food Facts API — detaily implementace

**Base URL:** `https://world.openfoodfacts.org/`

**Search endpoint:**
```
GET cgi/search.pl
  ?search_simple=1&action=process&json=1
  &sort_by=unique_scans_n
  &fields=product_name,product_name_cs,allergens_tags,ingredients_text,ingredients_text_cs,image_url
  &search_terms={query}
  &lc={languageCode}    ← dynamické, z UserPreferencesDataStore nebo Locale.getDefault()
  &cc={countryCode}     ← odvozeno z lc (cs→cz, de→de, en→gb, ...)
```

**Barcode endpoint:**
```
GET api/v0/product/{barcode}.json
  ?fields=product_name,product_name_cs,allergens_tags,ingredients_text,ingredients_text_cs,image_url
  &lc={languageCode}
  &cc={countryCode}
```

**Rate limits (důležité!):**
- Search: 10 req/min/IP → překročení vrátí **HTTP 503**
- Read product: 15 req/min/IP
- 503 se propaguje jako `retrofit2.HttpException` z repository do ViewModelu

**Lokalizace výsledků:**
- `lc` parametr řídí jazyk `product_name` v odpovědi (pokud překlad existuje)
- Pro češtinu: záloha `product_name_cs` pokud `product_name` není v češtině
- Nastavení v Settings: uživatel může přepsat auto-detekci ze `UserPreferencesDataStore`
- Auto-režim: `Locale.getDefault().language` + `.country`

**Databáze OFF:** Obsahuje primárně **balené produkty s čárovým kódem**. Čerstvé potraviny, zelenina, restaurační jídla v databázi typicky nejsou → proto existuje `FoodManualEntryScreen`.

### 3. Premium systém

```
PremiumHelper.visibilityMode = HIDDEN  ← aktuální stav
```

**HIDDEN** = prémiové funkce fyzicky neexistují v UI.
**VISIBLE** = viditelné, FREE uživatel vidí hvězdičku + upsell.
Přepnutí = **jeden řádek** v `PremiumHelper.kt`.

ViewModel vystavuje trojici StateFlow pro každou prémiovou funkci:
```kotlin
val isXxxVisible: StateFlow<Boolean>
val showXxxUpsell: StateFlow<Boolean>
val showXxxBadge: StateFlow<Boolean>
```

`PremiumGate` composable:
```kotlin
when {
    !isVisible -> { /* nezobrazit nic */ }
    showUpsell -> PremiumUpsellCard(...)
    else       -> content()
}
```

V DEBUG buildu: `DebugPreferences` přepisuje stav přes `DebugPremiumOverride` enum.

**Prémiové funkce** (aktuálně všechny HIDDEN):
- `FOOD_PHOTO_RECOGNITION` — foto rozpoznávání jídla
- `POLLEN_DATA` — pylová data
- `EXTENDED_CHARTS` — grafy 30+ dní
- `AI_CORRELATION_ANALYSIS` — AI korelační analýza
- `PDF_EXPORT` — export PDF pro lékaře
- `UNLIMITED_HISTORY` — neomezená historie

### 4. Environmentální data

Open-Meteo API — bez klíče:
```
https://air-quality-api.open-meteo.com/v1/air-quality
  ?current=european_aqi,pm2_5,grass_pollen,birch_pollen,...
```

`pollenGrassIndex: Int?` — **null = nedostupná**, **0 = dostupná ale nulová**. Nezaměňovat!
`EnvironmentLog.region` — `EUROPE` nebo `GLOBAL`. Pyl pouze v Evropě (únor–říjen).
UI zobrazuje `PollenUnavailableBanner` místo prázdného stavu.

### 5. Onboarding a Alergický profil

```
Welcome → AllergyProfile → Permissions → Ready → Dashboard
```

Profil uložen v `OnboardingDataStore` (klíče: `selected_allergy_types`, `selected_symptom_types`).
Formát: čárkou oddělené enum názvy (např. `"FOOD,POLLEN"`).

`AnalysisViewModel` využívá profil v `CorrelationEngine.analyze()` → `isExpected = true` → 10% bonus ke skóre, štítek "SHODA" v UI.

### 6. Korelační engine

- Min. 5 symptomů pro aktivaci
- Pearsonova korelace pro enviro data
- Sliding time window 2–72h pro potravinové korelace
- `CorrelationSeverity`: HIGH (≥70%), MEDIUM (40–70%), LOW (<40%)

### 7. Room DB

Aktuální verze: **2**.
`MIGRATION_1_2`: přidává `no2`, `ozone`, `region`, `pollenAvailable` do `environment_logs`.
Každá změna schématu = zvýšení verze + Migration objekt.

### 8. UserPreferencesDataStore

`user_preferences` DataStore — uživatelská nastavení přesahující onboarding:

| Klíč | Typ | Výchozí | Popis |
|------|-----|---------|-------|
| `food_search_language` | String | `""` | Kód jazyka pro OFF API; prázdný = auto z `Locale.getDefault()` |

---

## API PŘEHLED

| API | URL | Klíč | Cena | Účel |
|-----|-----|------|------|------|
| Open-Meteo AQI | air-quality-api.open-meteo.com | ❌ | Zdarma (nekomerční) | AQI + pyl |
| Open Food Facts | world.openfoodfacts.org | ❌ | Zdarma (ODbL) | Jídlo + alergeny (balené produkty) |

**Žádný API klíč není potřeba pro MVP.**

---

## MONETIZACE A VYDÁNÍ

**Aktuální stav: Cesta 3 — Pure Free**
- Vše zdarma, žádná monetizace
- Google Play: Personal účet, kategorie **Lifestyle**
- Kategorie Lifestyle = žádná Health declaration, žádný D-U-N-S číslo

**Budoucí přechod na freemium:**
1. `visibilityMode = HIDDEN` → `VISIBLE` v PremiumHelper
2. Implementovat Google Play Billing v `PremiumRepositoryImpl`
3. Aktivovat Open-Meteo commercial plan ($29/měsíc)
4. Převést Play účet Personal → Organization (D-U-N-S číslo)

---

## ZÁVAZNÁ PRAVIDLA

```
✅ Kotlin 100% — žádný Java kód
✅ Jetpack Compose — žádný XML layout
✅ Hilt DI — @HiltViewModel + @Inject constructor vždy
✅ StateFlow — žádný LiveData
✅ KSP — žádný KAPT
✅ Repository pattern — UI nikdy nevolá DAO přímo
✅ Kotlin Serialization — žádný Gson
✅ Version Catalog (libs.versions.toml) — závislosti nikdy jako string
✅ Premium check pouze přes PremiumHelper — nikde jinde
✅ API klíče pouze v local.properties + BuildConfig
✅ Debug kód vždy obaleno if (BuildConfig.DEBUG)
✅ Všechny hlavní obrazovky mají horizontální padding 16.dp
✅ Veškerý text UI v češtině (resources values-cs/ jsou primární)
✅ foodName a allergens v navigaci vždy Uri.encode() před navigate()
✅ HttpException ze síťových volání propagovat do ViewModelu — nekousat v repository

❌ runBlocking v produkčním kódu (výjimka: MainActivity onCreate)
❌ Přímé volání DAO z ViewModelu
❌ Byznys logika v Composable funkcích
❌ Nullable !! operátor — vždy ?: return nebo ?.let
❌ Hardcoded API klíče nebo URLs v kódu
```

---

## STAV IMPLEMENTACE

| Fáze | Obsah | Stav |
|------|-------|------|
| 1 | Projekt, struktura, Dashboard, Room, Hilt | ✅ |
| 2 | FoodLog, kamera, Open Food Facts API | ✅ |
| 3 | SymptomLog, intenzita, části těla | ✅ |
| 4a | EnvironmentWorker, GPS, WorkManager | ✅ |
| 4b | Patch: Open-Meteo místo Google Pollen | ✅ |
| 4c | Graceful degradation, nullable pyl, regiony | ✅ |
| 5 | ChartsScreen, Vico grafy, filtry | ✅ |
| 6 | AnalysisScreen, CorrelationEngine, AI shrnutí | ✅ |
| 7 | PremiumHelper, HIDDEN/VISIBLE mód, PremiumGate | ✅ |
| 8 | ProfileScreen, AboutScreen, SettingsScreen, DebugMenu | ✅ |
| 9 | Onboarding (4 kroky), PermissionsHelper, OnboardingDataStore | ✅ |
| 10a | FoodLog UX: barcode scanner (ML Kit), voice input (SpeechRecognizer) | ✅ |
| 10b | FoodLog UX: recently logged pills klikatelné, Premium gate na foto | ✅ |
| 10c | OFF API lokalizace: dynamické lc/cc, UserPreferencesDataStore, nastavení jazyka | ✅ |
| 10d | FoodManualEntryScreen: ruční zadání + EU 14 alergenů + návrh alergenů (A+B) | ✅ |
| 10e | FoodManualSearchScreen: info banner, always-visible "Přidat ručně", 503 handling | ✅ |

---

## DOPORUČENÉ DALŠÍ KROKY

**Kvalita a stabilita:**
- Unit testy pro `CorrelationEngine` (kritická logika)
- Unit testy pro `FoodAllergenHelper.suggestFromName()` (pokrytí CZ/EN klíčových slov)
- Unit testy pro `PremiumHelper` (oba módy + debug override)
- Error handling v `EnvironmentWorker` (retry strategie)

**UX vylepšení:**
- Animace při přidání záznamu na Dashboard (timeline item appear)
- Snackbar feedback po uložení jídla/symptomu
- Pull-to-refresh na Dashboardu
- Empty state pro Charts a Analysis (první týden bez dat)

**Funkce:**
- Notifikace — večerní připomínka logování (WorkManager + NotificationManager)
- Sdílení / PDF export reportu (placeholder → skutečný PDF)
- Nastavení intervalu WorkManager (30 min default, uživatel může změnit)

**Technický dluh:**
- `AiSummaryService.generateSummary()` je lokální šablona → napojit na Claude API
- `recognizeFoodFromImage()` je placeholder → napojit na FatSecret nebo Passio API
- `FoodAllergenHelper.suggestFromName()` — varianta C (Claude API) pro přesnější návrhy alergenů (naplánováno po spuštění monetizace)
- `CameraScreen.kt` — navigace na `FoodConfirmScreen` bez `Uri.encode()` (drobná chyba pro produkty s mezerami v názvu)

---

## JAK PRACOVAT S TÍMTO DOKUMENTEM

1. Vždy dodržuj pravidla z sekce **ZÁVAZNÁ PRAVIDLA**
2. Před přidáním nové funkce zkontroluj **ADRESÁŘOVOU STRUKTURU** a zařaď správně
3. Prémiové funkce přidávej do `PremiumFeature` enum a `PremiumHelper.premiumFeatures`
4. Nové debug nástroje vždy obaluj `if (BuildConfig.DEBUG)`
5. Environmentální data vždy ošetřuj jako nullable (`pollenGrassIndex: Int?`)
6. Každá změna Room schématu = zvýšení verze + Migration objekt
7. Nové uživatelské preference (ne-onboarding) patří do `UserPreferencesDataStore`

---

## RYCHLÁ REFERENCE — PŘIDÁNÍ NOVÉ FUNKCE

### Nová obrazovka:
```
1. ui/screens/xxx/XxxScreen.kt + XxxViewModel.kt
2. Screen.kt → přidat object Xxx : Screen("xxx")
3. NavGraph.kt → přidat composable(Screen.Xxx.route) { ... }
4. Volitelně: BottomNavBar.kt
```

### Nová prémiová funkce:
```
1. PremiumConfig.kt → přidat do PremiumFeature enum
2. PremiumHelper.kt → přidat do premiumFeatures set
3. ViewModel → přidat trojici: isXxxVisible, showXxxUpsell, showXxxBadge
4. Screen → obalit PremiumGate nebo if (isVisible)
```

### Nová Room entita:
```
1. data/local/database/entity/XxxEntity.kt
2. data/local/database/dao/XxxDao.kt
3. AllergyDatabase.kt → přidat do entities = [...], zvýšit version
4. Přidat MIGRATION objekt, přidat do addMigrations(...)
5. DatabaseModule.kt → @Provides fun provideXxxDao()
6. domain/model/Xxx.kt → doménový model
7. Mapper extension funkce v RepositoryImpl
```

### Nová uživatelská preference (non-onboarding):
```
1. UserPreferencesDataStore.kt → přidat PreferencesKey + Flow + setter
2. Příslušný ViewModel → inject UserPreferencesDataStore, stateIn flow
3. Příslušná obrazovka → UI prvek pro nastavení
```

### Nový debug nástroj:
```
1. DebugPreferences.kt → nový PreferencesKey + Flow + setter
2. DebugMenuViewModel.kt → přidat StateFlow + akci
3. DebugMenuScreen.kt → přidat UI prvek obalený if (BuildConfig.DEBUG)
4. Příslušný Helper/ViewModel → číst override s if (!BuildConfig.DEBUG) guard
```
