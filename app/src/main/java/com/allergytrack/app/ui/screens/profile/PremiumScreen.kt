package com.allergytrack.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FilterVintage
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.allergytrack.app.R
import com.allergytrack.app.ui.components.CardTitle
import com.allergytrack.app.ui.components.PrimaryButton
import com.allergytrack.app.ui.theme.MonoFontFamily
import com.allergytrack.app.ui.theme.SerifFontFamily
import com.allergytrack.app.ui.theme.warningColor

private data class PremiumFeatureItem(
    val icon: ImageVector,
    val titleRes: Int,
    val descRes: Int
)

private val premiumFeatureItems = listOf(
    PremiumFeatureItem(Icons.Outlined.CameraAlt, R.string.premium_feature_food_photo, R.string.premium_feature_food_photo_desc),
    PremiumFeatureItem(Icons.Outlined.FilterVintage, R.string.premium_feature_pollen, R.string.premium_feature_pollen_desc),
    PremiumFeatureItem(Icons.AutoMirrored.Outlined.ShowChart, R.string.premium_feature_extended_charts, R.string.premium_feature_extended_charts_desc),
    PremiumFeatureItem(Icons.Outlined.AutoAwesome, R.string.premium_feature_ai_analysis, R.string.premium_feature_ai_analysis_desc),
    PremiumFeatureItem(Icons.Outlined.Description, R.string.premium_feature_pdf_export, R.string.premium_feature_pdf_export_desc),
    PremiumFeatureItem(Icons.Outlined.History, R.string.premium_feature_unlimited_history, R.string.premium_feature_unlimited_history_desc),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.screen_premium), fontFamily = SerifFontFamily)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.nav_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.premium_page_cta),
                    onClick = {
                        Toast.makeText(
                            context,
                            "Tato funkce bude dostupná v příští verzi aplikace.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }

            item { PremiumHeroSection() }

            item {
                CardTitle(
                    text = stringResource(R.string.premium_page_features_title),
                    icon = Icons.Outlined.Star
                )
            }

            items(premiumFeatureItems) { feature ->
                PremiumFeatureRow(feature = feature)
            }

            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun PremiumHeroSection() {
    val accentColor = MaterialTheme.colorScheme.warningColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Star,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = accentColor
        )
        Text(
            text = stringResource(R.string.premium_page_headline),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = SerifFontFamily,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.premium_page_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PremiumFeatureRow(feature: PremiumFeatureItem) {
    val accentColor = MaterialTheme.colorScheme.warningColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = accentColor
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(feature.titleRes),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(feature.descRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = MonoFontFamily
            )
        }
    }
}
