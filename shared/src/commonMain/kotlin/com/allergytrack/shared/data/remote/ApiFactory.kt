package com.allergytrack.shared.data.remote

import com.allergytrack.shared.config.AppConfig
import com.allergytrack.shared.data.remote.airquality.AirQualityApi
import com.allergytrack.shared.data.remote.food.SupabaseFoodApi
import com.allergytrack.shared.platform.httpClientEngine
import kotlinx.serialization.json.Json

/**
 * Továrny na API klienty.
 *
 * Existují proto, aby Ktor zůstal celý uvnitř :shared. Platformní aplikace
 * volá jen tyto funkce a s typy HttpClient ani HttpClientEngineFactory
 * vůbec nepřijde do styku.
 */

/** Open-Meteo — bez autentizace, plné logování jako v původním interceptoru. */
fun createAirQualityApi(json: Json): AirQualityApi =
    AirQualityApi(
        client = createHttpClient(
            engine = httpClientEngine(),
            json = json,
            logging = true
        )
    )

/**
 * Supabase — hlavičky apikey a Authorization přidává SupabaseFoodApi
 * u každého requestu. Logování jen v debug buildu, stejně jako dřív.
 */
fun createSupabaseFoodApi(json: Json, config: AppConfig): SupabaseFoodApi =
    SupabaseFoodApi(
        client = createHttpClient(
            engine = httpClientEngine(),
            json = json,
            logging = config.isDebug
        ),
        config = config
    )
