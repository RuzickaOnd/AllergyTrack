# Privacy Policy — AllergyTrack

**Last updated: May 17, 2026**

---

## English

### 1. Overview

AllergyTrack ("the App") is a personal allergy tracking application that helps you log food, symptoms, and environmental conditions to identify potential allergy triggers. This Privacy Policy explains what data is collected, how it is used, and what rights you have.

### 2. Data We Collect

#### 2.1 Data You Enter Directly
- **Food logs** — names of foods, allergens, timestamps
- **Symptom logs** — symptom types, severity, body parts affected, timestamps
- **Allergy profile** — allergy types and symptom types selected during onboarding
- **Display name and avatar** — optional, stored only on your device

All of this data is stored **locally on your device** in an encrypted Room database. It is never uploaded to our servers.

#### 2.2 Device Sensors and Permissions

| Permission | Purpose | When Used |
|---|---|---|
| Location (GPS) | Fetching local air quality and pollen data from Open-Meteo | Background, every 30 minutes |
| Camera | Scanning barcodes, taking food photos | Only when you initiate a scan |
| Microphone | Voice input for food logging | Only when you tap the microphone button |
| Notifications | Evening reminders to log your symptoms | Only if you enable this in Settings |

Location data is sent to **Open-Meteo** (open-meteo.com) solely to retrieve environmental data for your area. Your coordinates are not stored by us or by Open-Meteo beyond the scope of a single API request. Open-Meteo is a free, open-source weather API that does not require registration.

#### 2.3 Food Database Queries
When you search for a food product or scan a barcode, the search query or barcode number is sent to our **Supabase** backend (a PostgreSQL database hosted on supabase.com). This database mirrors public data from Open Food Facts. We do not link these queries to your identity.

#### 2.4 Analytics and Crash Reporting
The App includes **Firebase Analytics** and **Firebase Crashlytics** (Google LLC). These services collect:
- Anonymous usage events (screen views, feature usage)
- Crash reports including device model, OS version, and app version

No personally identifiable information is included in these reports. Firebase data is processed by Google in accordance with their [Privacy Policy](https://policies.google.com/privacy).

### 3. Data Storage and Security

- All personal health data (food logs, symptom logs, allergy profile) is stored **exclusively on your device**.
- Data is not synced to any cloud service operated by us.
- If you uninstall the App, all locally stored data is permanently deleted.
- You can manually delete all data at any time via **Settings → Delete All Data**.

### 4. Data Sharing

We do not sell, rent, or share your personal data with third parties for advertising or marketing purposes.

Data is transmitted only to:
- **Open-Meteo** — your GPS coordinates for environmental data (no registration, no tracking)
- **Supabase** — food search queries and barcode lookups (no user identity)
- **Google Firebase** — anonymous analytics and crash reports

The App may in the future offer optional paid features. Any purchases would be processed exclusively by **Google Play** (Google LLC). We do not collect or store payment card information.

### 5. Children's Privacy

The App is not directed at children under the age of 13. We do not knowingly collect data from children.

### 6. Your Rights

You have the right to:
- **Access** all your data — it is stored locally and visible within the App
- **Delete** all your data — Settings → Delete All Data
- **Withdraw permissions** — via Android system Settings → Apps → AllergyTrack → Permissions

### 7. Changes to This Policy

We may update this policy from time to time. Changes will be posted at this URL. Continued use of the App after changes constitutes acceptance of the updated policy.

### 8. Contact

If you have questions about this Privacy Policy, contact us at:
**ruzicka.ond@gmail.com**

---

---

## Čeština

# Zásady ochrany osobních údajů — AllergyTrack

**Poslední aktualizace: 17. května 2026**

### 1. Přehled

AllergyTrack („Aplikace") je osobní aplikace pro sledování alergií, která vám pomáhá zaznamenávat jídlo, příznaky a podmínky prostředí za účelem identifikace možných spouštěčů alergií. Tyto zásady ochrany osobních údajů vysvětlují, jaká data jsou shromažďována, jak jsou využívána a jaká máte práva.

### 2. Jaká data shromažďujeme

#### 2.1 Data, která zadáváte přímo
- **Záznamy o jídle** — názvy potravin, alergeny, časové razítko
- **Záznamy o příznacích** — typy příznaků, intenzita, zasažené části těla, časové razítko
- **Alergický profil** — typy alergií a příznaků vybrané během úvodního nastavení
- **Zobrazované jméno a avatar** — volitelné, uloženo pouze v zařízení

Všechna tato data jsou uložena **výhradně ve vašem zařízení** v šifrované databázi Room. Nikdy nejsou nahrávána na naše servery.

#### 2.2 Senzory zařízení a oprávnění

| Oprávnění | Účel | Kdy se používá |
|---|---|---|
| Poloha (GPS) | Načtení místní kvality ovzduší a pylových dat z Open-Meteo | Na pozadí, každých 30 minut |
| Kamera | Skenování čárových kódů, focení jídla | Pouze při aktivním skenování |
| Mikrofon | Hlasové zadávání jídla | Pouze při stisku tlačítka mikrofonu |
| Oznámení | Večerní připomínky k zaznamenání příznaků | Pouze pokud zapnete v Nastavení |

Data o poloze jsou odesílána službě **Open-Meteo** (open-meteo.com) výhradně za účelem získání environmentálních dat pro vaši oblast. Vaše souřadnice nejsou námi ani Open-Meteo ukládány mimo rámec jediného API požadavku. Open-Meteo je bezplatné open-source API bez nutnosti registrace.

#### 2.3 Dotazy do databáze potravin
Při vyhledávání potravinového produktu nebo skenování čárového kódu je vyhledávací dotaz nebo číslo čárového kódu odeslán na náš backend **Supabase** (databáze PostgreSQL hostovaná na supabase.com). Tato databáze zrcadlí veřejná data z Open Food Facts. Tyto dotazy nespojujeme s vaší identitou.

#### 2.4 Analytika a hlášení pádu aplikace
Aplikace obsahuje **Firebase Analytics** a **Firebase Crashlytics** (Google LLC). Tyto služby shromažďují:
- Anonymní události o používání (zobrazení obrazovek, využití funkcí)
- Hlášení o pádech aplikace včetně modelu zařízení, verze OS a verze aplikace

Žádné osobně identifikovatelné informace nejsou součástí těchto hlášení. Data Firebase zpracovává Google v souladu s jejich [Zásadami ochrany osobních údajů](https://policies.google.com/privacy).

### 3. Ukládání dat a bezpečnost

- Veškerá osobní zdravotní data (záznamy o jídle, příznaky, alergický profil) jsou uložena **výhradně ve vašem zařízení**.
- Data nejsou synchronizována s žádnou cloudovou službou provozovanou námi.
- Po odinstalaci aplikace jsou všechna lokálně uložená data trvale odstraněna.
- Všechna data můžete kdykoli ručně smazat prostřednictvím **Nastavení → Smazat všechna data**.

### 4. Sdílení dat

Vaše osobní údaje neprodáváme, nepronajímáme ani nesdílíme s třetími stranami pro reklamní nebo marketingové účely.

Data jsou přenášena pouze:
- **Open-Meteo** — vaše GPS souřadnice pro environmentální data (bez registrace, bez sledování)
- **Supabase** — dotazy pro vyhledávání potravin a čárové kódy (bez identity uživatele)
- **Google Firebase** — anonymní analytika a hlášení o pádech

Aplikace může v budoucnu nabízet volitelné placené funkce. Veškeré nákupy by byly zpracovány výhradně prostřednictvím **Google Play** (Google LLC). Informace o platebních kartách neshromažďujeme ani neukládáme.

### 5. Ochrana dětí

Aplikace není určena dětem mladším 13 let. Vědomě neshromažďujeme data od dětí.

### 6. Vaše práva

Máte právo:
- **Přistupovat** ke všem svým datům — jsou uložena lokálně a viditelná v Aplikaci
- **Smazat** všechna svá data — Nastavení → Smazat všechna data
- **Odvolat oprávnění** — přes Nastavení systému Android → Aplikace → AllergyTrack → Oprávnění

### 7. Změny těchto zásad

Tyto zásady můžeme čas od času aktualizovat. Změny budou zveřejněny na této URL adrese. Pokračováním v používání Aplikace po provedení změn vyjadřujete souhlas s aktualizovanými zásadami.

### 8. Kontakt

Máte-li dotazy k těmto zásadám ochrany osobních údajů, kontaktujte nás na:
**ruzicka.ond@gmail.com**
