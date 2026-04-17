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
                fontSize = 16.sp
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
    fontSize: TextUnit = 16.sp
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val tabs = listOf(Screen.Dashboard, Screen.Playground, Screen.Settings)
    val tabNames = listOf("Дашборд", "Тест", "Настройки")
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    val animatedOffset by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = tween(durationMillis = 200), // длительность анимации мс
        label = "slider_animation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(8.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val tabWidth = maxWidth / tabs.size

            Box(
                modifier = Modifier
                    .width(tabWidth)
                    .fillMaxHeight()
                    .offset(x = tabWidth * animatedOffset)
                    .background(color = sliderColor, shape = RoundedCornerShape(16.dp))
            )
            // кнопки поверх slider
            BottomBarTabs(
                tabs = tabs,
                tabNames = tabNames,
                selectedIndex = selectedIndex,
                navController = navController,
                textColorSelected = textColorSelected,
                textColorUnselected = textColorUnselected,
                fontSize = fontSize
            )
        }
    }
}

@Composable
private fun BottomBarTabs(
    tabs: List<Screen>,
    tabNames: List<String>,
    selectedIndex: Int,
    navController: NavHostController,
    textColorSelected: Color,
    textColorUnselected: Color,
    fontSize: TextUnit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        tabs.forEachIndexed { index, screen ->
            val isSelected = index == selectedIndex

            Box(
                modifier = Modifier
                    .weight(1f)
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
                    text = tabNames[index],
                    color = if (isSelected) textColorSelected else textColorUnselected,
                    fontSize = fontSize,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
