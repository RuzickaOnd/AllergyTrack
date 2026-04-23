package com.allergytrack.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.app.ui.theme.SerifFontFamily

@Composable
fun AboutScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        // Logo / ikona
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("🌿", style = MaterialTheme.typography.displaySmall)
            }
        }

        Text(
            "AllergyTrack",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = SerifFontFamily
        )

        Text(
            "Verze ${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        // Co aplikace dělá
        AboutSection(
            title = "Co AllergyTrack dělá",
            content = "AllergyTrack ti pomáhá identifikovat spouštěče tvých alergií. " +
                    "Zaznamenáváš, co jíš, kde se pohybuješ a jaké alergické projevy " +
                    "pociťuješ — aplikace pak hledá vzorce a korelace mezi těmito daty."
        )

        AboutSection(
            title = "Jak to funguje",
            content = "Každý den loguješ jídlo (foto nebo ruční zadání), " +
                    "alergické symptomy (ekzém, rýma, svědění...) a aplikace automaticky " +
                    "načítá data o kvalitě ovzduší a pylu podle tvé GPS polohy. " +
                    "Po nashromáždění dat analýza odhalí, co tvé alergie nejčastěji spouští."
        )

        AboutSection(
            title = "Tvá data",
            content = "Veškerá data jsou uložena pouze v tvém zařízení a nikdy " +
                    "nejsou sdílena s třetími stranami. Aplikace nesbírá žádná osobní data."
        )

        AboutSection(
            title = "Zdroje dat",
            content = "Kvalita ovzduší a pylová data: Open-Meteo (open-meteo.com)\n" +
                    "Databáze potravin: Open Food Facts (openfoodfacts.org)"
        )

        // Disclaimer
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "⚠️ Tato aplikace není zdravotnický prostředek a neposkytuje " +
                        "lékařskou diagnózu ani léčbu. Pro lékařskou radu se vždy obraťte " +
                        "na kvalifikovaného zdravotnického pracovníka.",
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun AboutSection(title: String, content: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold)
        Text(content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight)
    }
}
