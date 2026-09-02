package com.allergytrack.app.platform

import android.content.Context
import android.net.Uri
import com.allergytrack.shared.domain.usecase.RecognizeFoodFromImage
import androidx.core.net.toUri
import com.allergytrack.shared.data.remote.food.RecognizedFood
import com.allergytrack.shared.domain.repository.FoodRepository

class RecognizeFoodFromImageUseCase(
    private val context: Context,
    private val repository: FoodRepository
) : RecognizeFoodFromImage {
    override suspend operator fun invoke(uriString: String): List<RecognizedFood> {
        // ViewModel je v commonMain a android.net.Uri tam neexistuje,
        // proto předává řetězec; parsuje se až tady, v Android vrstvě.
        val uri = uriString.toUri()
        val imageBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return emptyList()
        return repository.recognizeFoodFromImage(imageBytes)
    }
}
