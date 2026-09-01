package com.allergytrack.app.domain.usecase

import com.allergytrack.app.domain.model.FoodLog
import com.allergytrack.app.domain.repository.FoodRepository

class SaveFoodLogUseCase constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(log: FoodLog) = repository.saveLog(log)
}
