package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

/** 활동 기록(취미·동아리·현장학습·체험 등). */
interface ActivityRepository {
    val activities: Flow<List<ActivityEntity>>

    /** 저장. familyId 가 비어 있으면 현재 가족으로, 작성자 역할은 현재 프로필로 채웁니다. 제목이 비면 무시. */
    suspend fun save(activity: ActivityEntity)
    suspend fun delete(id: String)
}
