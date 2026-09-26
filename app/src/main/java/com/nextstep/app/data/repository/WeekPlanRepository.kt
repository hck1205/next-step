package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.WeekPlanEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** 주간 계획과 돌아보기(자기주도 한 바퀴). 주마다 한 행이며 작성자 역할은 현재 프로필로 채웁니다. */
interface WeekPlanRepository {
    val plans: Flow<List<WeekPlanEntity>>

    /** 그 주 계획을 쓰거나 고칩니다. 목표는 3개까지, 빈 계획이면 무시. 내용이 바뀌면 끝낸 표시와 확인은 지웁니다. */
    suspend fun savePlan(weekStart: LocalDate, goals: List<String>, plannedMinutes: Int)
    /** 목표 하나를 끝냄/안 끝냄으로. */
    suspend fun toggleGoal(planId: String, index: Int)
    /** 아이가 쓴 계획을 어른이 확인합니다. */
    suspend fun approve(planId: String)
    /** 그 주 돌아보기. 계획이 없던 주도 돌아볼 수 있습니다. 기분은 1~3. */
    suspend fun reflect(weekStart: LocalDate, mood: Int, good: String, hard: String, change: String)
}
