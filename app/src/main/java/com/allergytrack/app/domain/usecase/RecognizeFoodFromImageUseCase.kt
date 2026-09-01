package com.allergytrack.app.domain.usecase

import android.content.Context
import android.net.Uri
import com.allergytrack.shared.data.remote.food.RecognizedFood
import com.allergytrack.shared.domain.repository.FoodRepository

class RecognizeFoodFromImageUseCase(
    private val context: Context,
    private val repository: FoodRepository
) {
    suspend operator fun invoke(uri: Uri): List<RecognizedFood> {
        val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return emptyList()
        return repository.recognizeFoodFromImage(imageBytes)
    }
}
