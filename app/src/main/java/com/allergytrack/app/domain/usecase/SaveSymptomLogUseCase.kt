package com.allergytrack.app.domain.usecase

import com.allergytrack.shared.domain.model.SymptomLog
import com.allergytrack.shared.domain.repository.SymptomRepository

class SaveSymptomLogUseCase constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(log: SymptomLog) = repository.saveLog(log)
}
