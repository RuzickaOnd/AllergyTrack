# Baseline před KMP migrací

Zaznamenáno: _(nevyplněno — čeká na zařízení)_
Build: versionName 1.0.3, versionCode 1000302, commit `2a5b46a`
Větev: `feature/AT_migrace_do_KMP`
Záloha DB: `baseline/allergy_track.db` (necommitovaná, viz .gitignore)

## Stav vyplnění

| Část | Stav |
|---|---|
| Charakterizační testy | ✅ hotovo — 29 testů, viz níže |
| Záloha databáze | ⏳ čeká na připojené zařízení |
| Smoke průchod | ⏳ čeká na připojené zařízení |
| Zaznamenané hodnoty | ⏳ čeká na připojené zařízení |

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

_(vyplnit po vytažení zálohy)_

| Tabulka | Počet záznamů |
|---|---|
| `food_logs` | |
| `symptom_logs` | |
| `environment_logs` | |

`PRAGMA user_version` =

## Smoke průchod

Každý bod projít a zaškrtnout. Opakuje se po každé fázi migrace.

- [ ] Onboarding (nová instalace) — 4 kroky až na Dashboard
- [ ] Záznam jídla vyhledáváním v Supabase → potvrzení → uložení
- [ ] Záznam jídla čárovým kódem
- [ ] Záznam jídla ručním zadáním + tlačítko „Navrhnout alergeny"
- [ ] Záznam příznaku včetně změny data a času
- [ ] Dashboard — dnešní záznamy se zobrazí
- [ ] Grafy — přepnutí časového rozsahu a filtru příznaků
- [ ] Analýza — korelace + AI shrnutí
- [ ] Export PDF a sdílení
- [ ] Nastavení — tmavý režim, notifikace, přepnutí jazyka
- [ ] Profil — změna avataru a jména

## Zaznamenané konkrétní hodnoty

Slouží ke srovnání po migraci — musí zůstat identické.

- Nejvyšší korelace v Analýze: _(spouštěč, skóre %)_
- Počet karet na Dashboardu:
- Text AI shrnutí (první věta):
