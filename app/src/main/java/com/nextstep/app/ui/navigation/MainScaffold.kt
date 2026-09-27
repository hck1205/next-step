package com.nextstep.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.ui.quickadd.QuickAddSheet

/** 하단 탭 + 가운데 기록하기(+) 버튼. 탭 화면에서만 보이고, 세부 화면에서는 숨습니다. */
@Composable
internal fun MainScaffold(caps: Capabilities, studentScreen: StudentScreen?, onSwitchChild: (String) -> Unit) {
    val navController = rememberNavController()
    val destinations = TopLevelDestination.of(caps)
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val onTopLevel = destinations.any { currentDestination.isOn(it) }
    var showQuickAdd by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { if (onTopLevel) BottomTabs(navController, destinations, currentDestination) },
        floatingActionButton = {
            if (onTopLevel) FloatingActionButton(onClick = { showQuickAdd = true }) { Icon(Icons.Default.Add, contentDescription = "기록하기") }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        NextStepNavHost(navController = navController, caps = caps, studentScreen = studentScreen, onSwitchChild = onSwitchChild, modifier = Modifier.padding(padding))
    }

    if (showQuickAdd) {
        QuickAddSheet(caps = caps, studentLevel = studentScreen?.level, onDismiss = { showQuickAdd = false }, onOpenTimer = { navController.navigate(Routes.TIMER) })
    }
}

@Composable
private fun BottomTabs(navController: NavHostController, destinations: List<TopLevelDestination>, current: NavDestination?) {
    NavigationBar {
        destinations.forEach { d ->
            NavigationBarItem(
                selected = current.isOn(d),
                onClick = {
                    navController.navigate(if (d.route == Routes.RECORDS) Routes.records() else d.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(d.icon, contentDescription = d.label) },
                label = { Text(d.label) },
            )
        }
    }
}

private fun NavDestination?.isOn(d: TopLevelDestination): Boolean = this?.hierarchy?.any { it.route == d.route } == true
