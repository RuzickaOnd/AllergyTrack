package com.allergytrack.shared.resources

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Hlídá formátovací pravidla pro Compose Resources napříč všemi stringy,
 * ne jen u toho jednoho plurálu, který se rozbil.
 *
 * Compose Resources není Android resources:
 *  - substituuje jen poziční tvar %N$s / %N$d, holé %d vypíše doslova
 *  - neunescapuje, takže %% a \' se vypíšou tak, jak jsou napsané
 *
 * Běží na JVM, protože commonTest nemá přístup k souborům. Runtime
 * ověření samotného dosazení dělá PluralPlaceholderTest v iosTest.
 */
class ComposeResourcesFormatTest {

    private val resourceFiles: List<File> =
        generateSequence(File(".").absoluteFile) { it.parentFile }
            .map { File(it, "shared/src/commonMain/composeResources") }
            .firstOrNull { it.isDirectory }
            ?.walkTopDown()
            ?.filter { it.isFile && it.extension == "xml" }
            ?.toList()
            .orEmpty()

    /** Pojistka — kdyby se změnilo rozložení projektu, test nesmí tiše projít naprázdno. */
    @Test
    fun testVidiSouboryZdroju() {
        assertTrue(
            resourceFiles.size >= 4,
            "Nenalezeny XML soubory v commonMain/composeResources"
        )
    }

    @Test
    fun vsechnyPlaceholderyJsouPozicni() {
        val nalezy = najdi(Regex("""%[dsf]\b"""))
        assertTrue(
            nalezy.isEmpty(),
            "Compose Resources dosazuje jen %1\$s / %1\$d, holý placeholder vypíše " +
                "doslova. Oprav:\n" + nalezy.joinToString("\n")
        )
    }

    @Test
    fun procentoNeniEskapovaneJakoVAndroidResources() {
        val nalezy = najdi(Regex("""%%"""))
        assertTrue(
            nalezy.isEmpty(),
            "V Compose Resources se procento píše jako jedno %, ne %%. Oprav:\n" +
                nalezy.joinToString("\n")
        )
    }

    @Test
    fun apostrofNeniEskapovanyJakoVAndroidResources() {
        val nalezy = najdi(Regex("""\\'"""))
        assertTrue(
            nalezy.isEmpty(),
            "V Compose Resources se apostrof píše přímo jako ', ne \\'. Oprav:\n" +
                nalezy.joinToString("\n")
        )
    }

    private fun najdi(vzor: Regex): List<String> =
        resourceFiles.flatMap { soubor ->
            soubor.readLines().withIndex()
                .filter { (_, radek) -> vzor.containsMatchIn(radek) }
                .map { (index, radek) ->
                    "  ${soubor.parentFile.name}/${soubor.name}:${index + 1}  ${radek.trim()}"
                }
        }
}
