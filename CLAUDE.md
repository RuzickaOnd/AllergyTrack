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
| Analytics | Firebase Analytics + Crashlytics | BOM 34.13.0 |
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
│   └── NetworkModule.kt       ← Retrofit instances (openmeteo, supabase)
├── ui/
│   ├── theme/                 ← Color, Theme, Type
│   ├── navigation/
│   │   ├── Screen.kt          ← Sealed class všech routes (viz Food sub-flow níže)
│   │   ├── NavGraph.kt        ← AllergyNavGraph + MainAppScaffold
│   │   └── MainAppScaffold.kt
│   ├── util/
│   │   ├── StringResExt.kt
│   │   └── ExportUtils.kt     ← AnalysisExportHandler, sharePdfFile()
│   ├── components/
│   │   ├── CommonUI.kt        ← AppCard(contentPadding), AppSnackbarHost, CardTitle(icon), Pill(onClick), PrimaryButton,
│   │   │                         SecondaryButton, SectionHeading, AiBadge (renderuje **bold** markdown)
│   │   ├── BottomNavBar.kt
│   │   ├── DateTimePickerRow.kt ← Výběr data a času pro logování jídla a příznaků
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
│       │   ├── FoodManualSearchScreen.kt ← Vyhledávání v Supabase, info banner
│       │   └── FoodManualEntryScreen.kt  ← Ruční zadání názvu + výběr alergenů ze 14 EU
│       ├── symptom/
│       │   ├── SymptomLogScreen.kt
│       │   └── SymptomLogViewModel.kt
│       ├── charts/
│       │   ├── ChartsScreen.kt
│       │   ├── ChartsViewModel.kt
│       │   ├── ChartDataMapper.kt
│       │   ├── SymptomDetailActivity.kt   ← vstupní bod detailu (samostatná Activity)
│       │   ├── SymptomDetailScreen.kt     ← statistiky symptomů: count, avg/max intenzita, last occurrence
│       │   └── SymptomDetailViewModel.kt  ← SymptomTypeStat, SymptomDetailUiState; respektuje EXTENDED_CHARTS
│       ├── analysis/
│       │   ├── AnalysisScreen.kt
│       │   └── AnalysisViewModel.kt
│       ├── profile/
│       │   ├── ProfileScreen.kt       ← Avatar s pencil badge, inline editace jména
│       │   ├── AvatarPickerScreen.kt  ← 12 presetů (LazyVerticalGrid 4 sl.) + galerie
│       │   ├── ProfileViewModel.kt    ← UserPreferencesDataStore: userName, avatarSelection
│       │   └── AboutScreen.kt
│       ├── settings/
│       │   ├── SettingsScreen.kt         ← Plně implementovaná nastavení (viz sekce 11)
│       │   └── SettingsViewModel.kt      ← export, smazání dat, dark mode, notifikace, jazyk
│       └── debug/
│           ├── DebugMenuScreen.kt        ← Jen v BuildConfig.DEBUG; info chip + Crashlytics testovací tlačítka
│           └── DebugMenuViewModel.kt     ← injektuje PremiumHelper pro currentVisibilityMode
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
│   ├── export/
│   │   └── MedicalReportExporter.kt   ← Generování PDF (A4, Canvas, MediaStore)
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
│   │       ├── UserPreferencesDataStore.kt  ← 5 klíčů: jazyk, jméno, avatar, dark mode, notifikace
│   │       └── DebugPreferences.kt          ← debug_preferences, DebugPremiumOverride
│   ├── remote/
│   │   ├── ai/
│   │   │   └── AiSummaryService.kt     ← Lokální šablona shrnutí; renderuje **bold** markdown
│   │   ├── airquality/
│   │   │   ├── AirQualityApiService.kt ← Open-Meteo, zdarma, bez klíče
│   │   │   └── AirQualityDto.kt
│   │   └── food/
│   │       ├── SupabaseFoodApiService.kt   ← REST + RPC (search_products)
│   │       ├── FoodDto.kt                  ← SupabaseFoodProduct, SupabaseSearchResult, RecognizedFood
│   │       └── FoodAllergenHelper.kt       ← EU_14 definice + suggestFromName() heuristika
│   ├── repository/
│   │   ├── FoodRepositoryImpl.kt      ← Supabase integration, mapper funkce
│   │   ├── SymptomRepositoryImpl.kt
│   │   ├── EnvironmentRepositoryImpl.kt
│   │   └── PremiumRepositoryImpl.kt   ← MVP: vždy FREE
│   ├── permissions/
│   │   └── PermissionsHelper.kt       ← GPS, kamera, notifikace, mikrofon
│   └── worker/
│       ├── EnvironmentWorker.kt       ← HiltWorker, GPS + AQI každých 30 min
│       ├── ReminderWorker.kt          ← HiltWorker, večerní připomínka ~20:00
│       └── WorkManagerScheduler.kt   ← scheduleReminder() + cancelReminder()
```

---

## KLÍČOVÉ ARCHITEKTONICKÉ PRINCIPY

### 1. Food Log — celý flow zadávání jídla

Vstupní bod je `FoodLogScreen`, sdílený ViewModel je `FoodLogViewModel`. Všechny pod-obrazovky food flow používají **stejnou instanci** `FoodLogViewModel` (Hilt scope = NavBackStackEntry).

#### Navigation routes (Screen.kt):
```
camera/{mode}                        ← mode = "photo" | "barcode"
food_confirm/{foodName}/{allergens}
food_manual_search?query={query}     ← query je volitelný; Uri.encode() před navigate()
food_manual_entry/{foodName}
avatar_picker                        ← výběr avatara profilu
```

Parametry `foodName` a `allergens` jsou **vždy URL-enkódované** pomocí `Uri.encode()` před navigací.
`allergens` je `|`-separovaný řetězec alergenu klíčů (např. `"Milk|Gluten|Eggs"`).

#### Cesty jak uživatel může zadat jídlo:

**A) Foto (Premium)**
`FoodLogScreen` → `CameraScreen(mode=photo)` → ML Kit image labeling → `SearchFoodUseCase` → `FoodConfirmScreen`

**B) Čárový kód**
`FoodLogScreen` → `CameraScreen(mode=barcode)` → ML Kit BarcodeScanning → `getFoodByBarcode()` → `FoodConfirmScreen`

**C) Hlas**
`FoodLogScreen` → `SpeechRecognizer` dialog → rozpoznaný text → navigace na `FoodManualSearchScreen?query={encodedText}` → `initialQuery` předán přes nav argument → `viewModel.onSearchQueryChange(initialQuery)` spustí debounced vyhledávání

**D) Vyhledávání (Supabase)**
`FoodLogScreen` (tap na search card) → `FoodManualSearchScreen` → debounced search (400ms, min 2 znaky) → `SearchFoodUseCase` → Supabase RPC → výsledky → `FoodConfirmScreen`

**E) Ruční zadání (nová obrazovka)**
`FoodManualSearchScreen` (tlačítko "Přidat ručně") → `FoodManualEntryScreen` → název + výběr alergenů → `FoodConfirmScreen`

#### Recently Logged Pills (FoodLogScreen):
- Zobrazí posledních 10 záznamů z `foodRepository.getAllLogs().map { it.take(10) }`
- Červené = jídlo s alergeny, šedé = bez alergenů
- Klikatelné → navigace přímo na `FoodConfirmScreen` s předvyplněnými daty

#### FoodManualSearchScreen — důležité detaily:
- Přijímá `initialQuery: String = ""` z nav argumentu; pokud neprázdný, volá `viewModel.onSearchQueryChange(initialQuery)` v `LaunchedEffect(Unit)`
- Info banner vysvětluje zdroj: "Databáze produktů s čárovým kódem"
- Tlačítko "Zadat ručně" je **vždy viditelné** (ne jen při prázdných výsledcích)
- Styl tlačítka: **Primary** = žádné výsledky nebo prázdné pole; **Secondary (Outlined)** = výsledky nalezeny

#### FoodManualEntryScreen — detaily:
- Editovatelné textové pole pro název jídla (předvyplněno z `foodName` argumentu)
- `FlowRow` s `FilterChip` pro všech 14 EU povinných alergenů (multi-select)
- Tlačítko "Navrhnout alergeny" → spustí `viewModel.suggestAllergens(name)`
  - **Krok A (okamžitý)**: `FoodAllergenHelper.suggestFromName()` — lokální heuristika přes ~80 CZ/EN klíčových slov
  - **Krok B (síť)**: `SearchFoodUseCase(name)` → alergeny z prvního výsledku Supabase sloučeny s lokálními
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

### 2. Supabase Food API — detaily implementace

Aplikace používá vlastní Supabase backend, který zrcadlí data z Open Food Facts.

**Base URL:** `BuildConfig.SUPABASE_URL`

**Search endpoint (RPC):**
```
GET rest/v1/rpc/search_products?query={query}&result_limit=20
```

**Barcode endpoint (REST):**
```
GET rest/v1/food_products?code=eq.{barcode}&select={PRODUCT_SELECT}&limit=1
```

**Autentizace:**
- Hlavičky `apikey` a `Authorization: Bearer ...` obsahující anon klíč.
- Nastaveno v `NetworkModule` přes OkHttp interceptor.

**Lokalizace výsledků:**
- Databáze obsahuje `product_name` a `product_name_cs`.
- Mapper v `FoodDto.kt` preferuje českou variantu.
- Alergeny jsou mapovány z `allergens_tags` (očištěny od prefixu "en:").

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
`PremiumHelper.currentVisibilityMode` (val) zpřístupňuje aktuální `visibilityMode` pro DebugMenuViewModel.

**`DebugPremiumOverride` — 3 položky (redundantní FORCE_FREE a FORCE_PREMIUM odstraněny):**

| Hodnota | mode | status | Výsledek v UI |
|---|---|---|---|
| `USE_APP_STATE` | dle `visibilityMode` | skutečný | produkční chování |
| `FORCE_VISIBLE_FREE` | vždy VISIBLE | FREE | upsell karty, hvězdičky |
| `FORCE_VISIBLE_PREMIUM` | vždy VISIBLE | PREMIUM | vše odemčeno |

Deserializace v DataStore má `catch (IllegalArgumentException)` → fallback na `USE_APP_STATE` (odolné vůči starým hodnotám).
DebugMenuScreen zobrazuje aktuální `visibilityMode` jako info chip (šedý = HIDDEN, zelený = VISIBLE).

**Prémiové funkce** (aktuálně všechny HIDDEN):
- `FOOD_PHOTO_RECOGNITION` — foto rozpoznávání jídla
- `POLLEN_DATA` — pylová data
- `EXTENDED_CHARTS` — grafy 14+ dní (odemkne 14denní i 30denní rozsah)
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

#### Získávání polohy na pozadí (bez background location):
- Aplikace nedeklaruje `ACCESS_BACKGROUND_LOCATION`.
- Pokud `EnvironmentWorker` spuštěný na pozadí vyvolá `SecurityException`, `EnvironmentRepositoryImpl` zachytí chybu a použije souřadnice z posledního uloženého záznamu jídla nebo příznaku z databáze jako fallback pro stažení dat.

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
| `food_search_language` | String | `""` | Kód jazyka vyhledávání; prázdný = auto z `Locale.getDefault()` |
| `user_name` | String | `""` | Zobrazované jméno uživatele; prázdný = nezadáno |
| `avatar_selection` | String | `"person"` | ID presetu (např. `"pets"`) nebo `"gallery:<uri>"` pro obrázek z galerie |
| `dark_mode` | Boolean | `true` | Tmavý/světlý režim; změna se projeví okamžitě — MainActivity sleduje tento flow |
| `notifications_enabled` | Boolean | `false` | Večerní připomínka ~20:00; zapnutí/vypnutí řídí WorkManager job (`ReminderWorker`) |

### 9. ProfileScreen — avatar a editace jména

#### Avatar systém:
- `AvatarPreset(id, icon, labelRes)` + `AVATAR_PRESETS` (12 položek) definovány v `AvatarPickerScreen.kt`
- `AvatarCircle(selection, size, modifier)` — sdílená composable: renderuje preset ikonu nebo `AsyncImage` pro galerii
- Výběr persistován v `UserPreferencesDataStore.avatarSelection`
- Galerie: `ActivityResultContracts.PickVisualMedia` + `takePersistableUriPermission(FLAG_GRANT_READ_URI_PERMISSION)` pro přežití restartu

#### Inline editace jména:
- `isEditingName` stav v ProfileScreen; při aktivaci `FocusRequester.requestFocus()` + `editName = userName`
- Uložení na `ImeAction.Done` nebo klik na Done ikonu → `viewModel.setUserName(editName.trim())`
- Prázdné jméno = zobrazí placeholder v `onSurfaceVariant` barvě

#### 12 presetů avatarů (ID → Material Icon):
`person`, `favorite`, `star`, `pets`, `local_florist`, `fitness_center`,
`directions_run` (AutoMirrored), `spa`, `eco`, `health_and_safety`, `sentiment_satisfied`, `self_improvement`

### 10. Export a PDF reporty

Aplikace umožňuje vygenerovat lékařský report ve formátu PDF pro sdílení s lékařem.

#### Architektura:
- **MedicalReportExporter**: Generuje PDF na A4 Canvas. Obsahuje AI shrnutí, top korelace a podezřelé potraviny.
- **MediaStore**: Reporty se ukládají do veřejné složky `Documents/AllergyTrack` (Android 10+).
- **Sdílení**: Probíhá přes `FileProvider` (`${applicationId}.fileprovider`).

#### Implementace v UI:
- **AnalysisExportHandler**: Sjednocená Composable funkce v `ExportUtils.kt`.
- **Sjednocení**: Handler je umístěn v `AnalysisScreen` i v `MainAppScaffold`, aby export fungoval konzistentně z obou míst.
- **SettingsScreen**: Druhý vstupní bod exportu — `SettingsViewModel.exportData()` spouští stejný pipeline (korelace → AI → PDF → `sharePdfFile()`).
- **Context**: Využívá `AllergyTrackApplication.instance` nebo `applicationContext` pro operace mimo UI (Toast, FileProvider).

### 11. SettingsScreen — architektura a implementované funkce

Všechny položky v SettingsScreen jsou plně implementovány.

#### AppCard pro seznam položek
Pro klikatelné položky bez vnitřního paddingu karty se používá `contentPadding = PaddingValues(0.dp)`.
Samotné položky (`SettingsActionItem`, `PermissionSettingItem`, `SettingsSwitchItem`) nesou
vlastní padding (`horizontal = 12.dp`, `vertical = 16.dp`), čímž ripple efekt pokrývá celou
šířku karty až ke krajům. Karta je ořezána na zaoblené rohy pomocí `clip(shape)`.

#### Tmavý režim
- Persistence: `UserPreferencesDataStore.darkMode` (Boolean, default = `true`)
- `MainActivity`: `runBlocking { darkMode.first() }` pro bezpadový počáteční stav (žádný bílý záblesk),
  pak `collectAsState` v `setContent {}` → okamžitá změna bez restartu aplikace

#### Push notifikace — večerní připomínka
- Kanál: `reminder_channel` (`IMPORTANCE_DEFAULT`) — registrován v `AllergyTrackApplication.onCreate()`
- Worker: `ReminderWorker` (`@HiltWorker`) — odesílá notifikaci s `R.string.notification_reminder_*`
- Scheduling: `WorkManagerScheduler.scheduleReminder()` — počítá delay na nejbližší 20:00 místního času;
  pokud je již po 20:00, naplánuje na 20:00 následujícího dne
- Policy: `ExistingPeriodicWorkPolicy.REPLACE` — opakované zapnutí resetuje timing (zabraňuje driftu)
- Notifikace otevírá `MainActivity` s `FLAG_ACTIVITY_CLEAR_TASK` + `autoCancel = true`
- WorkManager nezaručuje přesný čas — Doze mode může zpozdit o desítky minut

#### Export dat ze Settings
`SettingsViewModel.exportData()` spouští stejný pipeline jako `AnalysisViewModel`:
`correlationEngine.analyze()` → `aiSummaryService.generateSummary()` →
`medicalReportExporter.generatePdfReport()` → `sharePdfFile()`

#### Smazání dat
`SettingsViewModel.deleteAllData()` volá `deleteAll()` na všech třech DAO.
DataStore preference (jméno, avatar, jazyk, dark mode, notifikace) zůstanou zachovány.

#### SettingsSwitchItem — disabled stav a onDisabledClick
`SettingsSwitchItem` má parametr `enabled: Boolean` a `onDisabledClick: (() -> Unit)?`.
Celý Row je klikatelný (`indication = null` — bez ripple efektu); `Switch` má `onCheckedChange = null`
a slouží pouze jako vizuální indikátor — klik zpracovává Row.
Pokud `enabled = false` a uživatel klikne, zavolá se `onDisabledClick` (např. zobrazí snackbar).

#### AppSnackbarHost — sdílená snackbar komponenta
`AppSnackbarHost` v `CommonUI.kt` je jediný způsob jak zobrazovat snackbary v aplikaci.
Vždy jej použij místo raw `SnackbarHost`.

**Použití:**
```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

Box(modifier = Modifier.fillMaxSize()) {
    // obsah obrazovky
    AppSnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
}

scope.launch { snackbarHostState.showSnackbar(message) }
```

**Design:**
| Vlastnost | Hodnota |
|-----------|---------|
| Tvar | `RoundedCornerShape(14.dp)` — shodné s `AppCard` |
| Pozadí (dark) | `secondaryContainer = Forest600 #3D6E4F` |
| Text (dark) | `onSecondaryContainer = Forest100 #EAF4ED` |
| Pozadí (light) | `secondaryContainer = Forest200 #C2DECA` |
| Text (light) | `onSecondaryContainer = Forest700 #2D5A3D` |
| Akce | `secondary` (Sage400 / Forest400) |
| Padding od okrajů | `horizontal 16 dp, vertical 12 dp` |

### 12. Volba data a času (DateTimePickerRow)
- Umožňuje uživateli změnit datum a čas při potvrzení jídla (`FoodConfirmScreen`) a zaznamenání příznaku (`SymptomLogScreen`).
- Využívá `DateTimePickerRow` (`ui/components/DateTimePickerRow.kt`), která otevírá standardní dialogy `DatePicker` a custom `TimePicker` lokalizované do češtiny.
- Stavy zvoleného timestampu spravují příslušné ViewModely (`FoodLogViewModel` a `SymptomLogViewModel`) a ukládají je do databáze.

---

## API PŘEHLED

| API | URL | Klíč | Cena | Účel |
|-----|-----|------|------|------|
| Open-Meteo AQI | air-quality-api.open-meteo.com | ❌ | Zdarma (nekomerční) | AQI + pyl |
| Supabase Food REST | `{SUPABASE_URL}/rest/v1/food_products` | ✅ anon | Zdarma (Free Tier 500 MB) | Barcode lookup |
| Supabase Food RPC | `{SUPABASE_URL}/rest/v1/rpc/search_products` | ✅ anon | Zdarma (Free Tier) | Full-text vyhledávání |

**Klíče pro Supabase:**
- `SUPABASE_URL` a `SUPABASE_ANON_KEY` → `local.properties` + `BuildConfig`
- Anon klíč je bezpečné zahrnout do APK — přístup omezuje RLS v PostgreSQL
- Service role klíč (pro import) NIKDY do Android projektu

---

## FOOD BACKEND — SUPABASE ARCHITEKTURA

Produktová databáze běží na vlastním Supabase (PostgreSQL) backendu,
který denně importuje data z Open Food Facts.

```
Open Food Facts (openfoodfacts.org)
        ↓  denní JSONL.gz export (~7 GB)
GitHub Actions (cron 3:00 UTC)
        ↓  scripts/import_off.py
        ↓  filtr: EU země (CORE nebo EU region) + 10 sloupců
Supabase PostgreSQL (free tier 500 MB)
        ↓  autogenerované REST API
Android — Retrofit → SupabaseFoodApiService
```

### Tabulka `food_products`

| Sloupec | Typ | Popis |
|---------|-----|-------|
| `code` | TEXT PK | EAN čárový kód |
| `product_name` | TEXT | Název (defaultní/EN) |
| `product_name_cs` | TEXT | Název v češtině |
| `allergens_tags` | TEXT[] | Alergeny, formát `"en:milk"` |
| `ingredients_text` | TEXT | Ingredience (defaultní) |
| `ingredients_text_cs` | TEXT | Ingredience česky |
| `brands` | TEXT | Výrobce/značka |
| `categories_tags` | TEXT[] | Kategorie produktu |
| `countries_tags` | TEXT[] | Země prodeje |
| `nutriscore_grade` | TEXT | Nutri-Score (a–e) |
| `nova_group` | INTEGER | NOVA skupina (1–4) |
| `image_url` | TEXT | URL thumbnailového obrázku |
| `last_modified_t` | BIGINT | Unix timestamp z OFF |
| `updated_at` | TIMESTAMPTZ | Čas posledního upsert |

### Filtr zemí (import skript)

Konfigurovatelné přes `--region` parametr v `scripts/import_off.py`:

| Region | Země | Velikost DB | Použití |
|--------|------|-------------|---------|
| `core` | CZ, SK, DE, AT, PL, HU | 80–150 MB | **Výchozí — free tier** |
| `eu` | celá EU + CH, UK | 250–400 MB | Free tier (těsně) |
| `all` | celý svět | 500 MB+ | Supabase Pro nutný |

Aktivní region se mění manuálně v GitHub Actions UI nebo změnou `DEFAULT_REGION` v skriptu.

### Android integrace

**`SupabaseFoodApiService`** — Retrofit interface:
- `getProductByBarcode(barcodeFilter = "eq.$barcode")` → barcode lookup
- `searchProducts(query, limit)` → volá PostgreSQL RPC `search_products()`

**Lokalizace v `FoodRepositoryImpl`:**
- `resolvedLocale()` čte `foodSearchLanguage` z `UserPreferencesDataStore`
- Jazyk se aplikuje v mapper funkcích (`localizedName()`, `localizedIngredients()`)
  výběrem správného sloupce (`product_name_cs` vs `product_name`)
- Supabase vždy vrátí oba jazykové sloupce — výběr probíhá na straně klienta

**Přidání nového sloupce z OFF:**
```
1. Supabase SQL Editor: ALTER TABLE food_products ADD COLUMN xxx TYPE;
2. scripts/import_off.py: přidej do FIELDS_TO_IMPORT a extract_fields()
3. FoodDto.kt: přidej pole do SupabaseFoodProduct
4. SupabaseFoodApiService.kt: přidej do PRODUCT_SELECT konstanty
→ Žádný redeploy, žádný downtime
```

### GitHub Actions workflow

Soubor: `.github/workflows/off_import.yml`
- Denní cron: `0 3 * * *` (3:00 UTC)
- Manuální spuštění: `workflow_dispatch` s volbou `full_import` a `region`
- Secrets: `SUPABASE_URL` + `SUPABASE_SERVICE_KEY` (service role — pouze pro import)
- Python skript: `scripts/import_off.py` + `scripts/requirements.txt`

**Monitoring:**
```sql
-- Stav posledních importů (Supabase SQL Editor):
SELECT import_type, started_at, products_upserted, status
FROM import_log ORDER BY started_at DESC LIMIT 5;

-- Aktuální velikost DB:
SELECT pg_size_pretty(pg_database_size(current_database()));
```

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
✅ Snackbary vždy přes AppSnackbarHost (CommonUI.kt) — nikdy raw SnackbarHost
✅ Ikony v UI vždy jako samostatné Icon() composable — nikdy emoji ani Unicode znaky vložené přímo do String resource nebo Text() tam, kde slouží jako ikona. Vizuální ikona a text musí být vždy oddělené composable elementy.

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
| 11 | Supabase backend: vlastní PostgreSQL, denní OFF import, Android integrace | ✅ |
| 12 | PDF Export: MedicalReportExporter, MediaStore, AnalysisExportHandler | ✅ |
| 13 | Příznak DIGESTIVE (Zažívání 🤢) přidán do SymptomType + OnboardingModels + strings | ✅ |
| 14 | Voice input fix: FoodManualSearch přijímá initialQuery přes nav argument | ✅ |
| 15 | ChartsScreen: oprava 6 bugů (mrtvý DAO, filter stats, AQI normalizace, X-osa) + crash fix (Compose Rules) | ✅ |
| 16 | ProfileScreen: avatar (12 presetů + galerie) + inline editace jména + AvatarPickerScreen | ✅ |
| 17 | ProfileScreen: restyling na AppCard + SettingsActionItem vzor (sjednocení se SettingsScreen) | ✅ |
| 18 | AppCard: parametr `contentPadding` — ripple edge-to-edge pro položky v seznamu | ✅ |
| 19 | SettingsScreen: Export dat — PDF pipeline, `sharePdfFile()`, progress sublabel | ✅ |
| 20 | SettingsScreen: Smazání dat — AlertDialog, `deleteAll()` na všech třech DAO | ✅ |
| 21 | SettingsScreen: Tmavý režim — reaktivní, perzistovaný, okamžitá změna v MainActivity | ✅ |
| 22 | SettingsScreen: Push notifikace — ReminderWorker, kanál, scheduling na ~20:00 | ✅ |
| 23 | SettingsScreen: notifications switch disabled při chybějícím oprávnění + snackbar hint | ✅ |
| 24 | AppSnackbarHost: sdílená komponenta s green brand barvami, zaoblené rohy, brand barvy | ✅ |
| 25 | ChartsScreen: premium UX — free uživatel vidí PremiumUpsellCard místo TimeRangeSelector; pořadí grafů: Pyl → AQI → Intenzita; SymptomFilterRow přesunuta nad grafy (uvnitř chartData bloku) | ✅ |
| 26 | PremiumUpsellCard: redesign — ikona + nadpis v Row, pozadí warningColor 8% alpha, tlačítko v barvě okraje (tertiary/onTertiary) | ✅ |
| 27 | DebugPremiumOverride: odstraněny redundantní FORCE_FREE a FORCE_PREMIUM; info chip s aktuálním visibilityMode v DebugMenuScreen; popisky upřesněny | ✅ |
| 28 | Firebase + Crashlytics: integrace Firebase Analytics a Crashlytics (BOM 34.13.0); DebugMenu rozšířen o testovací tlačítka fatal crash a non-fatal exception | ✅ |
| 29 | SymptomDetailScreen: nová obrazovka s přehledem statistik symptomů (count, průměrná/max intenzita, poslední výskyt); SymptomDetailActivity jako samostatná Activity; výběr časového rozsahu respektuje EXTENDED_CHARTS premium | ✅ |
| 30 | AiBadge + AiSummaryService: oprava výpisu klinického vhledu — bold markdown (**text**) v AiSummaryService i AiBadge composable renderován správně | ✅ |
| 31 | Volba data a času: DateTimePickerRow pro vlastní datum/čas u logování jídla a příznaků | ✅ |
| 32 | Bezpečnost: odstraněno ACCESS_BACKGROUND_LOCATION oprávnění, implementován fallback na poslední GPS souřadnice při chybě na pozadí | ✅ |

---

## DOPORUČENÉ DALŠÍ KROKY

**Kvalita a stabilita:**
- Unit testy pro `CorrelationEngine` (kritická logika)
- Unit testy pro `FoodAllergenHelper.suggestFromName()` (pokrytí CZ/EN klíčových slov)
- Unit testy pro `PremiumHelper` (oba módy + debug override)
- Error handling v `EnvironmentWorker` (retry strategie)

**UX vylepšení:**
- Animace při přidání záznamu na Dashboard (timeline item appear)
- Snackbar feedback po uložení jídla/symptomu — použít `AppSnackbarHost` z `CommonUI.kt`
- Pull-to-refresh na Dashboardu
- Empty state pro Charts a Analysis (první týden bez dat)

**Funkce:**
- Nastavení intervalu WorkManager (30 min default, uživatel může změnit)
- Vlastní čas připomínky (aktuálně fixní 20:00)

**Technický dluh:**
- `AiSummaryService.generateSummary()` - integrován on-device Gemini Nano přes ML Kit GenAI Prompt API s lokálním šablonovým fallbackem
- `recognizeFoodFromImage()` je placeholder → napojit na FatSecret nebo Passio API
- `FoodAllergenHelper.suggestFromName()` — varianta C (Claude API) pro přesnější návrhy alergenů (naplánováno po spuštění monetizace)
- `CameraScreen.kt` — navigace na `FoodConfirmScreen` bez `Uri.encode()` (drobná chyba pro produkty s mezerami v názvu)
- `ChartsScreen.kt` — `rememberXAxisFormatter` je lokální helper; při přidání nových grafů dodržet vzor (unconditional `remember` před jakýmkoliv podmíněným returnem)

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

### Lokální AI asistent (Gemini Nano):
- Knihovna: `com.google.mlkit:genai-prompt` (ML Kit GenAI Prompt API)
- Služba: Google AICore (Android 14+, API 26+ min SDK podpora)
- Třída: `AiSummaryService`
- Metoda: `suspend fun generateSummary(correlations: List<Correlation>): String` (musí běžet v coroutine/suspend kontextu)
- Správa stavu: Ověřováno přes `checkStatus()` z `FeatureStatus` (`com.google.mlkit.genai.common.FeatureStatus`)
- Fallback: Při chybě či nedostupnosti (`status != AVAILABLE`) se vrací statické rule-based textové shrnutí (100% offline bezpečnost).

### Deaktivace Firebase v debug sestavení a testování:
- Default: Analytics a Crashlytics jsou v debug verzi vypnuté přes `app/src/debug/AndroidManifest.xml`.
- Toggles: Lze je zapnout trvale v Debug menu (ukládá se do `DebugPreferences`).
- Adb příkaz pro testování Analytics (DebugView): `adb shell setprop debug.firebase.analytics.app com.allergytrack.app` (vypnutí přes `.none.`).

### AnalyticsManager:
- Třída: `AnalyticsManager` (`com.allergytrack.app.data.analytics.AnalyticsManager`), spravovaná Hilt (@Singleton).
- Inicializace: Lazy delegace inicializovaná asynchronně na pozadí (`Dispatchers.Default`) v `AllergyTrackApplication.onCreate()`.
- Metody: Interní `private fun logEvent(eventName: String, params: Bundle? = null)` a typově bezpečné veřejné metody (`logScreenView`, `logFoodLogged`, `logSymptomLogged` atd.).