package com.allergytrack.shared.data.remote.food

import com.allergytrack.shared.resources.*
import org.jetbrains.compose.resources.StringResource

/**
 * EU 14 povinných alergenů + lokální heuristika pro návrh alergenů dle názvu jídla.
 * Klíče jsou shodné s hodnotami vracenými z Open Food Facts (po removePrefix("en:") + capitalize).
 */
object FoodAllergenHelper {

    data class AllergenDefinition(val key: String, val displayRes: StringResource)

    val EU_14 = listOf(
        AllergenDefinition("Gluten",                           Res.string.allergen_gluten),
        AllergenDefinition("Milk",                             Res.string.allergen_milk),
        AllergenDefinition("Eggs",                             Res.string.allergen_eggs),
        AllergenDefinition("Fish",                             Res.string.allergen_fish),
        AllergenDefinition("Crustaceans",                      Res.string.allergen_crustaceans),
        AllergenDefinition("Peanuts",                          Res.string.allergen_peanuts),
        AllergenDefinition("Soybeans",                         Res.string.allergen_soybeans),
        AllergenDefinition("Nuts",                             Res.string.allergen_nuts),
        AllergenDefinition("Celery",                           Res.string.allergen_celery),
        AllergenDefinition("Mustard",                          Res.string.allergen_mustard),
        AllergenDefinition("Sesame-seeds",                     Res.string.allergen_sesame),
        AllergenDefinition("Sulphur-dioxide-and-sulphites",    Res.string.allergen_sulphites),
        AllergenDefinition("Lupin",                            Res.string.allergen_lupin),
        AllergenDefinition("Molluscs",                         Res.string.allergen_molluscs),
    )

    private val KEYWORD_MAP: List<Pair<List<String>, String>> = listOf(
        listOf(
            "lepek", "pšenice", "žito", "ječmen", "oves", "špalda",
            "těstoviny", "pasta", "chleba", "chléb", "rohlík", "bageta", "houska",
            "mouka", "knedlík", "krupice", "tortilla", "pivo", "perník",
            "sušenka", "koláč", "dort", "pizza", "croissant", "müsli", "cereálie",
            "wheat", "bread", "flour", "beer", "barley", "oat", "rye", "spelt",
            "biscuit", "cake", "pie", "noodle", "cracker", "pretzel", "semolina"
        ) to "Gluten",
        listOf(
            "mléko", "smetana", "máslo", "sýr", "jogurt", "tvaroh", "kefír",
            "laktóza", "parmazán", "eidam", "gouda", "brie", "camembert",
            "zmrzlina", "šlehačka", "ricotta", "mascarpone",
            "milk", "cheese", "butter", "cream", "yogurt", "whey",
            "casein", "lactose", "ghee", "paneer", "kefir"
        ) to "Milk",
        listOf(
            "vejce", "vaječný", "majonéza", "omeleta", "frittata", "quiche",
            "egg", "eggs", "mayonnaise", "meringue", "custard", "hollandaise"
        ) to "Eggs",
        listOf(
            "ryba", "losos", "tuňák", "treska", "sleď", "sardinka", "pstruh",
            "kapr", "štika", "candát", "ancovička", "platýs", "makrela",
            "fish", "salmon", "tuna", "cod", "herring", "sardine", "trout",
            "anchovy", "tilapia", "bass", "halibut", "mackerel", "carp"
        ) to "Fish",
        listOf(
            "krevety", "krab", "humr", "langusta", "rak",
            "shrimp", "prawn", "crab", "lobster", "crayfish", "langoustine"
        ) to "Crustaceans",
        listOf(
            "arašíd", "arašídový", "peanut", "groundnut"
        ) to "Peanuts",
        listOf(
            "sója", "sójový", "tofu", "tempeh", "edamame", "miso",
            "soy", "soya", "soymilk", "soybean"
        ) to "Soybeans",
        listOf(
            "ořech", "mandle", "kešu", "pistácie", "vlašský", "lískový", "pekán",
            "makadamia", "pralinky",
            "nut", "almond", "cashew", "pistachio", "walnut",
            "hazelnut", "pecan", "macadamia", "praline", "marzipan", "nougat"
        ) to "Nuts",
        listOf("celer", "celery") to "Celery",
        listOf("hořčice", "mustard") to "Mustard",
        listOf(
            "sezam", "tahini",
            "sesame", "tahini"
        ) to "Sesame-seeds",
        listOf(
            "siřičitan", "víno", "wine", "sulphite", "sulfite",
            "dried fruit", "sušené ovoce", "ocet", "vinegar"
        ) to "Sulphur-dioxide-and-sulphites",
        listOf("vlčí bob", "lupin", "lupine") to "Lupin",
        listOf(
            "měkkýš", "hlemýžď", "ústřice", "mušle", "chobotnice", "sépie",
            "mollusc", "mollusk", "oyster", "mussel", "clam", "squid",
            "octopus", "snail", "scallop"
        ) to "Molluscs",
    )

    /** Mapa klíče alergenu → string resource ID pro lokalizaci v UI. */
    val keyToDisplayRes: Map<String, StringResource> = EU_14.associate { it.key to it.displayRes }

    /** Navrhne alergeny na základě klíčových slov v názvu jídla (A: lokální heuristika). */
    fun suggestFromName(name: String): Set<String> {
        val lower = name.lowercase()
        return KEYWORD_MAP
            .filter { (keywords, _) -> keywords.any { lower.contains(it) } }
            .map { (_, allergen) -> allergen }
            .toSet()
    }
}
