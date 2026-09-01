package com.allergytrack.app.domain.usecase

import com.allergytrack.shared.domain.model.FoodLog
import com.allergytrack.shared.domain.repository.FoodRepository

class SaveFoodLogUseCase constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(log: FoodLog) = repository.saveLog(log)
}
