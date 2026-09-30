package com.nextstep.app.data.repository

import com.nextstep.app.domain.project.ProjectViewer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * 교육 프로젝트는 만든 사람과 학생만 봅니다([ProjectViewer.canSee]). 보이지 않는 프로젝트는 목표·단계·루틴 기록에서 함께 빠지므로
 * 기록 탭·오늘 루틴·한눈에·게임 계산이 모두 같은 범위를 봅니다. 프로젝트가 아닌 목표와 다른 기록은 [family] 그대로입니다.
 */
class ProjectScopedStreams(private val family: FamilyDataStreams) : FamilyDataStreams by family {
    private val hidden: Flow<Set<String>> = combine(family.profile.map { ProjectViewer(it.role, it.memberId) }, family.goals) { viewer, goals ->
        goals.filterNot(viewer::canSee).map { it.id }.toSet()
    }

    override val goals = combine(hidden, family.goals) { h, all -> all.filter { it.id !in h } }
    override val goalSteps = combine(hidden, family.goalSteps) { h, all -> all.filter { it.goalId !in h } }
    override val projectLogs = combine(hidden, family.projectLogs) { h, all -> all.filter { it.goalId !in h } }
}
