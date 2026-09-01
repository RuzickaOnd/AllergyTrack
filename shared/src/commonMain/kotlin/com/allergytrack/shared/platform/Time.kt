package com.allergytrack.shared.platform

/** Aktuální čas v milisekundách od epochy. Náhrada za System.currentTimeMillis(). */
expect fun currentTimeMillis(): Long
