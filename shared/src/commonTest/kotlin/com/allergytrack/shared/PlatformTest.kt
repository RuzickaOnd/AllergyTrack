package com.allergytrack.shared

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformTest {

    @Test
    fun platformNameNeniPrazdne() {
        assertTrue(platformName().isNotEmpty())
    }
}
