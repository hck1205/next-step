package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.JourneyDao
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.JourneyRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class RoomJourneyRepository(
    private val dao: JourneyDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), JourneyRepository {

    override val items: Flow<List<JourneyItemEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun setTemplateStatus(templateId: String, status: MilestoneStatus, dueDate: LocalDate) =
        modifyTemplate(templateId, dueDate) { it.copy(status = status, doneAt = if (status == MilestoneStatus.DONE) now() else null) }

    override suspend fun setTemplateNote(templateId: String, note: String, dueDate: LocalDate) =
        modifyTemplate(templateId, dueDate) { it.copy(note = note.trim()) }

    override suspend fun setTemplateDueDate(templateId: String, dueDate: LocalDate) =
        modifyTemplate(templateId, dueDate) { it.copy(dueDate = dueDate.toEpochDay()) }

    override suspend fun addCustom(title: String, description: String, category: String, dueDate: LocalDate, leadMonths: Int, priority: Int) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        dao.upsert(
            JourneyItemEntity(
                familyId = scope.requireFamilyId(), title = clean, description = description.trim(), category = category,
                dueDate = dueDate.toEpochDay(), leadMonths = leadMonths.coerceAtLeast(0), priority = priority.coerceIn(1, 3), updatedAt = now(),
            ),
        )
        pushLater()
    }

    override suspend fun update(item: JourneyItemEntity) {
        dao.upsert(item.copy(familyId = familyIdOr(item.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun setStatus(id: String, status: MilestoneStatus) {
        val item = dao.getById(id) ?: return
        update(item.copy(status = status, doneAt = if (status == MilestoneStatus.DONE) now() else null))
    }

    override suspend fun setNote(id: String, note: String) {
        val item = dao.getById(id) ?: return
        update(item.copy(note = note.trim()))
    }

    override suspend fun setDueDate(id: String, dueDate: LocalDate) {
        val item = dao.getById(id) ?: return
        update(item.copy(dueDate = dueDate.toEpochDay()))
    }

    override suspend fun delete(id: String) {
        val item = dao.getById(id) ?: return
        update(item.copy(deleted = true))
    }

    /** 카탈로그 항목의 저장 행을 찾거나 만들어서 바꿉니다. 새 행은 카탈로그 마감일을 함께 기록합니다. */
    private suspend fun modifyTemplate(templateId: String, dueDate: LocalDate, change: (JourneyItemEntity) -> JourneyItemEntity) {
        val familyId = scope.requireFamilyId()
        val current = dao.getByTemplate(familyId, templateId) ?: JourneyItemEntity(familyId = familyId, templateId = templateId, dueDate = dueDate.toEpochDay())
        dao.upsert(change(current).copy(updatedAt = now(), dirty = true))
        pushLater()
    }
}
