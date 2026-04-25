package com.allergytrack.app.ui.util

import androidx.annotation.StringRes
import com.allergytrack.app.R
import com.allergytrack.app.domain.model.AllergyType
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
