package com.allergytrack.app.data.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manažer pro správu a odesílání analytických událostí do Firebase Analytics.
 * Inicializace probíhá efektivně (lazy inicializace na pozadí), aby se neblokovalo hlavní vlákno při startu aplikace.
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Lazy inicializace Firebase Analytics - zabrání zbytečnému I/O čtení na hlavním vlákně při startu
    private val firebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(context)
    }

    /**
     * Inicializuje Firebase Analytics. Doporučuje se volat na pozadí (Dispatchers.Default/IO).
     */
    fun initialize() {
        // Přístupem k lazy vlastnosti vyvoláme inicializaci SDK na pozadí
        firebaseAnalytics
    }

    /**
     * Obecná privátní metoda pro odeslání události do Firebase Analytics.
     */
    private fun logEvent(eventName: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(eventName, params)
    }

    /**
     * Záznam zobrazení konkrétní obrazovky.
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            if (screenClass != null) {
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    /**
     * Záznam uložení jídla (Food Logged).
     */
    fun logFoodLogged(foodName: String, allergenCount: Int) {
        val bundle = Bundle().apply {
            putString("food_name", foodName)
            putInt("allergen_count", allergenCount)
        }
        logEvent("food_logged", bundle)
    }

    /**
     * Záznam uložení příznaku (Symptom Logged).
     */
    fun logSymptomLogged(symptomType: String, severity: Int) {
        val bundle = Bundle().apply {
            putString("symptom_type", symptomType)
            putInt("severity", severity)
        }
        logEvent("symptom_logged", bundle)
    }

    /**
     * Záznam uložení environmentálních dat (Environment Logged).
     */
    fun logEnvironmentLogged(region: String, aqi: Int?) {
        val bundle = Bundle().apply {
            putString("region", region)
            if (aqi != null) {
                putInt("aqi", aqi)
            }
        }
        logEvent("environment_logged", bundle)
    }

    /**
     * Záznam interakce s Premium funkcemi.
     */
    fun logPremiumInteraction(featureName: String, action: String) {
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
            putString("action", action) // např. "view_upsell", "click_buy", "purchase_success"
        }
        logEvent("premium_interaction", bundle)
    }

    /**
     * Záznam o onboarding krocích.
     */
    fun logOnboardingStep(stepNumber: Int, stepName: String) {
        val bundle = Bundle().apply {
            putInt("step_number", stepNumber)
            putString("step_name", stepName)
        }
        logEvent("onboarding_step", bundle)
    }

    /**
     * Záznam kliknutí na kartu AI analýzy.
     */
    fun logAiAnalysisCardClicked(triggerCount: Int) {
        val bundle = Bundle().apply {
            putInt("trigger_count", triggerCount)
        }
        logEvent("ai_analysis_card_clicked", bundle)
    }
}
