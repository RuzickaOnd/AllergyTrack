package com.allergytrack.app.data.local.recognition

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FoodImageRecognizer {

    suspend fun recognize(imageBytes: ByteArray): List<ImageLabel> {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return emptyList()

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(MIN_CONFIDENCE)
                .build()
        )

        return suspendCancellableCoroutine { cont ->
            labeler.process(inputImage)
                .addOnSuccessListener { labels -> cont.resume(labels) }
                .addOnFailureListener { e -> cont.resumeWithException(e) }
                .also { cont.invokeOnCancellation { labeler.close() } }
        }
    }

    companion object {
        private const val MIN_CONFIDENCE = 0.5f
    }
}
