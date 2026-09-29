package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.FamilyEventEntity
import kotlinx.coroutines.flow.Flow

/** 가족 달력의 일정(학생·학부모가 함께 씀). 새 일정의 가족 id·만든 사람은 현재 프로필로 채웁니다. */
interface FamilyEventRepository {
    val events: Flow<List<FamilyEventEntity>>

    /** 새로 넣거나 고칩니다. 제목이 비면 무시. */
    suspend fun save(event: FamilyEventEntity)
    /** 지웁니다(소프트 삭제, 반복 일정이면 전체). */
    suspend fun delete(id: String)
}
