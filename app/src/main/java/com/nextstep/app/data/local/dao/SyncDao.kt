package com.nextstep.app.data.local.dao

import com.nextstep.app.data.local.entity.Syncable

/**
 * 동기화 엔티티 DAO 의 고정 계약. 각 DAO 는 이 네 메서드를 @Query/@Insert 로 구현하고,
 * SyncRegistry 는 `SyncedCollection.of(mapper, dao)` 한 줄로 컬렉션을 만듭니다.
 */
interface SyncDao<T : Syncable> {
    suspend fun getById(id: String): T?
    suspend fun upsert(item: T)
    suspend fun getDirty(familyId: String): List<T>
    suspend fun markClean(ids: List<String>)
}
