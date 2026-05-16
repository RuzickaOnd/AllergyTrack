package com.allergytrack.app.ui.util

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Healing
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Sick
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.AllergyType
import com.allergytrack.app.domain.model.TriggerType
import com.allergytrack.app.domain.model.PremiumFeature
import com.allergytrack.app.domain.model.SymptomType
import com.allergytrack.app.domain.model.SymptomTypeOnboarding

@StringRes
fun SymptomType.labelRes(): Int = when (this) {
    SymptomType.ECZEMA       -> R.string.symptom_eczema
    SymptomType.RHINITIS     -> R.string.symptom_rhinitis
    SymptomType.EYE_ITCHING  -> R.string.symptom_eye_itching
    SymptomType.URTICARIA    -> R.string.symptom_urticaria
    SymptomType.ASTHMA       -> R.string.symptom_asthma
    SymptomType.ITCHING      -> R.string.symptom_itching
    SymptomType.DIGESTIVE    -> R.string.symptom_digestive
    SymptomType.OTHER        -> R.string.symptom_other
}

@StringRes
fun SymptomTypeOnboarding.labelRes(): Int = when (this) {
    SymptomTypeOnboarding.ECZEMA      -> R.string.symptom_eczema
    SymptomTypeOnboarding.RHINITIS    -> R.string.symptom_rhinitis
    SymptomTypeOnboarding.EYE_ITCHING -> R.string.symptom_eye_itching
    SymptomTypeOnboarding.URTICARIA   -> R.string.symptom_urticaria
    SymptomTypeOnboarding.ASTHMA      -> R.string.symptom_asthma
    SymptomTypeOnboarding.ITCHING     -> R.string.symptom_itching
    SymptomTypeOnboarding.DIGESTIVE   -> R.string.symptom_digestive
    SymptomTypeOnboarding.SNEEZING    -> R.string.symptom_sneezing
}

@StringRes
fun AllergyType.labelRes(): Int = when (this) {
    AllergyType.POLLEN  -> R.string.allergy_pollen
    AllergyType.FOOD    -> R.string.allergy_food
    AllergyType.ANIMAL  -> R.string.allergy_animal
    AllergyType.DUST    -> R.string.allergy_dust
    AllergyType.MOLD    -> R.string.allergy_mold
    AllergyType.UNKNOWN -> R.string.allergy_unknown
}

fun AllergyType.icon(): ImageVector = when (this) {
    AllergyType.POLLEN  -> Icons.Outlined.LocalFlorist
    AllergyType.FOOD    -> Icons.Outlined.Restaurant
    AllergyType.ANIMAL  -> Icons.Outlined.Pets
    AllergyType.DUST    -> Icons.Outlined.Home
    AllergyType.MOLD    -> Icons.Outlined.Science
    AllergyType.UNKNOWN -> Icons.Outlined.Help
}

fun SymptomType.icon(): ImageVector = when (this) {
    SymptomType.ECZEMA      -> Icons.Outlined.Healing
    SymptomType.RHINITIS    -> Icons.Outlined.WaterDrop
    SymptomType.EYE_ITCHING -> Icons.Outlined.Visibility
    SymptomType.URTICARIA   -> Icons.Outlined.MedicalServices
    SymptomType.ASTHMA      -> Icons.Outlined.Air
    SymptomType.ITCHING     -> Icons.Outlined.TouchApp
    SymptomType.DIGESTIVE   -> Icons.Outlined.Restaurant
    SymptomType.OTHER       -> Icons.Outlined.Help
}

fun TriggerType.icon(): ImageVector = when (this) {
    TriggerType.FOOD        -> Icons.Outlined.Restaurant
    TriggerType.ENVIRONMENT -> Icons.Outlined.LocalFlorist
    TriggerType.COMBINED    -> Icons.Outlined.Insights
}

/** Ikona pro FoodTrigger: allergen (EU 14) → Warning, obecné jídlo → Restaurant */
fun foodTriggerIcon(allergenKey: String): ImageVector =
    if (allergenKey.startsWith("__food__")) Icons.Outlined.Restaurant else Icons.Outlined.Warning

fun SymptomTypeOnboarding.icon(): ImageVector = when (this) {
    SymptomTypeOnboarding.ECZEMA      -> Icons.Outlined.Healing
    SymptomTypeOnboarding.RHINITIS    -> Icons.Outlined.WaterDrop
    SymptomTypeOnboarding.EYE_ITCHING -> Icons.Outlined.Visibility
    SymptomTypeOnboarding.URTICARIA   -> Icons.Outlined.MedicalServices
    SymptomTypeOnboarding.ASTHMA      -> Icons.Outlined.Air
    SymptomTypeOnboarding.ITCHING     -> Icons.Outlined.TouchApp
    SymptomTypeOnboarding.DIGESTIVE   -> Icons.Outlined.Restaurant
    SymptomTypeOnboarding.SNEEZING    -> Icons.Outlined.Sick
}

@StringRes
fun PremiumFeature.displayNameRes(): Int = when (this) {
    PremiumFeature.FOOD_PHOTO_RECOGNITION -> R.string.premium_feature_food_photo
    PremiumFeature.POLLEN_DATA            -> R.string.premium_feature_pollen
    PremiumFeature.EXTENDED_CHARTS        -> R.string.premium_feature_extended_charts
    PremiumFeature.AI_CORRELATION_ANALYSIS -> R.string.premium_feature_ai_analysis
    PremiumFeature.PDF_EXPORT             -> R.string.premium_feature_pdf_export
    PremiumFeature.UNLIMITED_HISTORY      -> R.string.premium_feature_unlimited_history
}

val BODY_PART_RES: Map<String, Int> = mapOf(
    "Obličej"  to R.string.body_part_face,
    "Krk"      to R.string.body_part_neck,
    "Hrudník"  to R.string.body_part_chest,
    "Záda"     to R.string.body_part_back,
    "Paže"     to R.string.body_part_arms,
    "Ruce"     to R.string.body_part_hands,
    "Břicho"   to R.string.body_part_stomach,
    "Nohy"     to R.string.body_part_legs,
    "Chodidla" to R.string.body_part_feet
)
