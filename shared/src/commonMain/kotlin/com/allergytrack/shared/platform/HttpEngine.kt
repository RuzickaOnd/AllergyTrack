package com.allergytrack.shared.platform

import io.ktor.client.engine.HttpClientEngineFactory

/** OkHttp na Androidu, Darwin na iOS. */
expect fun httpClientEngine(): HttpClientEngineFactory<*>
