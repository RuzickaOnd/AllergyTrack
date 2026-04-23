package com.allergytrack.app.domain.usecase

import com.allergytrack.app.data.remote.food.RecognizedFood
import com.allergytrack.app.domain.repository.FoodRepository
import javax.inject.Inject

class SearchFoodUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(query: String): List<RecognizedFood> =
        repository.searchFoodByName(query)
}
