package com.allergytrack.shared.platform

/**
 * Absolutní cesta k souboru DataStore, včetně přípony .preferences_pb.
 *
 * Na Androidu MUSÍ odpovídat cestě, kterou používal delegát
 * preferencesDataStore(name), jinak by uživatelé přišli o všechna nastavení.
 * Ověřeno na zařízení: files/datastore/<name>.preferences_pb
 */
expect fun dataStorePath(fileName: String): String
