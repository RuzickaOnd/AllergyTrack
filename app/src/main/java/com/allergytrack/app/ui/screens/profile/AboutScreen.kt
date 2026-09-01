package com.allergytrack.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.allergytrack.app.BuildConfig
import com.allergytrack.shared.resources.*
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

        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Outlined.LocalFlorist,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            "AllergyTrack",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = SerifFontFamily
        )

        Text(
            stringResource(Res.string.about_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider()

        AboutSection(
            title = stringResource(Res.string.about_section_what_title),
            content = stringResource(Res.string.about_section_what_content)
        )
        AboutSection(
            title = stringResource(Res.string.about_section_how_title),
            content = stringResource(Res.string.about_section_how_content)
        )
        AboutSection(
            title = stringResource(Res.string.about_section_data_title),
            content = stringResource(Res.string.about_section_data_content)
        )
        AboutSection(
            title = stringResource(Res.string.about_section_sources_title),
            content = stringResource(Res.string.about_section_sources_content)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).padding(top = 1.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(Res.string.about_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}
