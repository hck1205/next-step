package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ProjectLogEntity
import kotlinx.coroutines.flow.Flow

/** 교육 프로젝트의 루틴 기록. 프로젝트와 단계 자체는 목표 저장소(GoalRepository)에 있습니다. */
interface ProjectRepository {
    val logs: Flow<List<ProjectLogEntity>>

    /** 루틴 한 번을 남깁니다. 분이 0 이하면 무시하고, 작성자는 현재 프로필로 채웁니다. */
    suspend fun log(goalId: String, phaseKey: String, item: String, minutes: Int, date: Long)
    /** 루틴 체크: 그 날 같은 항목 기록이 있으면 지우고(체크 해제), 없으면 [minutes]분으로 남깁니다. */
    suspend fun toggle(goalId: String, phaseKey: String, item: String, minutes: Int, date: Long)
    suspend fun delete(id: String)
}
