package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.RewardEntity
import kotlinx.coroutines.flow.Flow

/** 학부모·멘토가 약속하고 주는 보상. 약속한 사람의 역할은 현재 프로필로 채웁니다. */
interface RewardRepository {
    val rewards: Flow<List<RewardEntity>>

    /** 같은 대상에 아직 주지 않은 약속이 있으면 내용을 바꾸고, 없으면 새로 약속합니다. 빈 내용이면 무시. */
    suspend fun promise(kind: String, targetId: String, title: String)
    /** 줬다고 남깁니다(이미 준 것은 그대로). */
    suspend fun give(id: String)
    /** 약속 취소(소프트 삭제). */
    suspend fun cancel(id: String)
}
