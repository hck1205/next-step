package com.nextstep.app.data.repository.room

import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.sync.SyncManager

/**
 * Room 저장소 구현체의 공통 뼈대: 가족 범위, 수정 시각, 쓰기 후 동기화 요청.
 * 각 구현체는 "무엇을 쓰는가"만 적고, "언제 올리는가"는 여기서 통일합니다.
 */
abstract class SyncedWriter(
    protected val scope: FamilyScope,
    private val sync: SyncManager,
    private val time: TimeSource,
) {
    protected fun now(): Long = time.now()

    /** 로컬 쓰기 직후 호출. 디바운스 후 서버로 밀어 올립니다. */
    protected fun pushLater() = sync.requestPush()

    /** 비어 있는 familyId 를 현재 가족으로 채웁니다. */
    protected suspend fun familyIdOr(existing: String): String = existing.ifEmpty { scope.requireFamilyId() }
}
