package com.allergytrack.app.domain.usecase

import com.allergytrack.app.domain.model.SymptomLog
import com.allergytrack.app.domain.repository.SymptomRepository
import javax.inject.Inject

class SaveSymptomLogUseCase @Inject constructor(
    private val repository: SymptomRepository
) {
    suspend operator fun invoke(log: SymptomLog) = repository.saveLog(log)
}
