package com.allergytrack.shared.platform

/** Rozpoznaný štítek z obrázku. */
data class RecognizedLabel(
    val text: String,
    val confidence: Float
)

/**
 * Rozpoznávání obsahu fotky. Implementuje FoodImageRecognizer v :app
 * (ML Kit Image Labeling). Na iOS to bude Vision framework.
 */
interface ImageRecognizer {
    suspend fun recognize(imageBytes: ByteArray): List<RecognizedLabel>
}
