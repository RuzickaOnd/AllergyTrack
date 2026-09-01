# Příprava AllergyTrack na Kotlin Multiplatform — návrh

Datum: 2026-09-01
Stav: návrh ke schválení
Větev: `feature/kmp-migration`

---

## 1. Cíl a rozsah

Rozdělit projekt na KMP modul `:shared` a Android aplikaci `:app` tak, aby
`domain`, `data` a `presentation` vrstva neobsahovaly žádnou vazbu na Android.

**Android zůstává jediná spustitelná platforma.** iOS targety se přidávají pouze
jako *canary* — nikdy se nespouštějí, jen musí zkompilovat. Kompilátor tím tvrdě
hlídá, že do `commonMain` nepropadne nic androidního.

### V rozsahu

- Nový KMP modul `:shared` s targety `androidTarget()`, `iosArm64`, `iosX64`, `iosSimulatorArm64`
- Přesun `domain/` a `data/` (kromě platformních adaptérů) do `commonMain`
- Přesun 12 z 13 ViewModelů do `commonMain` (`DebugMenuViewModel` zůstává v `:app`)
- Přesun 423 string resources do Compose Resources
- Výměna Hilt → Koin, Retrofit → Ktor, Room → Room KMP, DataStore → DataStore KMP

### Mimo rozsah

- Jakékoli iOS UI. `iosMain` obsahuje pouze `actual` stuby s `TODO()`.
- Compose Multiplatform pro UI. Compose kód zůstává v `:app` na androidx Compose.
- Náhrada Vico (grafy) a Coil (obrázky). Jsou to UI knihovny, řeší se až v iOS fázi.
- Změny chování aplikace. Po migraci se appka chová identicky jako dnes.

### Kritérium hotovo

1. `./gradlew :shared:compileKotlinIosArm64` je zelené
2. `./gradlew :app:assembleRelease` je zelené
3. Ruční smoke průchod (viz §7) projde na fyzickém zařízení
4. Existující databáze uživatele se otevře a data jsou neporušená
5. `:shared/src/commonMain` neobsahuje jediný import `android.*`, `androidx.*` (mimo `androidx.lifecycle`, `androidx.room`, `androidx.datastore`), `javax.*` ani `java.*`

---

## 2. Výchozí stav

| Vrstva | LOC | Podíl | Vazba na Android |
|---|---|---|---|
| `ui/` | 12 472 | 77 % | Compose Material3, 441× `R.string`, 425× `stringResource`, 84× `Activity`, Navigation Compose, 38× `hiltViewModel()` |
| `data/` | 2 136 | 13 % | Room, DataStore, Retrofit/OkHttp, WorkManager, ML Kit, Firebase, Play Services, RevenueCat |
| `domain/` | 1 222 | 8 % | Modely 0 android importů; výjimka `MedicalReportExporter` (Android Canvas/PdfDocument) |
| `di/` | 172 | 1 % | Hilt |

Celkem 16 216 LOC ve 119 souborech. 423 string resources ve 2 jazycích.

**Zjištění, která tvarují návrh:**

- ViewModely jsou čisté. Z 13 souborů (1 991 LOC) mají jen 4 android import mimo `lifecycle`:
  `@StringRes` (2×), `android.net.Uri` (1×), `Context`/`Build` (1×).
- `javax.inject.Inject` je ve 13 souborech `domain/` a `data/`. Do `commonMain` nejde.
  **To určuje pořadí fází — DI se musí vyměnit dřív, než se cokoli přesune.**
- `java.util.UUID` v 6 souborech, `java.util.Calendar` ve 3, `TimeUnit` ve 2.
- `BuildConfig.*` na 22 místech ve 12 souborech.
- Projekt nemá testy (jen 2 vygenerované šablony) a je živý na Google Play (verze 1.0.3).

---

## 3. Cílová struktura

```
AllergyTrack/
├── settings.gradle.kts                    include(":shared", ":app")
├── gradle/libs.versions.toml
│
├── shared/                                ← KMP modul
│   ├── build.gradle.kts                   androidTarget() + iosArm64/iosX64/iosSimulatorArm64
│   └── src/
│       ├── commonMain/kotlin/com/allergytrack/shared/
│       │   ├── domain/
│       │   │   ├── model/                 9 souborů, 1:1 přesun
│       │   │   ├── repository/            5 interfaces, 1:1 přesun
│       │   │   ├── usecase/               4 use case
│       │   │   ├── analysis/              CorrelationEngine
│       │   │   ├── premium/               PremiumHelper
│       │   │   └── export/                MedicalReportExporter — pouze interface
│       │   ├── data/
│       │   │   ├── local/database/        Room KMP: entity, DAO, AllergyDatabase, MIGRATION_1_2
│       │   │   ├── local/datastore/       OnboardingDataStore, UserPreferencesDataStore, DebugPreferences
│       │   │   ├── remote/                Ktor: SupabaseFoodApi, AirQualityApi + DTO
│       │   │   └── repository/            Food/Symptom/Environment impl
│       │   ├── presentation/              12 ViewModelů
│       │   ├── platform/                  expect deklarace
│       │   ├── config/                    AppConfig
│       │   └── di/                        Koin moduly
│       ├── commonMain/composeResources/
│       │   ├── values/strings.xml         423 stringů + plurals
│       │   └── values-cs/strings.xml
│       ├── commonTest/                    charakterizační testy
│       ├── androidMain/kotlin/…/platform/ actual implementace pro Android
│       └── iosMain/kotlin/…/platform/     actual stuby s TODO()
│
└── app/                                   ← Android aplikace
    └── src/main/
        ├── kotlin/com/allergytrack/app/
        │   ├── MainActivity.kt
        │   ├── AllergyTrackApplication.kt
        │   ├── ui/                        12 472 LOC Compose — beze změny umístění
        │   ├── platform/                  platformní adaptéry (viz §3.1)
        │   └── di/                        Koin Android modul
        └── res/                           drawables, themes, ikony (strings odejdou)
```

### 3.1 Co zůstává v `:app`

Do `:shared` jde pouze interface, implementace zůstává na Androidu:

| Komponenta | Důvod |
|---|---|
| `MedicalReportExporter` | Android Canvas + PdfDocument + MediaStore |
| `PremiumRepositoryImpl` | RevenueCat Android SDK |
| `AppUpdateRepositoryImpl` | Play In-App Update |
| `FoodImageRecognizer` | ML Kit Image Labeling |
| `AiSummaryService` | ML Kit GenAI Prompt (Gemini Nano) |
| `RecognizeFoodFromImageUseCase` | závisí na `Context` + `Uri` |
| `LocationProvider` | Play Services Location |
| `EnvironmentWorker`, `ReminderWorker`, `WorkManagerScheduler` | WorkManager |
| `NotificationManager`, `AppFirebaseMessagingService` | Firebase Messaging |
| `AnalyticsManager` | Firebase Analytics/Crashlytics |
| `PermissionsHelper` | Android runtime permissions |
| `DebugMenuViewModel` | čistě Firebase/debug nástroj, pro iOS bez hodnoty |
| celá `ui/` | Compose zůstává androidx |

### 3.2 expect/actual v `platform/`

```kotlin
expect val isDebugBuild: Boolean
expect fun randomUuid(): String
expect fun deviceLocale(): String
expect fun createDataStore(name: String): DataStore<Preferences>
expect fun databaseBuilder(): RoomDatabase.Builder<AllergyDatabase>
expect fun httpClientEngine(): HttpClientEngine
```

`iosMain` implementace jsou stuby: `actual val isDebugBuild: Boolean get() = TODO("iOS fáze")`.
Kompilují se, nikdy se nespouštějí.

---

## 4. Výměna knihoven

| Dnes | Po migraci | Důvod |
|---|---|---|
| Hilt 2.60.1 | **Koin** (+ `koin-androidx-workmanager`) | Hilt je JVM/Android-only |
| Retrofit 3.0.0 + OkHttp 5.5.0 | **Ktor client** (OkHttp engine / Darwin engine) | Retrofit není KMP |
| Room 2.8.4 (Android) | **Room 2.8.4 KMP** | `room-gradle-plugin`, KSP2, `@ConstructedBy` |
| DataStore 1.2.1 | **`datastore-preferences-core`** | KMP varianta téhož artefaktu |
| `java.util.UUID` | `kotlin.uuid.Uuid` | |
| `java.util.Calendar`, `TimeUnit` | **kotlinx-datetime** | |
| `BuildConfig.*` | `AppConfig` data class přes DI | `BuildConfig` je AGP artefakt |
| `R.string` / `androidx.compose.ui.res.stringResource` | **Compose Resources** `Res.string.*` | |
| `@StringRes Int` | `StringResource` | |

**Beze změny:** kotlinx.serialization, coroutines/StateFlow, všechny DTO,
`FoodAllergenHelper`, `ui/theme`, Vico, Coil.

### 4.1 Volba SQLite driveru

Na `androidMain` se použije **`AndroidSQLiteDriver`**, ne `BundledSQLiteDriver`.
Je to tentýž framework SQLite, který aplikace používá dnes, takže se riziko
poškození existujících databází prakticky odstraní. `BundledSQLiteDriver`
se použije až na `iosMain`.

---

## 5. Fáze migrace

Aplikace je funkční a nasaditelná po každé fázi. Každá fáze končí commitem.

### Fáze 0 — Pojistky *(~1 den)*

Projekt nemá testy a je živý na Play. Tohle je největší riziko celé migrace.

- Větev `feature/kmp-migration`
- Charakterizační testy proti **současnému** kódu: `CorrelationEngine`,
  `FoodAllergenHelper.suggestFromName()`, mappery ve `FoodDto`, `ChartDataMapper`.
  Cca 10 testů. Později se přesunou do `commonTest` a musí dávat stejné výsledky.
- Stáhnout zálohu reálné databáze ze zařízení (pro ověření ve fázi 6)
- Zaznamenat baseline smoke průchod (§7)
- Úklid rootu: `temp_classes/`, `temp_classes.jar`, `temp_classes_m3.jar`, `.DS_Store`

**Hotovo když:** testy jsou zelené, záloha DB existuje, smoke průchod zaznamenán.

### Fáze 1 — Kostra `:shared` *(~0,5 dne)*

Prázdný KMP modul s jedním dummy souborem v `commonMain`.
Do `libs.versions.toml` přibude: `kotlin-multiplatform` plugin, Ktor, Koin,
kotlinx-datetime, `sqlite-bundled`, `room-gradle-plugin`.

**Hotovo když:** `:shared:compileKotlinIosArm64` i `:app:assembleDebug` jsou zelené.

### Fáze 2 — Hilt → Koin *(~2–3 dny, nejrizikovější)*

Celé v `:app`, nepřesouvá se ani řádek. Big-bang — nelze rozdělit.

- 38× `hiltViewModel()` → `koinViewModel()`, zrušit `@HiltViewModel`
- 3 Workery: `@HiltWorker` → `koin-androidx-workmanager`
- `@AndroidEntryPoint` na 6 Activitách (včetně `MainActivity`) + `AppFirebaseMessagingService`
- `di/AppModule`, `DatabaseModule`, `NetworkModule` → Koin moduly
- Odstranit `javax.inject.*` z 13 souborů `domain/`/`data/`

**Hotovo když:** celý smoke průchod projde na fyzickém zařízení.
Toto je jediná fáze bez postupného ústupu — otestovat důkladně, než se pokračuje.

### Fáze 3 — `domain/model` + repository interfaces *(~0,5 dne)*

Nejlevnější přesun — 9 modelů a 5 repository interfaces, všechny s 0 android importů.
Jediná změna: `java.util.UUID` → `kotlin.uuid.Uuid` (6 souborů).

**Hotovo když:** iOS target kompiluje, appka běží, testy zelené.

### Fáze 4 — Use cases, `CorrelationEngine`, `PremiumHelper`, `AppConfig` *(~1 den)*

- `TimeUnit` → kotlinx-datetime
- 22 výskytů `BuildConfig.*` → `AppConfig` data class předaná z `:app` přes Koin
- `PremiumHelper`: `BuildConfig.DEBUG` → `isDebugBuild` z `platform/`
- `MedicalReportExporter` se rozdělí: interface do `shared`, implementace v `:app`
- Charakterizační testy z fáze 0 → `commonTest`, očekávané hodnoty se nemění

**Hotovo když:** `commonTest` běží pro JVM i iOS a dává identické výsledky jako ve fázi 0.

### Fáze 5 — Síť: Retrofit → Ktor *(~1–2 dny)*

`SupabaseFoodApiService` a `AirQualityApiService` na Ktor `HttpClient`. DTO se nemění.

CLAUDE.md má pravidlo *„HttpException ze síťových volání propagovat do ViewModelu"*.
`retrofit2.HttpException` po výměně neexistuje. Všechny `catch` bloky ve ViewModelech
se musí projít **ručně**, ne jen zkompilovat — jinak se tiše rozbije ošetření
HTTP 503 ve `FoodManualSearchScreen`.

**Hotovo když:** vyhledávání jídla, lookup čárového kódu i stažení AQI fungují;
ošetření chyby 503 ověřeno (např. dočasnou změnou URL na neplatnou).

### Fáze 6 — Úložiště: Room KMP + DataStore KMP *(~2–3 dny, druhé nejrizikovější)*

- Room: `room-gradle-plugin`, KSP2, `@ConstructedBy` + `expect object AllergyDatabaseConstructor`
- `AndroidSQLiteDriver` na `androidMain` (viz §4.1)
- Ověřit `MIGRATION_1_2` proti záloze DB z fáze 0
- Zkontrolovat, že žádná DAO metoda není blokující (Room KMP vyžaduje `suspend` nebo `Flow`)
- DataStore: 3 soubory, `expect fun dataStorePath(name)`
- `FoodRepositoryImpl`, `SymptomRepositoryImpl`, `EnvironmentRepositoryImpl` → `commonMain`
  (`java.util.Calendar` → kotlinx-datetime)

**Hotovo když:** aplikace nainstalovaná přes existující build otevře zálohovanou DB
a všechna data jsou viditelná v Dashboardu i Grafech.

### Fáze 7 — Stringy → Compose Resources *(~1–2 dny)*

- 423 stringů × 2 jazyky + plurals do `shared/src/commonMain/composeResources/`
- 425 volání v UI: `androidx.compose.ui.res.stringResource` →
  `org.jetbrains.compose.resources.stringResource`, `R.string.x` → `Res.string.x`.
  Většina mechanická, `StringResExt.kt` (123 LOC) ručně.

**Hotovo když:** přepínání jazyka přes `AppLocaleHelper` funguje pro češtinu i angličtinu,
včetně plurálů.

### Fáze 8 — ViewModely → `commonMain` *(~1 den)*

12 ViewModelů (`DebugMenuViewModel` zůstává v `:app`). Devět jde 1:1. Čtyři se čistí:

| ViewModel | Změna |
|---|---|
| `ChartsViewModel` | `@StringRes Int` → `StringResource` |
| `DashboardViewModel` | `@StringRes Int` → `StringResource` |
| `FoodLogViewModel` | `android.net.Uri` → `String` |
| `SettingsViewModel` | `Context`/`Build` → interface `DeviceInfo` |

**Hotovo když:** `commonMain` neobsahuje žádný android import (viz kritérium §1.5).

### Fáze 9 — Úklid a dokumentace *(~0,5 dne)*

- `iosMain` stuby opatřit komentářem, co je čeká
- Aktualizovat `CLAUDE.md` a `AGENTS.md` — obsahují starou strukturu a jsou to
  hlavní kontextové dokumenty projektu
- Sekce v README: co je hotové, co čeká na iOS fázi (Vico, Coil, ML Kit, PDF export,
  WorkManager, Firebase, RevenueCat, Play Update)

**Celková odhadovaná pracnost: 10–15 dní čisté práce.**

---

## 6. Rizika

| # | Riziko | Dopad | Ošetření |
|---|---|---|---|
| 1 | Žádné testy | Regrese se projeví až u uživatelů | Fáze 0 pokrývá logiku; UI odhalí jen ruční smoke průchod po každé fázi |
| 2 | Ztráta dat uživatelů při Room KMP | Nevratný, aplikace je živá na Play | `AndroidSQLiteDriver` na Androidu + ověření proti reálné záloze DB |
| 3 | Hilt → Koin je big-bang | Jediná fáze bez postupného ústupu | Provést jako fázi 2, v izolaci, na plně funkčním kódu |
| 4 | Ktor mění tvar výjimek | Tiché rozbití ošetření HTTP 503 | Ruční průchod všech `catch` bloků, ne jen kompilace |
| 5 | Compose Resources vedle androidx Compose | Build friction | Fáze 7 je samostatná a odložitelná; fallback = stringy zůstanou v `res/` a VM vrací sealed klíče |
| 6 | Vico 3.3.1 a Coil 2.7 nejsou KMP | Blokuje iOS fázi, ne tuto | Vědomě odloženo, zdokumentováno ve fázi 9 |

---

## 7. Smoke průchod

Spouští se po každé fázi na fyzickém zařízení:

1. Onboarding (nová instalace) — 4 kroky až na Dashboard
2. Záznam jídla vyhledáváním v Supabase → potvrzení → uložení
3. Záznam jídla čárovým kódem
4. Záznam jídla ručním zadáním + návrh alergenů
5. Záznam příznaku včetně změny data a času
6. Dashboard — dnešní záznamy se zobrazí
7. Grafy — přepnutí časového rozsahu a filtru příznaků
8. Analýza — korelace + AI shrnutí
9. Export PDF a sdílení
10. Nastavení — tmavý režim, notifikace, přepnutí jazyka
11. Profil — změna avataru a jména

---

## 8. Poznámka k návratnosti

Po dokončení se aplikace chová přesně jako dnes, jen se složitějším buildem.
Přínos migrace se realizuje až s iOS aplikací. Pokud iOS není v horizontu
zhruba roku, je tato práce investice na skladě — což je legitimní volba,
ale je dobré ji učinit vědomě.
