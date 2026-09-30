package com.nextstep.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.ui.components.layout.WindowWidth
import com.nextstep.app.ui.components.layout.rememberWindowWidth
import com.nextstep.app.ui.quickadd.QuickAddSheet

/**
 * 앱 뼈대. 폰(좁은 창)은 하단 탭 + 가운데 기록하기(+) — 탭 화면에서만 보이고 세부 화면에서는 숨습니다.
 * 넓은 창(폴더블·태블릿·가로)은 왼쪽 세로 탭(레일) 맨 위에 기록하기, 본문은 가운데 최대 폭까지([WindowWidth]).
 */
@Composable
internal fun MainScaffold(caps: Capabilities, studentScreen: StudentScreen?, onSwitchChild: (String) -> Unit) {
    val navController = rememberNavController()
    val destinations = TopLevelDestination.of(caps)
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val onTopLevel = destinations.any { currentDestination.isOn(it) }
    val width = rememberWindowWidth()
    var showQuickAdd by remember { mutableStateOf(false) }
    val quickAdd: @Composable () -> Unit = { FloatingActionButton(onClick = { showQuickAdd = true }) { Icon(Icons.Default.Add, contentDescription = "기록하기") } }

    if (width.usesRail) {
        Row(Modifier.fillMaxSize()) {
            SideRail(navController, destinations, currentDestination, header = quickAdd)
            Scaffold(modifier = Modifier.weight(1f)) { padding -> CenteredHost(navController, caps, studentScreen, onSwitchChild, Modifier.padding(padding)) }
        }
    } else {
        Scaffold(
            bottomBar = { if (onTopLevel) BottomTabs(navController, destinations, currentDestination) },
            floatingActionButton = { if (onTopLevel) quickAdd() },
            floatingActionButtonPosition = FabPosition.Center,
        ) { padding -> CenteredHost(navController, caps, studentScreen, onSwitchChild, Modifier.padding(padding)) }
    }

    if (showQuickAdd) {
        QuickAddSheet(caps = caps, studentLevel = studentScreen?.level, onDismiss = { showQuickAdd = false }, onOpenTimer = { navController.navigate(Routes.TIMER) })
    }
}

/** 본문은 넓은 창에서도 가운데 [WindowWidth.MAX_CONTENT_DP] 폭까지만: 카드가 가로로 늘어나 읽기 어려워지지 않게. */
@Composable
private fun CenteredHost(navController: NavHostController, caps: Capabilities, studentScreen: StudentScreen?, onSwitchChild: (String) -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        NextStepNavHost(
            navController = navController, caps = caps, studentScreen = studentScreen, onSwitchChild = onSwitchChild,
            modifier = Modifier.widthIn(max = WindowWidth.MAX_CONTENT_DP.dp).fillMaxHeight(),
        )
    }
}

@Composable
private fun BottomTabs(navController: NavHostController, destinations: List<TopLevelDestination>, current: NavDestination?) {
    NavigationBar {
        destinations.forEach { d ->
            NavigationBarItem(
                selected = current.isOn(d),
                onClick = { navController.openTab(d) },
                icon = { Icon(d.icon, contentDescription = d.label) },
                label = { Text(d.label) },
            )
        }
    }
}

/** 넓은 창의 왼쪽 세로 탭. 맨 위([header])에 기록하기(+), 아래로 탭 네 개. 세부 화면에서도 남아 있어 어디서든 탭을 오갑니다. */
@Composable
private fun SideRail(navController: NavHostController, destinations: List<TopLevelDestination>, current: NavDestination?, header: @Composable () -> Unit) {
    NavigationRail(header = { header() }) {
        destinations.forEach { d ->
            NavigationRailItem(
                selected = current.isOn(d),
                onClick = { navController.openTab(d) },
                icon = { Icon(d.icon, contentDescription = d.label) },
                label = { Text(d.label) },
            )
        }
    }
}

/** 탭 이동: 탭마다 스크롤·상태를 남겨 두고, 같은 탭을 다시 눌러도 쌓이지 않게. */
private fun NavHostController.openTab(d: TopLevelDestination) {
    navigate(if (d.route == Routes.RECORDS) Routes.records() else d.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavDestination?.isOn(d: TopLevelDestination): Boolean = this?.hierarchy?.any { it.route == d.route } == true
