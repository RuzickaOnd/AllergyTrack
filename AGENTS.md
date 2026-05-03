# AllergyTrack — AGENTS.md
# Závazné instrukce pro Gemini agenta v Android Studiu

> Tento soubor definuje pravidla chování agenta pro veškerou práci na projektu AllergyTrack.
> Před každou akcí si tato pravidla projdi. Při konfliktu s jiným promptem mají tato pravidla přednost.

---

## 1. IDENTITA PROJEKTU

```
Název aplikace:     AllergyTrack
Package name:       com.allergytrack.app  ← NIKDY neměnit
Min SDK:            26 (Android 8.0)      ← NIKDY nesnižovat
Target SDK:         35
Jazyk:              Kotlin (100%)         ← žádný Java kód
UI jazyk:           Čeština               ← veškerý text v UI česky
```

---

## 2. TECHNICKÝ STACK — ZÁVAZNÝ

Níže uvedený stack je fixní. Agent **nesmí** nahrazovat, přidávat ani odstraňovat
závislosti bez explicitního pokynu uživatele.

### UI
| Technologie | Verze | Poznámka |
|-------------|-------|----------|
| Jetpack Compose | BOM 2024.12.01 | Výhradní UI framework — žádný XML layout |
| Material 3 | přes BOM | Designový systém |
| Navigation Compose | 2.8.5 | |
| Coil | 2.7.0 | Načítání obrázků |

### Architektura
| Vrstva | Technologie | Pravidlo |
|--------|-------------|----------|
| UI | Compose + ViewModel | Žádná byznys logika v Composable |
| State | StateFlow + UiState sealed class | Žádný LiveData |
| Domain | Use Cases | Každá akce = samostatný UseCase |
| Data | Repository pattern | UI nikdy nevolá DAO přímo |
| DI | Hilt 2.53.1 | Žádný jiný DI framework |

### Persistence & Síť
| Technologie | Účel |
|-------------|------|
| Room 2.6.1 | Lokální databáze — KSP, ne KAPT |
| DataStore Preferences | Uživatelská nastavení |
| Retrofit 2.11.0 | HTTP klient |
| OkHttp 4.12.0 | HTTP vrstva |
| Kotlin Serialization 1.7.3 | JSON parsing — žádný Gson ani Moshi |

### Background & Kamera
| Technologie | Účel |
|-------------|------|
| WorkManager 2.10.0 | Periodické úlohy |
| FusedLocationProviderClient | GPS — bateriově efektivní |
| CameraX 1.4.1 | Kamera |

### Build
| Položka | Hodnota |
|---------|---------|
| Build systém | Gradle Kotlin DSL (.kts) |
| Dependency management | `libs.versions.toml` (Version Catalog) |
| Annotation processing | KSP — žádný KAPT |

---

## 3. ARCHITEKTURA — PRAVIDLA

### Struktura balíčků (fixní)
```
com.allergytrack.app/
├── di/              Hilt moduly
├── ui/
│   ├── theme/
│   ├── navigation/
│   ├── components/  Sdílené Compose komponenty
│   └── screens/     Jedna složka per obrazovka (Screen + ViewModel)
│       └── profile/ ProfileScreen, AvatarPickerScreen (sdílí AVATAR_PRESETS + AvatarCircle), ProfileViewModel
├── domain/
│   ├── model/       Čisté Kotlin datové třídy
│   ├── repository/  Pouze interfaces
│   └── usecase/     Jeden soubor per use case
└── data/
    ├── local/       Room (entity, dao, database)
    ├── remote/      API services + DTO
    ├── repository/  Implementace interfaces z domain/
    └── worker/      WorkManager workers
```

### Povinná pravidla
- **ViewModel** vždy `@HiltViewModel` + `@Inject constructor`
- **Repository interface** v `domain/`, implementace v `data/`
- **DTO ≠ Domain model** — nikdy nevracet DTO do UI, vždy mapovat
- **Room Entity ≠ Domain model** — vždy mapovat přes extension funkce
- **Coroutines** pro async — žádné RxJava, žádné callbacks kde lze použít suspend
- **Flow** pro reaktivní data z Room — žádný blocking přístup k DB
- **Sealed interface** pro UiState (Loading / Success / Error pattern)

### Zakázané praktiky
- ❌ `runBlocking` v produkčním kódu
- ❌ Přímé volání DAO z ViewModelu
- ❌ Byznys logika v Composable funkcích
- ❌ `GlobalScope`
- ❌ Nullable `!!` operátor — vždy `?: return` nebo `?.let`
- ❌ `Thread.sleep()`
- ❌ Hardcoded strings mimo UI texty (API klíče, URLs)

### Premium & Gating
- **PremiumHelper** je JEDINÉ místo kde se rozhoduje o přístupu a viditelnosti.
- **Visibility Mode**: `HIDDEN` (MVP) = vše přístupné bez badge; `VISIBLE` = paywally a hvězdičky aktivní.
- **Debug Overrides**: V `DEBUG` buildu lze vynutit stavy přes `DebugMenu` (DataStore `DebugPreferences`). To umožňuje QA testování paywallů i bez reálných nákupů.
- **ViewModel State**: Vždy rozlišovat `canUseFeature` (pro logiku) a `showUpsell/Badge` (pro UI).
- **Komponenty**:
  - `PremiumGate(showUpsell = ...)` — obaluje celou funkci, při `true` zobrazí `PremiumUpsellCard`.
  - `PremiumBadge()` — malá hvězdička pro inline označení prémiové funkce.
- Všechny prémiové funkce MUSÍ být definovány v `PremiumFeature` enum.
- Veškerý billing kód patří POUZE do `PremiumRepositoryImpl`.

---

## 4. COMPOSE — KONVENCE

```kotlin
// Pojmenování composable funkcí — PascalCase, popisné
✅ DashboardScreen, EnvironmentCard, SymptomTypeChip
❌ Screen1, MyCard, Chip

// State hoisting — state výše, události dolů
✅ @Composable fun SymptomTypeCard(isSelected: Boolean, onClick: () -> Unit)
❌ @Composable fun SymptomTypeCard() { var selected by remember... }

// Preview každé netriviální komponenty
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
@Composable
fun XyzPreview() { AllergyTrackTheme { Xyz(...) } }

// Modifier jako první nepovinný parametr
@Composable fun MyComponent(modifier: Modifier = Modifier, ...)

// Padding a spacing
- ✅ Všechny hlavní obrazovky (Screens) mají horizontální padding nastavený na 16.dp
```

### Zakázané UI praktiky
- ❌ `Column` s `verticalScroll` + `LazyColumn` vnořeně
- ❌ Hardcoded `dp` hodnoty mimo definované konstanty — používej `MaterialTheme.spacing`
- ❌ `android:layout_*` XML atributy (jsme full Compose)
- ❌ `remember { ... }` nebo composable volání **po** podmíněném `return` — porušení Compose Rules of Hooks způsobí slot table corruption a crash při změně stavu. Vždy: nejdřív všechna `remember`, pak podmíněná logika.

---

## 5. SPRÁVA API KLÍČŮ

## Aktuální API přehled

| API | URL | Cena | API klíč |
|-----|-----|------|----------|
| Open-Meteo Air Quality + Pollen | air-quality-api.open-meteo.com | Zdarma | ❌ |
| Supabase Food REST/RPC | `{SUPABASE_URL}/rest/v1/...` | Zdarma (Free Tier) | ✅ anon |

### Supabase klíče

| Klíč | Umístění | Použití |
|------|----------|---------|
| `SUPABASE_URL` | `local.properties` + `BuildConfig` | Base URL pro Retrofit |
| `SUPABASE_ANON_KEY` | `local.properties` + `BuildConfig` | Android — pouze čtení (RLS) |
| `SUPABASE_SERVICE_KEY` | GitHub Actions Secrets **POUZE** | Import skript — plný přístup |

**Pravidla:**
- `SUPABASE_ANON_KEY` smí být v APK — Row Level Security omezuje přístup pouze na čtení `food_products`
- `SUPABASE_SERVICE_KEY` **NIKDY** do Android projektu, **NIKDY** do gitu
- Oba klíče v `local.properties` (je v `.gitignore`)

---

## 6. FOOD BACKEND — ZÁVAZNÁ PRAVIDLA PRO AGENTA

### Architektura

```
Open Food Facts (denní JSONL.gz export)
         ↓
GitHub Actions — scripts/import_off.py
(filtr zemí, 10 sloupců, upsert po 500)
         ↓
Supabase PostgreSQL — tabulka food_products
         ↓  REST API + RPC search_products()
Android — SupabaseFoodApiService (Retrofit)
         ↓
FoodRepositoryImpl → RecognizedFood
```

### Soubory food backendu (NEMĚNIT bez pokynu)

| Soubor | Role |
|--------|------|
| `data/remote/food/SupabaseFoodApiService.kt` | Retrofit interface — barcode + search |
| `data/remote/food/FoodDto.kt` | DTO: `SupabaseFoodProduct`, `SupabaseSearchResult`, `RecognizedFood` + mapper funkce |
| `data/remote/food/FoodAllergenHelper.kt` | EU 14 alergenů + heuristika návrhu |
| `data/repository/FoodRepositoryImpl.kt` | Integrace Supabase + lokalizace |
| `di/NetworkModule.kt` | `@Named("supabase")` Retrofit instance |
| `scripts/import_off.py` | Python import skript (mimo Android projekt) |
| `.github/workflows/off_import.yml` | GitHub Actions cron |

### Lokalizace produktů — jak funguje

Supabase databáze vrátí vždy **oba jazykové sloupce** (`product_name` + `product_name_cs`).
Výběr správného sloupce probíhá v mapper funkcích v `FoodDto.kt`:

```kotlin
// Priorita pro cs: product_name_cs → product_name → fallback
// Priorita pro ostatní: product_name → product_name_cs → fallback
fun SupabaseFoodProduct.localizedName(languageCode: String, fallback: String): String
fun SupabaseFoodProduct.localizedIngredients(languageCode: String): List<String>
```

Jazyk se čte z `UserPreferencesDataStore.foodSearchLanguage` v `resolvedLocale()`.
**Nikdy neposílat jazyk jako HTTP parametr** — Supabase REST API ho neakceptuje.

### Přidání nového pole z Open Food Facts

```
1. Supabase SQL Editor:
   ALTER TABLE food_products ADD COLUMN novy_sloupec TEXT;

2. scripts/import_off.py:
   - Přidej do FIELDS_TO_IMPORT: "novy_sloupec"
   - Přidej do extract_fields(): "novy_sloupec": product.get("novy_sloupec") or None

3. data/remote/food/FoodDto.kt:
   - Přidej do SupabaseFoodProduct: @SerialName("novy_sloupec") val novySloupec: Type? = null

4. data/remote/food/SupabaseFoodApiService.kt:
   - Přidej do PRODUCT_SELECT: "...novy_sloupec"
```

Žádný redeploy backendu, žádná Room migrace, žádný downtime.

---

## 7. DATABÁZE — PRAVIDLA

- Verze DB začíná na `1`, každá změna schématu = `Migration` objekt
- `exportSchema = false` pouze v MVP — v produkci změnit na `true`
- Listy v entitách ukládány jako JSON String (MVP) — TypeConverter přidat ve v2
- Všechny DAO metody vracející seznam → `Flow<List<T>>`
- Jednorázové dotazy → `suspend fun`

```kotlin
// ✅ Správně
@Query("SELECT * FROM food_logs WHERE timestamp >= :from")
fun getLogs(from: Long): Flow<List<FoodLogEntity>>

// ❌ Špatně — blokující volání
@Query("SELECT * FROM food_logs")
fun getLogs(): List<FoodLogEntity>
```

---

## 8. CHOVÁNÍ AGENTA PŘI ZMĚNÁCH

### Před každou změnou agent musí:
1. Identifikovat, které vrstvy jsou dotčeny (UI / Domain / Data)
2. Zkontrolovat, zda změna neporušuje pravidla v sekci 3
3. Při změně DB entity automaticky aktualizovat mapper funkce
4. Při přidání nové obrazovky automaticky přidat route do `Screen.kt` a `NavGraph.kt`

### Při přidání nové závislosti agent musí:
1. Přidat verzi do `[versions]` v `libs.versions.toml`
2. Přidat knihovnu do `[libraries]`
3. Přidat `implementation(libs.xxx)` do `app/build.gradle.kts`
4. Nikdy přidávat závislost přímo jako string do build.gradle — vždy přes Version Catalog

### Při refaktoringu agent musí:
1. Zachovat package name `com.allergytrack.app`
2. Zachovat stávající Room schema (nebo přidat Migration)
3. Zachovat stávající API kontrakty (interfaces v domain/repository)
4. Nerozbít existující navigační routes

---

## 9. KÓDOVÝ STYL

```kotlin
// Kotlin idiomy — preferovat
✅ data class, sealed class/interface, object
✅ Extension funkce pro mapování (Entity.toDomain(), DomainModel.toEntity())
✅ Named arguments pro přehlednost
✅ when expression místo if-else řetězce
✅ apply, let, run, also kde zpřehledňují kód

// Formátování
- Odsazení: 4 mezery
- Max délka řádku: 120 znaků
- Prázdný řádek mezi property a funkcemi ve třídě
- KDoc pro public API (repository interfaces, use cases)
```

---

## 10. TESTOVÁNÍ (připravit na budoucí testy)

Produkční kód musí být testovatelný:
- ViewModely přijímají závislosti přes konstruktor (ne singleton přístup)
- Repository interfaces umožňují mock implementace
- Use cases jsou čisté funkce kde to jde
- Side effecty izolované do dedicated tříd (LocationProvider, WorkManagerScheduler)

Testovací závislosti (přidat až při implementaci testů):
```toml
# Připravit v libs.versions.toml až bude potřeba:
junit5 = "5.11.0"
turbine = "1.2.0"
mockk = "1.13.12"
coroutines-test = "1.9.0"
```

---

## 11. GIT KONVENCE (pro budoucí verzování)

```
Formát commit message:
feat(screen): přidána SymptomLogScreen s animovaným sliderem
fix(worker): opraveno selhání WorkManager bez GPS oprávnění
refactor(repo): extrahována mapper logika do extension funkcí
chore(deps): aktualizace Vico na 2.0.1

Větve:
main          ← stabilní, buildující kód
feature/xxx   ← nová funkce
fix/xxx       ← oprava bugu
```

---

## 12. PRIORITA PRAVIDEL

Při konfliktu platí tato priorita (nejvyšší → nejnižší):

1. **Bezpečnost** — API klíče, oprávnění, GDPR
2. **Tento AGENTS.md** — architektura, stack, konvence
3. **Aktuální prompt fáze** — konkrétní implementační instrukce
4. **Obecné Android best practices** — kde AGENTS.md mlčí

---

## 13. QUICK REFERENCE — ČASTÉ DOTAZY

**Q: Jak přidat novou obrazovku?**
1. Vytvoř `ui/screens/xxx/XxxScreen.kt` + `XxxViewModel.kt`
2. Přidej `object Xxx : Screen("xxx")` do `Screen.kt`
3. Přidej `composable(Screen.Xxx.route) { XxxScreen(...) }` do `NavGraph.kt`
4. Volitelně přidej do `BottomNavBar.kt`

**Q: Jak přidat nové API?**
1. Vytvoř DTO v `data/remote/xxx/XxxDto.kt`
2. Vytvoř service interface `XxxApiService.kt`
3. Přidej Retrofit instanci do `NetworkModule.kt`
4. API klíč do `local.properties` + `BuildConfig`
5. Implementuj v `RepositoryImpl`

**Q: Jak přidat nové pole produktu z Open Food Facts?**
1. `ALTER TABLE food_products ADD COLUMN ...` v Supabase SQL Editoru
2. Přidej do `FIELDS_TO_IMPORT` a `extract_fields()` v `scripts/import_off.py`
3. Přidej do `SupabaseFoodProduct` v `FoodDto.kt`
4. Přidej do `PRODUCT_SELECT` v `SupabaseFoodApiService.kt`
   → Žádný redeploy, žádná Room migrace

**Q: Jak změnit region importu produktů (více/méně zemí)?**
- Manuálně: GitHub Actions → Run workflow → `region = core/eu/all`
- Trvale: změň `DEFAULT_REGION` v `scripts/import_off.py`
- Před rozšířením na větší region: ověř místo v Supabase (free tier = 500 MB)
- Při přechodu na menší region: nejprve `DROP TABLE food_products CASCADE`
  v Supabase SQL Editoru, pak znovu spusť plný import

**Q: Databáze produktů je přeplněná (Supabase read-only)?**
1. Supabase SQL Editor: `DROP TABLE IF EXISTS food_products CASCADE;`
2. Znovu spusť `supabase_schema.sql`
3. GitHub Actions → plný import s `region = core`
   (viz soubor `DB_RESET_GUIDE.md` v repozitáři)

**Q: Jak přidat novou Room entitu?**
1. Vytvoř `XxxEntity.kt` v `data/local/database/entity/`
2. Vytvoř `XxxDao.kt` v `data/local/database/dao/`
3. Přidej entitu do `@Database(entities = [...])` v `AllergyDatabase.kt`
4. **Zvyš verzi databáze** a přidej `Migration`
5. Přidej `@Provides fun provideXxxDao()` do `DatabaseModule.kt`
6. Vytvoř doménový model v `domain/model/`
7. Přidej mapper extension funkce

**Q: Jak přidat Use Case?**
1. Vytvoř `domain/usecase/DoSomethingUseCase.kt`
2. `@Inject constructor` s potřebnými repository
3. `operator fun invoke(...)` jako hlavní metoda
4. Injektuj do ViewModelu

**Q: Co dělat při Room migraci?**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE food_logs ADD COLUMN new_column TEXT DEFAULT ''")
    }
}
// Přidat do Room.databaseBuilder(...).addMigrations(MIGRATION_1_2)
```

**Q: Jak přidat prémiovou funkci?**
1. Přidej položku do `PremiumFeature` v `PremiumConfig.kt`.
2. V `PremiumHelper` ji přidej do `premiumFeatures` setu.
3. Ve ViewModelu vystav `canUse...`, `show...Upsell` a `show...Badge` pomocí `PremiumHelper`.
4. V UI použij `PremiumGate` nebo `PremiumBadge`.

**Q: Jak pracovat s alergickým profilem uživatele?**
1. Profil je v `OnboardingDataStore` (`selectedAllergyTypes`, `selectedSymptomTypes`).
2. V analytice použij `knownAllergies` a `knownSymptoms`.
3. Vždy prioritizuj nálezy, které jsou v profilu označeny jako `isExpected`.

---

## 14. ENVIRONMENTÁLNÍ DATA (Open-Meteo)

### Pravidla dostupnosti
- **AQI (Air Quality Index):** Globálně dostupný po celý rok.
- **Pyl (Pollen):** Dostupný **pouze v Evropě** (viz bounding box v `DataAvailability.kt`) a je sezónní.
- **Důležité:** `null` v modelech/databázi znamená "data nejsou dostupná" (mimo region/sezónu), zatímco `0` znamená "nulová koncentrace".

### Implementační pravidla
- **Data Log:** Pole pro pyly v `EnvironmentLog` musí být nullable.
- **UI:** Pokud jsou data nedostupná (`pollenAvailable == false`), UI musí zobrazit informativní banner/badge místo prázdných grafů nebo chyb (platí pro Dashboard i Grafy).
- **Analýza:** `CorrelationEngine` musí ignorovat záznamy s `pollenAvailable == false` při výpočtu korelací pylů, aby nedocházelo ke zkreslení výsledků nulami tam, kde data jen chyběla.

---

## 15. EXPORT A PDF REPORTY

### Architektura exportu
- **MedicalReportExporter**: Třída v `domain/export/` zodpovědná za generování PDF pomocí `android.graphics.pdf.PdfDocument`.
- **Lokální uložení**: Reporty se ukládají do veřejné složky `Documents/AllergyTrack` pomocí `MediaStore` (Scoped Storage kompatibilní pro Android 10+).
- **Sdílení**: Probíhá přes `FileProvider` (autorita `${applicationId}.fileprovider`). Cesty jsou definovány v `res/xml/file_paths.xml`.

### Pravidla pro UI export
- Veškerá logika pro odchycení vygenerovaného souboru a spuštění sdílení musí být unifikována v **`AnalysisExportHandler(viewModel)`** (v `ui/util/ExportUtils.kt`).
- Tento handler se umisťuje do obrazovek (`AnalysisScreen`) nebo scaffoldů (`MainAppScaffold`), které mají možnost export spustit.
- Pro samotné sdílení souboru z libovolného místa slouží extension funkce `Context.sharePdfFile(file)`.

### Globální Context
- Pro případy, kdy je vyžadován `Application Context` a není k dispozici DI (např. statické helpery), lze použít **`AllergyTrackApplication.instance`**. V rámci Hilt modulů a ViewModelů však vždy preferuj `@ApplicationContext context: Context`.
