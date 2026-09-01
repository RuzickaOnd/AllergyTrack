package com.allergytrack.shared.domain.model

enum class PremiumVisibilityMode {
    HIDDEN,   // funkce neexistují v UI
    VISIBLE   // funkce viditelné, free = hvězdička + upsell
}

enum class PremiumStatus {
    FREE,
    PREMIUM,
    TRIAL
}

enum class PremiumFeature(val displayName: String) {
    FOOD_PHOTO_RECOGNITION("Foto rozpoznávání jídla"),
    POLLEN_DATA("Pylová data"),
    EXTENDED_CHARTS("Grafy 14+ dní"),
    AI_CORRELATION_ANALYSIS("AI analýza spouštěčů"),
    PDF_EXPORT("Export PDF pro lékaře"),
    UNLIMITED_HISTORY("Neomezená historie")
}
