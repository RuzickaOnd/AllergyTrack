package com.allergytrack.shared.platform

/**
 * Platformní informace a operace, které ViewModel potřebuje,
 * ale nemůže na ně sáhnout přímo z commonMain.
 */
interface DeviceInfo {
    /** Aktuální jazyk aplikace, prázdný řetězec = systémový. */
    fun currentLanguage(): String

    /** Nastaví jazyk aplikace. */
    fun setLanguage(code: String)

    /**
     * True, pokud změna jazyka vyžaduje restart aktivity.
     * Na Androidu < 13 ano, od API 33 to řeší LocaleManager sám.
     */
    fun requiresRestartOnLanguageChange(): Boolean
}
