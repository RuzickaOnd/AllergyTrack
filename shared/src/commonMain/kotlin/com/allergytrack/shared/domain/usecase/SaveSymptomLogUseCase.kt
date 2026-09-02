package com.allergytrack.shared.domain.usecase

import com.allergytrack.shared.domain.model.SymptomLog
import com.allergytrack.shared.domain.repository.SymptomRepository

class SaveSymptomLogUseCase(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(log: SymptomLog) = repository.saveLog(log)
}
