package com.allergytrack.app.ui.util

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
import org.jetbrains.compose.resources.StringResource
import com.allergytrack.shared.resources.*
import com.allergytrack.shared.domain.model.AllergyType
import com.allergytrack.shared.domain.model.TriggerType
import com.allergytrack.shared.domain.model.PremiumFeature
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.shared.domain.model.SymptomTypeOnboarding

fun SymptomType.labelRes(): StringResource = when (this) {
    SymptomType.ECZEMA       -> Res.string.symptom_eczema
    SymptomType.RHINITIS     -> Res.string.symptom_rhinitis
    SymptomType.EYE_ITCHING  -> Res.string.symptom_eye_itching
    SymptomType.URTICARIA    -> Res.string.symptom_urticaria
    SymptomType.ASTHMA       -> Res.string.symptom_asthma
    SymptomType.ITCHING      -> Res.string.symptom_itching
    SymptomType.DIGESTIVE    -> Res.string.symptom_digestive
    SymptomType.OTHER        -> Res.string.symptom_other
}

fun SymptomTypeOnboarding.labelRes(): StringResource = when (this) {
    SymptomTypeOnboarding.ECZEMA      -> Res.string.symptom_eczema
    SymptomTypeOnboarding.RHINITIS    -> Res.string.symptom_rhinitis
    SymptomTypeOnboarding.EYE_ITCHING -> Res.string.symptom_eye_itching
    SymptomTypeOnboarding.URTICARIA   -> Res.string.symptom_urticaria
    SymptomTypeOnboarding.ASTHMA      -> Res.string.symptom_asthma
    SymptomTypeOnboarding.ITCHING     -> Res.string.symptom_itching
    SymptomTypeOnboarding.DIGESTIVE   -> Res.string.symptom_digestive
    SymptomTypeOnboarding.SNEEZING    -> Res.string.symptom_sneezing
}

fun AllergyType.labelRes(): StringResource = when (this) {
    AllergyType.POLLEN  -> Res.string.allergy_pollen
    AllergyType.FOOD    -> Res.string.allergy_food
    AllergyType.ANIMAL  -> Res.string.allergy_animal
    AllergyType.DUST    -> Res.string.allergy_dust
    AllergyType.MOLD    -> Res.string.allergy_mold
    AllergyType.UNKNOWN -> Res.string.allergy_unknown
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

fun PremiumFeature.displayNameRes(): StringResource = when (this) {
    PremiumFeature.FOOD_PHOTO_RECOGNITION -> Res.string.premium_feature_food_photo
    PremiumFeature.POLLEN_DATA            -> Res.string.premium_feature_pollen
    PremiumFeature.EXTENDED_CHARTS        -> Res.string.premium_feature_extended_charts
    PremiumFeature.AI_CORRELATION_ANALYSIS -> Res.string.premium_feature_ai_analysis
    PremiumFeature.PDF_EXPORT             -> Res.string.premium_feature_pdf_export
    PremiumFeature.UNLIMITED_HISTORY      -> Res.string.premium_feature_unlimited_history
}

val BODY_PART_RES: Map<String, StringResource> = mapOf(
    "Obličej"  to Res.string.body_part_face,
    "Krk"      to Res.string.body_part_neck,
    "Hrudník"  to Res.string.body_part_chest,
    "Záda"     to Res.string.body_part_back,
    "Paže"     to Res.string.body_part_arms,
    "Ruce"     to Res.string.body_part_hands,
    "Břicho"   to Res.string.body_part_stomach,
    "Nohy"     to Res.string.body_part_legs,
    "Chodidla" to Res.string.body_part_feet
)
