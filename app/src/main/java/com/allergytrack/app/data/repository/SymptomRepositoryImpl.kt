package com.allergytrack.app.data.repository

import com.allergytrack.app.data.local.database.dao.SymptomLogDao
import com.allergytrack.app.data.local.database.entity.SymptomLogEntity
import com.allergytrack.shared.domain.model.SymptomLog
import com.allergytrack.shared.domain.model.SymptomType
import com.allergytrack.shared.domain.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SymptomRepositoryImpl(
    private val symptomLogDao: SymptomLogDao,
    private val json: Json
) : SymptomRepository {

    private val startOfDay: Long
        get() {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

    override fun getAllLogs(): Flow<List<SymptomLog>> =
        symptomLogDao.getAllLogs().map { it.map { e -> e.toDomain(json) } }

    override fun getTodayLogs(): Flow<List<SymptomLog>> =
        symptomLogDao.getTodayLogs(startOfDay).map { it.map { e -> e.toDomain(json) } }

    override fun getLogsBetween(from: Long, to: Long): Flow<List<SymptomLog>> =
        symptomLogDao.getLogsBetween(from, to).map { it.map { e -> e.toDomain(json) } }

    override suspend fun saveLog(log: SymptomLog) {
        symptomLogDao.insert(log.toEntity(json))
    }

    override suspend fun deleteLog(id: String) {
        symptomLogDao.deleteById(id)
    }

    private fun SymptomLogEntity.toDomain(json: Json) = SymptomLog(
        id = Uuid.parse(this.id),
        timestamp = this.timestamp,
        type = SymptomType.valueOf(this.type),
        intensity = this.intensity,
        bodyParts = if (this.bodyParts.isNotEmpty())
            json.decodeFromString(this.bodyParts) else emptyList(),
        latitude = this.latitude,
        longitude = this.longitude,
        notes = this.notes
    )

    private fun SymptomLog.toEntity(json: Json) = SymptomLogEntity(
        id = this.id.toString(),
        timestamp = this.timestamp,
        type = this.type.name,
        intensity = this.intensity,
        bodyParts = json.encodeToString(this.bodyParts),
        latitude = this.latitude,
        longitude = this.longitude,
        notes = this.notes
    )
}
