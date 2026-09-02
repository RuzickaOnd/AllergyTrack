package com.allergytrack.shared.platform

/** Implementuje AnalyticsManager v :app (Firebase Analytics). */
interface Analytics {
    fun logScreenView(screenName: String, screenClass: String? = null)
    fun logFoodLogged(foodName: String, allergenCount: Int)
    fun logSymptomLogged(symptomType: String, severity: Int)
    fun logEnvironmentLogged(region: String, aqi: Int?)
    fun logPremiumInteraction(featureName: String, action: String)
    fun logOnboardingStep(stepNumber: Int, stepName: String)
    fun logAiAnalysisCardClicked(triggerCount: Int)
}
