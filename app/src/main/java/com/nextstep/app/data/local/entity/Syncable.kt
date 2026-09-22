package com.nextstep.app.data.local.entity

import java.util.UUID

/**
 * 모든 동기화 대상 엔티티가 공유하는 필드.
 * - updatedAt: 마지막 수정 시각(epoch millis). 충돌 시 최신 값이 이깁니다.
 * - deleted: 소프트 삭제 플래그. 삭제 정보도 상대 기기에 전파돼야 하므로 바로 지우지 않습니다.
 * - dirty: 아직 서버에 올리지 못한 로컬 변경.
 */
interface Syncable {
    val id: String
    val familyId: String
    val updatedAt: Long
    val deleted: Boolean
    val dirty: Boolean
}

fun newId(): String = UUID.randomUUID().toString()
