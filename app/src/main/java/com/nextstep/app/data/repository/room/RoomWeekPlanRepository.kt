package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.WeekPlanDao
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

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
        val existing = findWeek(weekStart)
        val text = clean.joinToString("\n")
        val changed = existing == null || existing.goals != text
        val base = existing ?: newWeek(weekStart)
        dao.upsert(
            base.copy(
                goals = text, plannedMinutes = minutes, doneMask = if (changed) 0 else base.doneMask,
                approvedAt = if (changed || existing?.plannedMinutes != minutes) null else base.approvedAt,
                authorRole = myRole(), updatedAt = now(), dirty = true,
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
        val now = now()
        dao.upsert(plan.copy(approvedAt = now, updatedAt = now, dirty = true))
        pushLater()
    }

    override suspend fun reflect(weekStart: LocalDate, mood: Int, good: String, hard: String, change: String) {
        if (mood !in 1..MAX_MOOD) return
        val base = findWeek(weekStart) ?: newWeek(weekStart)
        val now = now()
        dao.upsert(
            base.copy(
                mood = mood, good = good.trim(), hard = hard.trim(), change = change.trim(),
                reflectedByRole = myRole(), reflectedAt = now, updatedAt = now, dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun saveTalk(weekStart: LocalDate, proud: String, wish: String, treat: String) {
        val base = findWeek(weekStart) ?: newWeek(weekStart)
        val now = now()
        dao.upsert(
            base.copy(
                proud = proud.trim(), wish = wish.trim(), treat = treat.trim(),
                talkByRole = myRole(), talkAt = now, updatedAt = now, dirty = true,
            ),
        )
        pushLater()
    }

    /** [day] 가 속한 주(월요일 시작)의 행. 없으면 null. */
    private suspend fun findWeek(day: LocalDate): WeekPlanEntity? = dao.findByWeek(familyIdOr(""), DateUtils.weekStart(day).toEpochDay())

    private suspend fun newWeek(day: LocalDate): WeekPlanEntity = WeekPlanEntity(familyId = familyIdOr(""), weekStart = DateUtils.weekStart(day).toEpochDay())

    private companion object {
        const val MAX_MINUTES = 3000
        const val MAX_MOOD = 3
    }
}
