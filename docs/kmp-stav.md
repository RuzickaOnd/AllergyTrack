# Stav KMP migrace

Dokončeno: 2026-09-01
Větev: `feature/AT_migrace_do_KMP`

Projekt je rozdělen na KMP modul `:shared` a Android aplikaci `:app`.
Android zůstává jediná spustitelná platforma; iOS targety slouží jako
*canary* — nikdy se nespouštějí, jen musí zkompilovat, čímž kompilátor
hlídá, že do `commonMain` nepropadne nic androidního.

## Rozdělení kódu

| Zdrojová sada | Souborů | Řádků |
|---|---:|---:|
| `:shared` commonMain | 70 | 4 319 |
| `:shared` androidMain | 4 | 82 |
| `:shared` iosMain | 4 | 79 |
| `:shared` commonTest | 3 | 394 |
| `:app` | 70 | 12 400 |

## Co je sdílené (`commonMain`)

- Doménové modely, repository interfaces, use cases
- `CorrelationEngine` — korelační analýza
- `FoodAllergenHelper` — heuristika návrhu alergenů
- Room databáze (entity, DAO, `MIGRATION_1_2`)
- DataStore (3 soubory preferencí)
- Ktor klienti — Supabase a Open-Meteo
- 12 ViewModelů + `ChartDataMapper`
- 423 string resources ve 2 jazycích (Compose Resources)

## Co zůstává v `:app`

| Komponenta | Proč | Co bude potřeba pro iOS |
|---|---|---|
| celá `ui/` | androidx Compose | přepsat na Compose Multiplatform |
| Vico grafy | Android-only | najít KMP náhradu nebo vykreslit ručně |
| Coil 2.7 | Android-only | upgrade na Coil 3.x (KMP) |
| ML Kit — barcode | Android-only | Vision framework (`VNBarcodeObservation`) |
| ML Kit — rozpoznání jídla | Android-only | Vision framework nebo Core ML |
| ML Kit GenAI (Gemini Nano) | Android-only | Apple Intelligence nebo vzdálené API |
| CameraX | Android-only | AVFoundation |
| WorkManager | Android-only | BGTaskScheduler |
| Play Services Location | Android-only | CoreLocation |
| Play In-App Update | Android-only | nemá obdobu, vypustit |
| Firebase Analytics/Crashlytics/FCM | má iOS SDK | napojit přes existující rozhraní |
| RevenueCat | má iOS SDK | napojit přes `PremiumRepository` |
| PDF export (Canvas + MediaStore) | Android-only | PDFKit + UIActivityViewController |
| `DebugMenuViewModel` | čistě debug nástroj | nepřenášet |

## Platformní rozhraní

Služby zůstávající v `:app` jsou za rozhraními v `commonMain`, takže
ViewModely na ně mohou sahat bez znalosti platformy:

| Rozhraní | Implementace v `:app` |
|---|---|
| `Analytics` | `AnalyticsManager` |
| `Permissions` + `PermissionsState` | `PermissionsHelper` |
| `BackgroundScheduler` | `WorkManagerScheduler` |
| `AiSummaryGenerator` | `AiSummaryService` |
| `AppNotifications` | `NotificationManager` |
| `DeviceInfo` | `AndroidDeviceInfo` |
| `LocationSource` + `Coordinates` | `LocationProvider` |
| `ImageRecognizer` + `RecognizedLabel` | `FoodImageRecognizer` |
| `ReportExporter` | `MedicalReportExporter` |
| `RecognizeFoodFromImage` | `RecognizeFoodFromImageUseCase` |

Do `platform/Time.kt` patří `expect` funkce nahrazující JVM API:
`currentTimeMillis()`, `startOfToday()`, `currentHourOfDay()`,
`dayAndMonth()`, `randomUuidString()`, `deviceLanguage()`, `deviceCountry()`.

## Omezení, na která se narazilo

**Ktor max. 3.1.x.** Ktor 3.2.0 má v `io.ktor.client.plugins.Messages` pole
pojmenované přes backticky s mezerami (`use streaming syntax`), které D8
odmítne dexovat pro DEX verze pod 040, tedy `minSdk < 35`. Projekt má
`minSdk = 26`.

**iosX64 target neexistuje.** `androidx.sqlite:sqlite-bundled:2.7.0` pro něj
nepublikuje variantu, takže s Room KMP ho nelze sestavit. Zbývají `iosArm64`
a `iosSimulatorArm64`, což na Apple Siliconu pokrývá zařízení i simulátor.

**AGP 9+ vyžaduje `com.android.kotlin.multiplatform.library`.** Kombinace
`com.android.library` + `kotlin.multiplatform` je od AGP 9.0 zakázaná.
DSL je jiné: `kotlin { androidLibrary { } }` a úkol se jmenuje
`assembleAndroidMain`, ne `assembleDebug`.

**Compose Resources vyžadují poziční placeholdery.** `%s` se vypíše doslova,
je nutné `%1$s`. Android bere obojí, takže se to při migraci tiše rozbije
a odhalí to jedině pohled na obrazovku.

**Room zůstává na classpath `:app`.** Osm ViewModelů volalo DAO přímo; po
fázi 8 jsou v `commonMain`, ale `:app` si Room drží kvůli Koin definicím DAO.
Databáze i migrace žijí v `:shared` za `createAllergyDatabase()`.

**26 stringů je duplicitních.** Manifest ani `context.getString()` Compose
Resources neumí, takže klíče pro labely Activit a pro kód mimo Compose
zůstaly i v `app/src/main/res/`. **Při změně textu je nutné upravit obě místa.**

## Zděděné chyby — opraveny

Migrace sama chování nezměnila. Následující chyby byly rozbité už před ní
a opraveny samostatně po jejím dokončení (commity `0c77cd8` a `1c198bd`):

1. **Ošetření HTTP 503 bylo mrtvé.** Ktor má ve výchozím stavu
   `expectSuccess = false`, takže chybové odpovědi vůbec nevyhazoval —
   kód se je pokusil deserializovat a spadl na
   `NoTransformationFoundException`, kde se status kód ztratil.
   Opraveno: `createHttpClient` zapíná `expectSuccess`, `SupabaseFoodApi`
   převádí `ResponseException` na `ApiHttpException` a
   `FoodRepositoryImpl` ji propouští. Offline a timeout dál končí jako
   „žádné výsledky". Pokryto 5 testy s Ktor MockEngine.
2. **`AnalysisReadiness` nesl české texty.** Zrušeny `Locked.message`
   a `Partial.disclaimer`, text skládá UI z `requiredCount`,
   `remainingToFull` a `hasEnvironmentData`.
3. **`AiSummaryService` byl celý česky** (23 literálů). Lokalizován
   včetně promptu pro Gemini — instrukce o jazyce je samostatný klíč
   `ai_prompt_language`, takže model odpovídá jazykem aplikace.
4. **`AirQualityApi.getHourlyHistory()` byl mrtvý kód.** Smazán.
   Zůstal po něm nepoužitý DTO `AirQualityHourly`; jeho odstranění
   je samostatná drobnost.

### Dvě regrese z fáze 7, odhalené až při těchto opravách

Doslovný přesun stringů do Compose Resources přenesl i escapovací
konvence Android resources, které Compose Resources neznají:

- **`%%` se nekolabuje na `%`** — v UI se objevilo „100%% případů".
  Opraveno v 7 stringech; tři z nich (`interpretation_food`,
  `interpretation_environment`, `faq_a_correlation_percent`) byly
  rozbité od okamžiku přesunu. Kopie v `res/` `%%` zachovávají,
  tam ho Android formátování vyžaduje.
- **`\'` se neunescapuje** — v UI se objevilo „don\'t". Opraveno
  v 9 anglických stringech.

**Ponaučení:** Compose Resources escapování Android resources nesdílejí.
Nový string patří psát bez escapů (`'` přímo) a s pozičními parametry
(`%1$s`), procento jako jedno `%`. Kompilátor ani testy to nezachytí —
odhalí to jedině pohled na obrazovku.

## Neověřeno

**Přepínání jazyka na API < 33.** Tam jazyk řídí `createConfigurationContext`
místo `LocaleManager`. Všechny dostupné AVD jsou API 36+. `minSdk` je 26,
takže tuto cestu reálná část uživatelů používá — **doověřit na starším
zařízení, než to půjde do produkce.**

## Jak ověřit, že `commonMain` zůstává čistý

```bash
./gradlew :shared:compileKotlinIosArm64
grep -rn "^import java\.\|^import android\.\|^import javax\." shared/src/commonMain/
```

První příkaz musí projít, druhý nesmí nic vypsat. Z `androidx` smí
v `commonMain` být jen `room`, `datastore`, `sqlite` a `lifecycle` —
to jsou KMP knihovny.

## Testy

29 charakterizačních testů z fáze 0 běží v `commonTest`, tedy na Androidu
i na iOS simulátoru (58 spuštění):

| Třída | Testů | Co hlídá |
|---|---:|---|
| `CorrelationEngineCharacterizationTest` | 10 | práh, skóre, `isExpected`, řazení, readiness |
| `FoodDtoMapperCharacterizationTest` | 12 | lokalizace názvů a ingrediencí, čištění alergenů |
| `FoodAllergenHelperCharacterizationTest` | 7 | heuristika klíčových slov, obsah EU_14 |

V `:app` zůstává `KoinModuleGraphTest`, který ověřuje, že Koin graf jde
rozřešit **a** že typy tahané přes `by inject()` mají definici. Druhá
kontrola vznikla proto, že Koin `verify()` `by inject()` místa nevidí
a aplikace na tom ve fázi 8 dvakrát spadla.

```bash
./gradlew :shared:allTests :app:testDebugUnitTest
```
