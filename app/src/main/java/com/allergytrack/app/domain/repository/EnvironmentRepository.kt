package com.allergytrack.app.domain.repository

import com.allergytrack.app.domain.model.EnvironmentLog
import com.allergytrack.app.domain.model.EnvironmentRegion
import kotlinx.coroutines.flow.Flow

interface EnvironmentRepository {
    fun getAllLogs(): Flow<List<EnvironmentLog>>
    fun getLogsBetween(from: Long, to: Long): Flow<List<EnvironmentLog>>
    suspend fun getLatest(): EnvironmentLog?
    suspend fun saveLog(log: EnvironmentLog)
    suspend fun fetchAndSaveCurrentEnvironment(): EnvironmentLog?

    // Rychlé zjištění regionu z kešované poslední polohy — bez čekání na GPS fix.
    // Používá se jako pre-check před zahájením fetch, aby se zbytečně nespouštěl
    // loading pro uživatele mimo EU (kde API neposkytuje žádná data).
    // Vrací UNKNOWN pokud kešovaná poloha není dostupná (první spuštění, bez oprávnění).
    suspend fun getLastKnownRegion(): EnvironmentRegion
}
