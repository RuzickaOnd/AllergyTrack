package com.allergytrack.app.di.koin

import com.allergytrack.shared.data.remote.createAirQualityApi
import com.allergytrack.shared.data.remote.createSupabaseFoodApi
import org.koin.dsl.module

/**
 * Síťová vrstva žije celá v :shared — Ktor se sem záměrně nepropisuje.
 * Kvalifikátory named("openmeteo") / named("supabase") už nejsou potřeba,
 * protože každý klient si drží vlastní API třída.
 */
val networkModule = module {
    single { createAirQualityApi(json = get()) }
    single { createSupabaseFoodApi(json = get(), config = get()) }
}
