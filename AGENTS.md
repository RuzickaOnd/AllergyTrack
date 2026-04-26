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

---

## 5. SPRÁVA API KLÍČŮ

## Aktuální API přehled

| API | URL | Cena | API klíč |
|-----|-----|------|----------|
| Open-Meteo Air Quality + Pollen | air-quality-api.open-meteo.com | Zdarma | ❌ |
| Open Food Facts | world.openfoodfacts.org | Zdarma | ❌ |

> Projekt nevyžaduje žádný API klíč pro základní funkce MVP.
> local.properties je prázdný nebo obsahuje pouze dev poznámky.

---

## 6. DATABÁZE — PRAVIDLA

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

## 7. CHOVÁNÍ AGENTA PŘI ZMĚNÁCH

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

## 8. KÓDOVÝ STYL

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

## 9. TESTOVÁNÍ (připravit na budoucí testy)

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

## 10. GIT KONVENCE (pro budoucí verzování)

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

## 11. PRIORITA PRAVIDEL

Při konfliktu platí tato priorita (nejvyšší → nejnižší):

1. **Bezpečnost** — API klíče, oprávnění, GDPR
2. **Tento AGENTS.md** — architektura, stack, konvence
3. **Aktuální prompt fáze** — konkrétní implementační instrukce
4. **Obecné Android best practices** — kde AGENTS.md mlčí

---

## 12. QUICK REFERENCE — ČASTÉ DOTAZY

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

## 13. ENVIRONMENTÁLNÍ DATA (Open-Meteo)

### Pravidla dostupnosti
- **AQI (Air Quality Index):** Globálně dostupný po celý rok.
- **Pyl (Pollen):** Dostupný **pouze v Evropě** (viz bounding box v `DataAvailability.kt`) a je sezónní.
- **Důležité:** `null` v modelech/databázi znamená "data nejsou dostupná" (mimo region/sezónu), zatímco `0` znamená "nulová koncentrace".

### Implementační pravidla
- **Data Log:** Pole pro pyly v `EnvironmentLog` musí být nullable.
- **UI:** Pokud jsou data nedostupná (`pollenAvailable == false`), UI musí zobrazit informativní banner/badge místo prázdných grafů nebo chyb (platí pro Dashboard i Grafy).
- **Analýza:** `CorrelationEngine` musí ignorovat záznamy s `pollenAvailable == false` při výpočtu korelací pylů, aby nedocházelo ke zkreslení výsledků nulami tam, kde data jen chyběla.
