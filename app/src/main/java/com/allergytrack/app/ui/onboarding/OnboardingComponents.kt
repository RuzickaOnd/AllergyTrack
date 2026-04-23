package com.allergytrack.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.allergytrack.app.domain.model.OnboardingStep
import com.allergytrack.app.ui.theme.Border
import com.allergytrack.app.ui.theme.DotRed
import com.allergytrack.app.ui.theme.SurfaceVariant

import com.allergytrack.app.ui.theme.*

/**
 * Progress dots — ukazatel aktuálního kroku podle wireframu.
 */
@Composable
fun OnboardingProgressDots(
    currentStep: OnboardingStep,
    modifier: Modifier = Modifier
) {
    val currentIndex = currentStep.ordinal

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnboardingStep.entries.forEachIndexed { index, _ ->
            val isActive = index == currentIndex
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (isActive) 24.dp else 6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isActive) AccentGreen else Border)
            )
        }
    }
}

/**
 * Grid item pro výběr alergií ve stylu wireframu.
 */
@Composable
fun OnboardingSelectCard(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) AccentGreen.copy(alpha = 0.1f) else SurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) AccentGreen else Border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) TextPrimary else TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 9.sp
            )
        }
    }
}

/**
 * Společná dolní lišta pro onboarding s tlačítky Zpět a Pokračovat.
 */
@Composable
fun OnboardingBottomBar(
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    nextLabel: String = "POKRAČOVAT →",
    nextEnabled: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.allergytrack.app.ui.components.SecondaryButton(
            text = "ZPĚT",
            onClick = onBack,
            modifier = Modifier.weight(1f)
        )
        
        com.allergytrack.app.ui.components.PrimaryButton(
            text = nextLabel.uppercase(),
            onClick = onNext,
            modifier = Modifier.weight(1.5f),
            enabled = nextEnabled
        )
    }
}
