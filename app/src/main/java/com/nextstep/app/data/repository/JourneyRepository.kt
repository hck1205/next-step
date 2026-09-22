package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.model.MilestoneStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/** 여정 이정표의 저장 상태. 카탈로그 항목은 templateId 로, 직접 추가 항목은 id 로 다룹니다. */
interface JourneyRepository {
    val items: Flow<List<JourneyItemEntity>>

    /** 카탈로그 항목의 상태 변경. 저장 행이 없으면 만듭니다. */
    suspend fun setTemplateStatus(templateId: String, status: MilestoneStatus, dueDate: LocalDate)
    suspend fun setTemplateNote(templateId: String, note: String, dueDate: LocalDate)
    suspend fun setTemplateDueDate(templateId: String, dueDate: LocalDate)

    suspend fun addCustom(title: String, description: String, category: String, dueDate: LocalDate, leadMonths: Int, priority: Int)
    suspend fun update(item: JourneyItemEntity)
    suspend fun setStatus(id: String, status: MilestoneStatus)
    suspend fun setNote(id: String, note: String)
    suspend fun setDueDate(id: String, dueDate: LocalDate)
    suspend fun delete(id: String)
}
