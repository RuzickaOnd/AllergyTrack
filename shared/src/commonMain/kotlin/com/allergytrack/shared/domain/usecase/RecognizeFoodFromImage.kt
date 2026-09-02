package com.allergytrack.shared.domain.usecase

import com.allergytrack.shared.data.remote.food.RecognizedFood

/**
 * Rozpozná jídlo z obrázku zadaného řetězcovým URI.
 *
 * Implementace zůstává v :app — potřebuje android.net.Uri a ContentResolver
 * k načtení bajtů. Samotné rozpoznávání pak jde přes FoodRepository.
 */
interface RecognizeFoodFromImage {
    suspend operator fun invoke(uriString: String): List<RecognizedFood>
}
