package com.allergytrack.app.domain.usecase

import com.allergytrack.shared.data.remote.food.RecognizedFood
import com.allergytrack.shared.domain.repository.FoodRepository

class SearchFoodUseCase constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(query: String): List<RecognizedFood> =
        repository.searchFoodByName(query)
}
