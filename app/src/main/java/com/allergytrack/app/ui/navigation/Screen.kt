package com.allergytrack.app.ui.navigation

sealed class Screen(val route: String) {
    // Hlavní obrazovky (bottom nav)
    object Dashboard : Screen("dashboard")
    object FoodLog : Screen("food_log")
    object SymptomLog : Screen("symptom_log")
    object Charts : Screen("charts")
    object Analysis : Screen("analysis")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
    object DebugMenu : Screen("debug_menu")

    // Food sub-flow
    object Camera : Screen("camera/{mode}") {
        const val ARG_MODE = "mode"
        fun photo() = "camera/photo"
        fun barcode() = "camera/barcode"
    }
    object FoodConfirm : Screen("food_confirm/{foodName}/{allergens}") {
        fun createRoute(foodName: String, allergens: String) =
            "food_confirm/${foodName}/${allergens}"
    }
    object FoodManualSearch : Screen("food_manual_search?query={query}") {
        const val ARG_QUERY = "query"
        fun route(query: String = "") =
            if (query.isNotEmpty()) "food_manual_search?query=$query" else "food_manual_search"
    }
    object FoodManualEntry : Screen("food_manual_entry/{foodName}") {
        fun createRoute(foodName: String) = "food_manual_entry/$foodName"
    }
}
