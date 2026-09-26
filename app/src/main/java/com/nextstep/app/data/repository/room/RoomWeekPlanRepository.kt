package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.WeekPlanDao
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.selfdirection.SelfDirection
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class RoomWeekPlanRepository(
    private val dao: WeekPlanDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), WeekPlanRepository {

    override val plans: Flow<List<WeekPlanEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun savePlan(weekStart: LocalDate, goals: List<String>, plannedMinutes: Int) {
        val clean = SelfDirection.cleanGoals(goals)
        val minutes = plannedMinutes.coerceIn(0, MAX_MINUTES)
        if (clean.isEmpty() && minutes == 0) return
        val week = SelfDirection.weekStart(weekStart)
        val existing = dao.findByWeek(familyIdOr(""), week.toEpochDay())
        val text = clean.joinToString("\n")
        val changed = existing == null || existing.goals != text
        val base = existing ?: WeekPlanEntity(familyId = familyIdOr(""), weekStart = week.toEpochDay())
        dao.upsert(
            base.copy(
                goals = text, plannedMinutes = minutes, doneMask = if (changed) 0 else base.doneMask,
                approvedAt = if (changed || existing?.plannedMinutes != minutes) null else base.approvedAt,
                authorRole = scope.currentProfile().role?.name ?: "", updatedAt = now(), dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun toggleGoal(planId: String, index: Int) {
        val plan = dao.getById(planId) ?: return
        if (index !in plan.goalList.indices) return
        dao.upsert(plan.copy(doneMask = plan.doneMask xor (1 shl index), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun approve(planId: String) {
        val plan = dao.getById(planId) ?: return
        if (plan.approvedAt != null) return
        dao.upsert(plan.copy(approvedAt = now(), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun reflect(weekStart: LocalDate, mood: Int, good: String, hard: String, change: String) {
        if (mood !in 1..MAX_MOOD) return
        val week = SelfDirection.weekStart(weekStart)
        val base = dao.findByWeek(familyIdOr(""), week.toEpochDay()) ?: WeekPlanEntity(familyId = familyIdOr(""), weekStart = week.toEpochDay())
        dao.upsert(
            base.copy(
                mood = mood, good = good.trim(), hard = hard.trim(), change = change.trim(),
                reflectedByRole = scope.currentProfile().role?.name ?: "", reflectedAt = now(), updatedAt = now(), dirty = true,
            ),
        )
        pushLater()
    }

    private companion object {
        const val MAX_MINUTES = 3000
        const val MAX_MOOD = 3
    }
}
