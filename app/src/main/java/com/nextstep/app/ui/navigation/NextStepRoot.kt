package com.nextstep.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.onboarding.OnboardingScreen

/** 앱의 시작점: 불러오는 중 → 온보딩 → 역할별 탭 화면. */
@Composable
fun NextStepRoot(rootViewModel: RootViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by rootViewModel.state.collectAsStateWithLifecycle()
    val current = state
    val caps = current?.capabilities
    when {
        current == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        !current.profile.onboarded || caps == null -> OnboardingScreen()
        else -> StudentTextScale(current.studentTextScale) {
            MainScaffold(caps = caps, studentScreen = current.studentScreen, onSwitchChild = { rootViewModel.switchChild(it) })
        }
    }
}

/** 학생 글씨 배율(해마다 다름)을 기기 글꼴 크기 위에 곱합니다. 학부모·멘토는 1. */
@Composable
private fun StudentTextScale(scale: Float, content: @Composable () -> Unit) {
    if (scale == 1f) return content()
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * scale), content = content)
}
