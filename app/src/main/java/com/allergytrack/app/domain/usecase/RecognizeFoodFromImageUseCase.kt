package com.allergytrack.app.domain.usecase

import android.content.Context
import android.net.Uri
import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.domain.repository.FoodRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RecognizeFoodFromImageUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: FoodRepository
) {
    suspend operator fun invoke(uri: Uri): List<RecognizedFood> {
        val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return emptyList()
        return repository.recognizeFoodFromImage(imageBytes)
    }
}
