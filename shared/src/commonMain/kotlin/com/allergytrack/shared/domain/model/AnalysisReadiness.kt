package com.allergytrack.shared.domain.model

/**
 * Stav připravenosti korelační analýzy.
 *
 * Logika:
 *   < 5 symptomů  → Locked    (analýza nedává smysl, nezobrazovat nic)
 *   5–9 symptomů  → Partial   (základní korelace, zobrazit s disclaimerem)
 *   10+ symptomů  → Full      (plná analýza, AI shrnutí bez disclaimeru)
 *
 * Environmentální korelace navíc vyžaduje alespoň 5 env. logů.
 * Pokud chybí env. data, zobrazí se pouze potravinové korelace.
 */
sealed class AnalysisReadiness {

    /**
     * Nedostatek dat — analýza se nezobrazuje.
     * @param symptomCount Aktuální počet symptomů
     * @param requiredCount Minimální počet pro odemčení (vždy 5)
     * @param progress 0.0–1.0 pro progress bar
     *
     * Text hlášky sem nepatří — je to doménový model v commonMain a text
     * by nešel lokalizovat. Vykresluje ho AnalysisScreen z requiredCount.
     */
    data class Locked(
        val symptomCount: Int,
        val requiredCount: Int = MIN_SYMPTOMS,
        val progress: Float = (symptomCount.toFloat() / requiredCount).coerceIn(0f, 1f)
    ) : AnalysisReadiness()

    /**
     * Základní analýza dostupná — zobrazit s disclaimerem o přesnosti.
     * Nastane při 5–9 symptomech.
     * @param hasEnvironmentData Jsou dostupná env. data pro korelaci?
     * @param remainingToFull Kolik symptomů zbývá do plné analýzy
     *
     * Text disclaimeru sem nepatří — vykresluje ho AnalysisScreen
     * z remainingToFull a hasEnvironmentData.
     */
    data class Partial(
        val symptomCount: Int,
        val hasEnvironmentData: Boolean,
        val remainingToFull: Int = FULL_SYMPTOMS - symptomCount
    ) : AnalysisReadiness()

    /**
     * Plná analýza — 10+ symptomů, žádný disclaimer.
     * @param hasEnvironmentData Jsou dostupná env. data pro korelaci?
     */
    data class Full(
        val hasEnvironmentData: Boolean
    ) : AnalysisReadiness()

    companion object {
        const val MIN_SYMPTOMS = 5
        const val FULL_SYMPTOMS = 10
        const val MIN_ENV_LOGS = 5

        /**
         * Vyhodnotí stav připravenosti na základě aktuálních dat.
         */
        fun evaluate(
            symptomCount: Int,
            environmentLogCount: Int
        ): AnalysisReadiness {
            val hasEnv = environmentLogCount >= MIN_ENV_LOGS
            return when {
                symptomCount < MIN_SYMPTOMS ->
                    Locked(symptomCount = symptomCount)
                symptomCount < FULL_SYMPTOMS ->
                    Partial(
                        symptomCount = symptomCount,
                        hasEnvironmentData = hasEnv
                    )
                else ->
                    Full(hasEnvironmentData = hasEnv)
            }
        }

    }
}
