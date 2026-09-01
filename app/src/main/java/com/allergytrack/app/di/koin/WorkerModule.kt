package com.allergytrack.app.di.koin

import com.allergytrack.app.data.worker.EnvironmentWorker
import com.allergytrack.app.data.worker.ReminderWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

/**
 * ReminderWorker nemá vlastní závislosti (jen Context + WorkerParameters),
 * EnvironmentWorker potřebuje EnvironmentRepository.
 * Koin dodá Context a WorkerParameters sám.
 */
val workerModule = module {
    worker { EnvironmentWorker(get(), get(), environmentRepository = get()) }
    worker { ReminderWorker(get(), get()) }
}
