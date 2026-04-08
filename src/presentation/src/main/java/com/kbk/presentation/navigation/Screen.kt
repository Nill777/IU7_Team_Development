package com.kbk.presentation.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen(ROUTE_DASHBOARD)
    data object Settings : Screen(ROUTE_SETTINGS)
    data object Playground : Screen(ROUTE_PLAYGROUND)

    private companion object {
        const val ROUTE_DASHBOARD = "dashboard_screen"
        const val ROUTE_SETTINGS = "settings_screen"
        const val ROUTE_PLAYGROUND = "playground_screen"
    }
}
