package com.nextstep.app.ui

import com.nextstep.app.ui.assignments.AssignmentsViewModel
import com.nextstep.app.ui.review.ReviewViewModel
import com.nextstep.app.ui.habits.HabitsViewModel
import com.nextstep.app.ui.yearplan.YearPlanViewModel
import com.nextstep.app.ui.kidfamily.KidFamilyViewModel
import com.nextstep.app.ui.kidme.KidMeViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nextstep.app.NextStepApp
import com.nextstep.app.di.AppContainer
import com.nextstep.app.ui.activities.ActivitiesViewModel
import com.nextstep.app.ui.calendar.CalendarViewModel
import com.nextstep.app.ui.content.ContentViewModel
import com.nextstep.app.ui.curriculum.CurriculumViewModel
import com.nextstep.app.ui.goals.GoalsViewModel
import com.nextstep.app.ui.grades.GradesViewModel
import com.nextstep.app.ui.home.HomeViewModel
import com.nextstep.app.ui.insights.InsightsViewModel
import com.nextstep.app.ui.journey.JourneyViewModel
import com.nextstep.app.ui.mentor.MentorDashboardViewModel
import com.nextstep.app.ui.navigation.RootViewModel
import com.nextstep.app.ui.onboarding.OnboardingViewModel
import com.nextstep.app.ui.parent.ParentDashboardViewModel
import com.nextstep.app.ui.progress.ProgressViewModel
import com.nextstep.app.ui.progress.SubjectDetailViewModel
import com.nextstep.app.ui.project.ProjectViewModel
import com.nextstep.app.ui.projectcatalog.ProjectCatalogViewModel
import com.nextstep.app.ui.projects.ProjectsViewModel
import com.nextstep.app.ui.quickadd.QuickAddViewModel
import com.nextstep.app.ui.growth.GrowthViewModel
import com.nextstep.app.ui.overview.OverviewViewModel
import com.nextstep.app.ui.talent.TalentViewModel
import com.nextstep.app.ui.roadmap.RoadmapViewModel
import com.nextstep.app.ui.settings.SettingsViewModel
import com.nextstep.app.ui.timer.TimerViewModel

/** 모든 ViewModel 을 AppContainer 의 인터페이스로 조립하는 팩토리. ViewModel 은 구현체를 모릅니다. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { with(container()) { RootViewModel(onboarding, members) } }
        initializer { with(container()) { OnboardingViewModel(onboarding) } }
        initializer { with(container()) { HomeViewModel(streams, tasks, topics, roadmap, contents, plans, members, projects) } }
        initializer { with(container()) { ParentDashboardViewModel(streams, tasks, projects) } }
        initializer { with(container()) { MentorDashboardViewModel(streams, members, tasks) } }
        initializer { with(container()) { ProgressViewModel(streams, subjects) } }
        initializer { with(container()) { SubjectDetailViewModel(createSavedStateHandle(), streams, subjects, topics, tasks) } }
        initializer { with(container()) { CalendarViewModel(streams, events, tasks) } }
        initializer { with(container()) { GradesViewModel(streams, grades) } }
        initializer { with(container()) { InsightsViewModel(streams, tasks) } }
        initializer { with(container()) { TimerViewModel(streams, sessions) } }
        initializer { with(container()) { SettingsViewModel(streams, onboarding, members) } }
        initializer { with(container()) { RoadmapViewModel(streams, roadmap) } }
        initializer { with(container()) { ContentViewModel(streams, contents) } }
        initializer { with(container()) { JourneyViewModel(streams, journey, members, goals, tasks) } }
        initializer { with(container()) { GoalsViewModel(streams, goals, tasks) } }
        initializer { with(container()) { ActivitiesViewModel(streams, activities) } }
        initializer { with(container()) { OverviewViewModel(streams) } }
        initializer { with(container()) { GrowthViewModel(streams, growth) } }
        initializer { with(container()) { TalentViewModel(streams, growth) } }
        initializer { with(container()) { KidMeViewModel(streams) } }
        initializer { with(container()) { YearPlanViewModel(streams, journey, tasks) } }
        initializer { with(container()) { HabitsViewModel(streams) } }
        initializer { with(container()) { ReviewViewModel(streams, tasks, topics) } }
        initializer { with(container()) { AssignmentsViewModel(streams) } }
        initializer { with(container()) { KidFamilyViewModel(streams) } }
        initializer { with(container()) { CurriculumViewModel(streams, peerCurriculum, subjects, topics, tasks, contents) } }
        initializer { with(container()) { QuickAddViewModel(streams, activities, tasks, grades, events) } }
        initializer { with(container()) { ProjectsViewModel(streams, projects) } }
        initializer { with(container()) { ProjectCatalogViewModel(streams, goals) } }
        initializer { with(container()) { ProjectViewModel(createSavedStateHandle(), streams, goals, projects) } }
    }
}

private fun CreationExtras.container(): AppContainer =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NextStepApp).container
