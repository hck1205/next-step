package com.nextstep.app.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nextstep.app.NextStepApp
import com.nextstep.app.ui.calendar.CalendarViewModel
import com.nextstep.app.ui.grades.GradesViewModel
import com.nextstep.app.ui.home.HomeViewModel
import com.nextstep.app.ui.insights.InsightsViewModel
import com.nextstep.app.ui.mentor.MentorDashboardViewModel
import com.nextstep.app.ui.navigation.RootViewModel
import com.nextstep.app.ui.onboarding.OnboardingViewModel
import com.nextstep.app.ui.parent.ParentDashboardViewModel
import com.nextstep.app.ui.progress.ProgressViewModel
import com.nextstep.app.ui.progress.SubjectDetailViewModel
import com.nextstep.app.ui.settings.SettingsViewModel
import com.nextstep.app.ui.timer.TimerViewModel

/** 모든 ViewModel 을 AppContainer 로부터 만드는 팩토리. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { RootViewModel(app().container.repository) }
        initializer { OnboardingViewModel(app().container.repository) }
        initializer { HomeViewModel(app().container.repository) }
        initializer { ParentDashboardViewModel(app().container.repository) }
        initializer { MentorDashboardViewModel(app().container.repository) }
        initializer { ProgressViewModel(app().container.repository) }
        initializer { SubjectDetailViewModel(createSavedStateHandle(), app().container.repository) }
        initializer { CalendarViewModel(app().container.repository) }
        initializer { GradesViewModel(app().container.repository) }
        initializer { InsightsViewModel(app().container.repository) }
        initializer { TimerViewModel(app().container.repository) }
        initializer { SettingsViewModel(app().container.repository) }
    }
}

private fun CreationExtras.app(): NextStepApp =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NextStepApp
