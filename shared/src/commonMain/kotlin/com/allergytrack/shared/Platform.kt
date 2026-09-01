package com.allergytrack.shared

/**
 * Dočasný soubor ověřující, že KMP kostra a mechanismus expect/actual fungují.
 * Fáze 3–8 sem přidají skutečné expect deklarace
 * (isDebugBuild, randomUuid, createDataStore, databaseBuilder, httpClientEngine).
 *
 * Až bude platform/ naplněné, tento soubor se smaže.
 */
expect fun platformName(): String
