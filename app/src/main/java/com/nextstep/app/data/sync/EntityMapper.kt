package com.nextstep.app.data.sync

import com.nextstep.app.data.local.entity.Syncable

/**
 * 엔티티 <-> Firestore 문서(Map) 변환 계약. 엔티티 하나당 구현 하나를 `mapper/` 에 둡니다.
 * 리플렉션 대신 명시적 매핑을 써서 난독화·스키마 변경에 안전하게 합니다. `dirty` 는 기기 로컬 상태라 올리지 않습니다.
 */
interface EntityMapper<T : Syncable> {
    /** Firestore 하위 컬렉션 이름. */
    val collection: String
    fun toMap(entity: T): Map<String, Any?>
    fun fromMap(id: String, data: Map<String, Any?>): T
}
