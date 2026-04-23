package com.allergytrack.app.domain.usecase

import com.allergytrack.app.domain.model.FoodLog
import com.allergytrack.app.domain.repository.FoodRepository
import javax.inject.Inject

class SaveFoodLogUseCase @Inject constructor(
    private val repository: FoodRepository
) {
    suspend operator fun invoke(log: FoodLog) = repository.saveLog(log)
}
