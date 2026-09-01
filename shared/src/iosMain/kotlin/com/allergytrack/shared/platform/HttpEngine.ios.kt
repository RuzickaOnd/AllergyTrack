package com.allergytrack.shared.platform

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin

actual fun httpClientEngine(): HttpClientEngineFactory<*> = Darwin
