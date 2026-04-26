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
│   └── NetworkModule.kt       ← Retrofit instances
├── ui/
│   ├── theme/                 ← Color, Theme, Type
│   ├── navigation/
│   │   ├── Screen.kt          ← Sealed class všech routes
│   │   ├── NavGraph.kt        ← AllergyNavGraph + MainAppScaffold
│   │   └── MainAppScaffold.kt
│   ├── components/
│   │   ├── BottomNavBar.kt
│   │   ├── PremiumGate.kt     ← Třístavový wrapper (hidden/upsell/content)
│   │   ├── PremiumUpsellCard.kt
│   │   ├── PremiumBadge.kt
│   │   └── PermissionsHandler.kt
│   ├── onboarding/
│   │   ├── OnboardingFlow.kt          ← AnimatedContent step machine
│   │   ├── OnboardingViewModel.kt
│   │   ├── OnboardingComponents.kt    ← ProgressDots, BottomBar, SelectChip
│   │   ├── OnboardingWelcomeScreen.kt
│   │   ├── OnboardingProfileScreen.kt
│   │   ├── OnboardingPermissionsScreen.kt
│   │   └── OnboardingReadyScreen.kt
│   └── screens/
│       ├── dashboard/
│       │   ├── DashboardScreen.kt
│       │   └── DashboardViewModel.kt
│       ├── food/
│       │   ├── FoodLogScreen.kt
│       │   ├── FoodLogViewModel.kt
│       │   ├── CameraScreen.kt
│       │   ├── FoodConfirmScreen.kt
│       │   └── FoodManualSearchScreen.kt
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
│           ├── SettingsScreen.kt
│           ├── SettingsViewModel.kt
│           ├── DebugMenuScreen.kt     ← Jen v BuildConfig.DEBUG
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
│   │   │   ├── AllergyDatabase.kt     ← Room DB, version 2 (po migration)
│   │   │   ├── entity/
│   │   │   │   ├── FoodLogEntity.kt
│   │   │   │   ├── SymptomLogEntity.kt
│   │   │   │   └── EnvironmentLogEntity.kt
│   │   │   └── dao/
│   │   │       ├── FoodLogDao.kt
│   │   │       ├── SymptomLogDao.kt
│   │   │       └── EnvironmentLogDao.kt
│   │   └── datastore/
│   │       ├── OnboardingDataStore.kt  ← isCompleted, allergyProfile
│   │       ├── DebugPreferences.kt     ← debug_preferences, DebugPremiumOverride
│   │       └── UserPreferencesDataStore.kt
│   ├── remote/
│   │   ├── airquality/
│   │   │   ├── AirQualityApiService.kt ← Open-Meteo, zdarma, bez klíče
│   │   │   └── AirQualityDto.kt        ← AQI + pyl v jednom requestu
│   │   └── food/
│   │       ├── OpenFoodFactsApiService.kt
│   │       └── FoodDto.kt              ← RecognizedFood, allergens
│   ├── repository/
│   │   ├── FoodRepositoryImpl.kt
│   │   ├── SymptomRepositoryImpl.kt
│   │   ├── EnvironmentRepositoryImpl.kt
│   │   └── PremiumRepositoryImpl.kt    ← MVP: vždy FREE
│   ├── permissions/
│   │   └── PermissionsHelper.kt        ← GPS, kamera, notifikace
│   └── worker/
│       ├── EnvironmentWorker.kt        ← HiltWorker, GPS + AQI každých 30 min
│       └── WorkManagerScheduler.kt
```

---

## KLÍČOVÉ ARCHITEKTONICKÉ PRINCIPY

### 1. Premium systém

```
PremiumHelper.visibilityMode = HIDDEN  ← aktuální stav
```

**HIDDEN** = prémiové funkce fyzicky neexistují v UI (neviditelné, nepřístupné).
**VISIBLE** = prémiové funkce viditelné, FREE uživatel vidí hvězdičku + upsell.

Přepnutí = **jeden řádek** v `PremiumHelper.kt`.

ViewModel vystavuje trojici StateFlow pro každou prémiovou funkci:
```kotlin
val isXxxVisible: StateFlow<Boolean>   // HIDDEN = false vždy
val showXxxUpsell: StateFlow<Boolean>  // HIDDEN = false vždy
val showXxxBadge: StateFlow<Boolean>   // HIDDEN = false vždy
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
V RELEASE buildu: debug kód se vůbec nespustí (`if (!BuildConfig.DEBUG) return realFlow`).

**Prémiové funkce** (aktuálně všechny HIDDEN):
- `FOOD_PHOTO_RECOGNITION` — foto rozpoznávání jídla
- `POLLEN_DATA` — pylová data
- `EXTENDED_CHARTS` — grafy 30+ dní
- `AI_CORRELATION_ANALYSIS` — AI korelační analýza
- `PDF_EXPORT` — export PDF pro lékaře
- `UNLIMITED_HISTORY` — neomezená historie

### 2. Environmentální data

Open-Meteo API — jeden endpoint pro vše, bez API klíče:
```
https://air-quality-api.open-meteo.com/v1/air-quality
  ?current=european_aqi,pm2_5,grass_pollen,birch_pollen,...
```

**Dostupnost:**
- AQI, PM2.5, NO2 → globálně, celoročně
- Pyl → pouze Evropa, sezónně (únor–říjen)

`pollenGrassIndex: Int?` — **null = data nedostupná** (mimo EU nebo mimo sezónu),
**0 = dostupná ale nulová**. Nikdy nezaměňovat!

`EnvironmentLog.region: EnvironmentRegion` — `EUROPE` nebo `GLOBAL`.
`isEurope(lat, lon)` — bounding box: lat 34–72, lon -25–45.

UI zobrazuje `PollenUnavailableBanner` s vysvětlením místo prázdného stavu.

### 3. Onboarding

Jednosměrný flow, stav v `OnboardingDataStore`:
```
Welcome → AllergyProfile → Permissions → Ready → Dashboard
```

`MainActivity` čte `onboardingDataStore.isCompleted.first()` synchronně před
prvním sestavením UI. Po dokončení `AppRoot` přepne bez restartu Activity.

Debug menu obsahuje "Reset onboardingu" pro opakované testování.

### 4. Korelační engine

- Minimálně 5 symptomů pro aktivaci analýzy
- Pearsonova korelace pro environmentální data
- Sliding time window 2–72h pro potravinové korelace
- `CorrelationSeverity`: HIGH (≥70%), MEDIUM (40–70%), LOW (<40%)

### 5. Room DB

Aktuální verze: **2** (po migration z fáze 4c).

`MIGRATION_1_2` přidává: `no2`, `ozone`, `region`, `pollenAvailable` do `environment_logs`.
Pyl sloupce (`pollenGrassIndex` atd.) jsou nullable — SQLite to zvládne zpětně kompatibilně.

---

## API PŘEHLED

| API | URL | Klíč | Cena | Účel |
|-----|-----|------|------|------|
| Open-Meteo AQI | air-quality-api.open-meteo.com | ❌ | Zdarma (nekomerční) | AQI + pyl |
| Open Food Facts | world.openfoodfacts.org | ❌ | Zdarma (ODbL) | Jídlo + alergeny |

**Žádný API klíč není potřeba pro MVP.**
`local.properties` je prázdný nebo obsahuje pouze poznámky.

---

## MONETIZACE A VYDÁNÍ

**Aktuální stav: Cesta 3 — Pure Free**
- Vše zdarma, žádná monetizace
- Open-Meteo free tier (nekomerční = OK)
- Google Play: Personal účet, kategorie **Lifestyle** ← klíčové rozhodnutí
- Kategorie Lifestyle = žádná Health declaration, žádný D-U-N-S číslo

**Budoucí přechod na freemium:**
1. Změnit `visibilityMode = HIDDEN` → `VISIBLE` v PremiumHelper
2. Implementovat Google Play Billing v `PremiumRepositoryImpl`
3. Aktivovat Open-Meteo commercial plan ($29/měsíc)
4. Převést Play účet Personal → Organization (D-U-N-S číslo)

---

## ZÁVAZNÁ PRAVIDLA (z AGENTS.md)

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
✅ Veškerý text UI v češtině

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
| 1 | Projekt, struktura, Dashboard, Room, Hilt | ✅ Hotovo |
| 2 | FoodLog, kamera, Open Food Facts API | ✅ Hotovo |
| 3 | SymptomLog, intenzita, části těla | ✅ Hotovo |
| 4a | EnvironmentWorker, GPS, WorkManager | ✅ Hotovo |
| 4b | Patch: Open-Meteo místo Google Pollen | ✅ Hotovo |
| 4c | Graceful degradation, nullable pyl, regiony | ✅ Hotovo |
| 5 | ChartsScreen, Vico grafy, filtry | ✅ Hotovo |
| 6 | AnalysisScreen, CorrelationEngine, AI shrnutí | ✅ Hotovo |
| 7 | PremiumHelper, HIDDEN/VISIBLE mód, PremiumGate | ✅ Hotovo |
| 8 | ProfileScreen, AboutScreen, SettingsScreen, DebugMenu | ✅ Hotovo |
| 9 | Onboarding (4 kroky), PermissionsHelper, OnboardingDataStore | ✅ Hotovo |

---

## DOPORUČENÉ DALŠÍ KROKY

Níže jsou oblasti kde aplikace potřebuje práci — seřazeno podle priority:

**Kvalita a stabilita:**
- Unit testy pro `CorrelationEngine` (kritická logika)
- Unit testy pro `PremiumHelper` (oba módy + debug override)
- UI testy pro onboarding flow
- Error handling v `EnvironmentWorker` (retry strategie)

**UX vylepšení:**
- Animace při přidání záznamu na Dashboard (timeline item appear)
- Empty state obrazovky pro Charts a Analysis (první týden bez dat)
- Snackbar feedback po uložení jídla/symptomu
- Pull-to-refresh na Dashboardu

**Funkce:**
- Notifikace — večerní připomínka logování (WorkManager + NotificationManager)
- Barcode scanner integrace v CameraScreen (ML Kit)
- Sdílení reportu (placeholder PDF export)
- Nastavení intervalu WorkManager (30 min default, uživatel může změnit)

**Technický dluh:**
- `AiSummaryService.generateSummary()` je lokální šablona — napojit na Claude API
- `recognizeFoodFromImage()` je placeholder — napojit na FatSecret nebo Passio API
- `exportReport()` je simulace — implementovat skutečný PDF

---

## JAK PRACOVAT S TÍMTO DOKUMENTEM

1. Vždy dodržuj pravidla z sekce **ZÁVAZNÁ PRAVIDLA**
2. Před přidáním nové funkce zkontroluj **ADRESÁŘOVOU STRUKTURU** a zařaď správně
3. Prémiové funkce přidávej do `PremiumFeature` enum a `PremiumHelper.premiumFeatures`
4. Nové debug nástroje vždy obaluj `if (BuildConfig.DEBUG)`
5. Environmentální data vždy ošetřuj jako nullable (`pollenGrassIndex: Int?`)
6. Každá změna Room schématu = zvýšení verze + Migration objekt

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

### Nový debug nástroj:
```
1. DebugPreferences.kt → nový PreferencesKey + Flow + setter
2. DebugMenuViewModel.kt → přidat StateFlow + akci
3. DebugMenuScreen.kt → přidat UI prvek obalený if (BuildConfig.DEBUG)
4. Příslušný Helper/ViewModel → číst override s if (!BuildConfig.DEBUG) guard
```
