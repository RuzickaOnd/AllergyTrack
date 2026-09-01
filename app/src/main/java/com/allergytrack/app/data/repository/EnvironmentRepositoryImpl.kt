package com.allergytrack.app.data.repository

import com.allergytrack.app.data.local.database.dao.EnvironmentLogDao
import com.allergytrack.app.data.local.database.entity.EnvironmentLogEntity
import com.allergytrack.app.data.location.LocationProvider
import com.allergytrack.app.data.remote.airquality.AirQualityApiService
import com.allergytrack.app.data.remote.airquality.toPollenIndex
import com.allergytrack.app.domain.model.EnvironmentLog
import com.allergytrack.app.domain.model.EnvironmentRegion
import com.allergytrack.app.domain.model.isEurope
import com.allergytrack.app.domain.repository.EnvironmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EnvironmentRepositoryImpl constructor(
    private val environmentLogDao: EnvironmentLogDao,
    private val airQualityApi: AirQualityApiService,
    private val locationProvider: LocationProvider
) : EnvironmentRepository {

    override fun getAllLogs(): Flow<List<EnvironmentLog>> =
        environmentLogDao.getAllLogs().map { it.map { e -> e.toDomain() } }

    override fun getLogsBetween(from: Long, to: Long): Flow<List<EnvironmentLog>> =
        environmentLogDao.getLogsBetween(from, to).map { it.map { e -> e.toDomain() } }

    override suspend fun getLatest(): EnvironmentLog? =
        environmentLogDao.getLatest()?.toDomain()

    override suspend fun saveLog(log: EnvironmentLog) {
        environmentLogDao.insert(log.toEntity())
    }

    override suspend fun getLastKnownRegion(): EnvironmentRegion {
        val location = locationProvider.getLastKnownLocation() ?: return EnvironmentRegion.UNKNOWN
        return if (isEurope(location.latitude, location.longitude))
            EnvironmentRegion.EUROPE
        else
            EnvironmentRegion.GLOBAL
    }

    override suspend fun fetchAndSaveCurrentEnvironment(): EnvironmentLog? {
        val location = try {
            locationProvider.getCurrentLocation()
        } catch (e: SecurityException) {
            null
        }

        val lat: Double
        val lon: Double
        if (location != null) {
            lat = location.latitude
            lon = location.longitude
        } else {
            val lastLog = environmentLogDao.getLatest() ?: return null
            lat = lastLog.latitude
            lon = lastLog.longitude
        }
        val region = if (isEurope(lat, lon)) EnvironmentRegion.EUROPE else EnvironmentRegion.GLOBAL

        // Vždy voláme API — AQI je globální
        val data = try {
            airQualityApi.getCurrentAirQuality(lat, lon)
        } catch (e: Exception) {
            null // Síťová chyba — uložíme log s GPS ale bez env. dat
        }

        val current = data?.current

        // Pyl: přijmeme hodnotu pouze pokud jsme v EU a API ji vrátilo (non-null)
        // Mimo EU nebo mimo sezónu API vrací null — to ukládáme jako null, ne jako 0
        val pollenAvailable = region == EnvironmentRegion.EUROPE &&
            current?.grassPollen != null

        val log = EnvironmentLog(
            latitude = lat,
            longitude = lon,
            region = region,
            // POZOR: european_aqi je null pro non-EU souřadnice (CAMS model pokrývá jen EU).
            // Ukládáme 0 jako fallback, ale tato hodnota NESMÍ být zobrazována mimo EU —
            // 0 by bylo chybně interpretováno jako "dobrá kvalita vzduchu" místo "data nejsou k dispozici".
            // UI proto filtruje zobrazení podle region == EUROPE (viz DashboardViewModel).
            aqi    = current?.europeanAqi ?: 0,
            pm25   = current?.pm25 ?: 0f,
            no2    = current?.no2 ?: 0f,
            ozone  = current?.ozone ?: 0f,
            // Pyl — nullable, null pokud není dostupný
            pollenGrassIndex = if (pollenAvailable)
                current?.grassPollen.toPollenIndex() else null,
            pollenTreeIndex = if (pollenAvailable) maxOf(
                current?.birchPollen.toPollenIndex(),
                current?.alderPollen.toPollenIndex()
            ) else null,
            pollenWeedIndex = if (pollenAvailable) maxOf(
                current?.mugwortPollen.toPollenIndex(),
                current?.ragweedPollen.toPollenIndex()
            ) else null,
            pollenAvailable = pollenAvailable
        )

        saveLog(log)
        return log
    }

    // Mapper: Entity → Domain (aktualizovaný pro nullable pyl)
    private fun EnvironmentLogEntity.toDomain() = EnvironmentLog(
        id = java.util.UUID.fromString(this.id),
        timestamp = this.timestamp,
        latitude = this.latitude,
        longitude = this.longitude,
        aqi = this.aqi,
        pm25 = this.pm25,
        no2 = this.no2,
        ozone = this.ozone,
        pollenGrassIndex = this.pollenGrassIndex,   // null zůstane null
        pollenTreeIndex = this.pollenTreeIndex,
        pollenWeedIndex = this.pollenWeedIndex,
        region = EnvironmentRegion.valueOf(this.region),
        pollenAvailable = this.pollenAvailable,
        temperature = this.temperature,
        humidity = this.humidity
    )

    // Mapper: Domain → Entity
    private fun EnvironmentLog.toEntity() = EnvironmentLogEntity(
        id = this.id.toString(),
        timestamp = this.timestamp,
        latitude = this.latitude,
        longitude = this.longitude,
        aqi = this.aqi,
        pm25 = this.pm25,
        no2 = this.no2,
        ozone = this.ozone,
        pollenGrassIndex = this.pollenGrassIndex,
        pollenTreeIndex = this.pollenTreeIndex,
        pollenWeedIndex = this.pollenWeedIndex,
        region = this.region.name,
        pollenAvailable = this.pollenAvailable,
        temperature = this.temperature,
        humidity = this.humidity
    )
}
