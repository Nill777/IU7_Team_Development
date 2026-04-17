package com.kbk.presentation.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kbk.presentation.dashboard.DashboardScreen
import com.kbk.presentation.dashboard.DashboardViewModel
import com.kbk.presentation.playground.PlaygroundScreen
import com.kbk.presentation.playground.PlaygroundViewModel
import com.kbk.presentation.settings.SettingsScreen
import com.kbk.presentation.settings.SettingsViewModel

private const val BOTTOM_BAR_HEIGHT_VAL = 72
private const val BOTTOM_BAR_PADDING_VAL = 8
private const val BOTTOM_BAR_CORNER_RADIUS_VAL = 16
private const val ANIMATION_DURATION = 200
private const val TAB_WEIGHT = 1f
private const val TAB_FONT_SIZE_VAL = 16

data class BottomBarTabsParams(
    val tabs: List<Screen>,
    val tabNames: List<String>,
    val selectedIndex: Int,
    val textColorSelected: Color,
    val textColorUnselected: Color,
    val fontSize: TextUnit
)

@Composable
fun KeystrokeApp(viewModelFactory: ViewModelProvider.Factory) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.navigationBars),
        bottomBar = {
            CustomAnimatedBottomBar(
                navController = navController,
                sliderColor = MaterialTheme.colorScheme.tertiary,
                textColorSelected = MaterialTheme.colorScheme.onTertiary,
                textColorUnselected = MaterialTheme.colorScheme.onSecondary,
                fontSize = TAB_FONT_SIZE_VAL.sp
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
                DashboardScreen(viewModel = viewModel)
            }
            composable(Screen.Playground.route) {
                val viewModel: PlaygroundViewModel = viewModel(factory = viewModelFactory)
                PlaygroundScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun CustomAnimatedBottomBar(
    navController: NavHostController,
    sliderColor: Color = MaterialTheme.colorScheme.secondary,
    textColorSelected: Color = MaterialTheme.colorScheme.onSecondary,
    textColorUnselected: Color = MaterialTheme.colorScheme.onPrimary,
    fontSize: TextUnit = TAB_FONT_SIZE_VAL.sp
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = listOf(Screen.Dashboard, Screen.Playground, Screen.Settings)
    val tabNames = listOf("Дашборд", "Тест", "Настройки")
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = ANIMATION_DURATION), // длительность анимации мс
        label = "slider_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BOTTOM_BAR_HEIGHT_VAL.dp)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(BOTTOM_BAR_PADDING_VAL.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tabWidth = maxWidth / tabs.size

            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .offset(x = tabWidth * animatedOffset)
                    .background(
                        color = sliderColor,
                        shape = RoundedCornerShape(BOTTOM_BAR_CORNER_RADIUS_VAL.dp)
                    )
            )
            // кнопки поверх slider
            BottomBarTabs(
                params = BottomBarTabsParams(
                    tabs = tabs,
                    tabNames = tabNames,
                    selectedIndex = selectedIndex,
                    textColorSelected = textColorSelected,
                    textColorUnselected = textColorUnselected,
                    fontSize = fontSize
                ),
                navController = navController
            )
        }
    }
}

@Composable
private fun BottomBarTabs(
    params: BottomBarTabsParams,
    navController: NavHostController
) {
    Row(modifier = Modifier.fillMaxSize()) {
        params.tabs.forEachIndexed { index, screen ->
            val isSelected = index == params.selectedIndex

            Box(
                modifier = Modifier
                    .weight(TAB_WEIGHT)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = params.tabNames[index],
                    color = if (isSelected) params.textColorSelected else params.textColorUnselected,
                    fontSize = params.fontSize,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
