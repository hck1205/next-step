package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.cheer.CheerKind
import kotlinx.coroutines.flow.Flow

/** 해낸 일에 붙이는 응원(학부모 → 학생). 보낸 사람은 현재 프로필로 채웁니다. */
interface CheerRepository {
    val cheers: Flow<List<CheerEntity>>

    /** [task] 에 붙인 내 응원을 [kind] 로 바꿉니다(없으면 새로, null 이면 거둠). [fromName] 은 받는 아이에게 보일 부름(엄마·아빠 …). */
    suspend fun set(task: TaskEntity, kind: CheerKind?, fromName: String)
    /** 아이가 "고마워요"를 눌렀다고 남깁니다. */
    suspend fun markSeen(ids: List<String>)
}
