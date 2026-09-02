package com.allergytrack.app.di.koin

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hlídá, že po migraci z Hiltu na Koin nezůstal nikde AndroidX delegát
 * `by viewModels()`.
 *
 * KoinModuleGraphTest tuhle chybu chytit nemůže — kontroluje jen to, že
 * definice v grafu existují. `by viewModels()` sestaví ViewModel přes
 * SavedStateViewModelFactory, Koin úplně obejde a u ViewModelu s parametry
 * v konstruktoru spadne až za běhu na
 * `Cannot create an instance of class …ViewModel`.
 *
 * Přesně tohle přežilo v PremiumActivity celou migraci: obrazovka spadla
 * hned při čtení prvního StateFlow.
 */
class ViewModelDelegateConventionTest {

    private val zdrojeApp: List<File> =
        generateSequence(File(".").absoluteFile) { it.parentFile }
            .map { File(it, "app/src/main") }
            .firstOrNull { it.isDirectory }
            ?.walkTopDown()
            ?.filter { it.isFile && it.extension == "kt" }
            ?.toList()
            .orEmpty()

    /** Pojistka — kdyby se změnilo rozložení projektu, test nesmí tiše projít naprázdno. */
    @Test
    fun testVidiZdrojoveSoubory() {
        assertTrue(
            "Nenalezeny zdrojové soubory app/src/main — test by neověřoval nic",
            zdrojeApp.size > 50
        )
    }

    @Test
    fun nikdeNezustalAndroidxDelegatViewModels() {
        val hrisnici = zdrojeApp.filter { soubor ->
            val text = soubor.readText()
            "by viewModels(" in text || "androidx.activity.viewModels" in text
        }
        assertTrue(
            "Použij Koin `by viewModel()` místo AndroidX `by viewModels()`: " +
                hrisnici.map { it.name },
            hrisnici.isEmpty()
        )
    }

    @Test
    fun nikdeNezustalyAnotaceHiltu() {
        val hrisnici = zdrojeApp.filter { soubor ->
            val text = soubor.readText()
            "@AndroidEntryPoint" in text || "@HiltViewModel" in text || "dagger.hilt" in text
        }
        assertTrue(
            "Hilt byl nahrazen Koinem, zbytky anotací: " + hrisnici.map { it.name },
            hrisnici.isEmpty()
        )
    }
}
