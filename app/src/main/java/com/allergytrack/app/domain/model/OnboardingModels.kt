package com.allergytrack.app.domain.model

/** Typy alergií pro výběr v onboardingu */
enum class AllergyType(val label: String, val emoji: String) {
    POLLEN("Pylová (senná rýma)", "🌿"),
    FOOD("Potravinová", "🥜"),
    ANIMAL("Zvířata", "🐱"),
    DUST("Prach a roztoči", "🏠"),
    MOLD("Plísně", "🍄"),
    UNKNOWN("Nevím / jiné", "🤔")
}

/** Typické alergické projevy pro výběr v onboardingu */
enum class SymptomTypeOnboarding(val label: String, val emoji: String) {
    ECZEMA("Ekzém", "🔴"),
    RHINITIS("Rýma", "💧"),
    EYE_ITCHING("Svědění očí", "👁"),
    URTICARIA("Kopřivka", "🔵"),
    ASTHMA("Astma", "🌬"),
    ITCHING("Svědění kůže", "⚡"),
    SNEEZING("Kýchání", "🤧")
}

/** Stav konkrétního kroku onboardingu */
enum class OnboardingStep {
    WELCOME,
    ALLERGY_PROFILE,
    PERMISSIONS,
    READY
}
