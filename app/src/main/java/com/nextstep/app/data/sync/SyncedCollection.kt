package com.nextstep.app.data.sync

import com.nextstep.app.data.local.dao.SyncDao
import com.nextstep.app.data.local.entity.Syncable

/**
 * 동기화 대상 컬렉션 하나를 엔티티 타입에 독립적으로 다루는 단위.
 * 원격 문서 병합(last-write-wins)과 로컬 dirty 행 전송을 담당하며, 동기화 매니저는 이 타입만 알면 됩니다.
 */
class SyncedCollection<T : Syncable>(
    val mapper: EntityMapper<T>,
    private val getLocal: suspend (id: String) -> T?,
    private val upsert: suspend (T) -> Unit,
    private val getDirty: suspend (familyId: String) -> List<T>,
    private val markClean: suspend (ids: List<String>) -> Unit,
    /** 원격 값을 로컬에 쓰기 직전 조정할 지점. 예: 기기 로컬 필드 보존. */
    private val reconcile: (remote: T, local: T?) -> T = { remote, _ -> remote },
) {
    val name: String get() = mapper.collection

    /** 원격 문서를 로컬과 비교해 더 최신일 때만 반영합니다. 파싱 실패는 false 로 건너뜁니다. */
    suspend fun mergeRemote(id: String, data: Map<String, Any?>): Boolean {
        val remote = runCatching { mapper.fromMap(id, data) }.getOrNull() ?: return false
        val local = getLocal(remote.id)
        if (local != null && remote.updatedAt <= local.updatedAt) return false
        upsert(reconcile(remote, local))
        return true
    }

    /** 미전송 행을 [batchSize] 단위로 [write] 에 넘기고, 성공한 묶음만 clean 처리합니다. */
    suspend fun pushDirty(familyId: String, batchSize: Int = DEFAULT_BATCH, write: suspend (docs: List<Pair<String, Map<String, Any?>>>) -> Unit) {
        val dirty = getDirty(familyId)
        if (dirty.isEmpty()) return
        dirty.chunked(batchSize).forEach { chunk ->
            write(chunk.map { it.id to mapper.toMap(it) })
            markClean(chunk.map { it.id })
        }
    }

    companion object {
        /** Firestore 배치 쓰기 한도(500) 아래로. */
        const val DEFAULT_BATCH = 400

        /** 가족 컬렉션: DAO 의 고정 계약(SyncDao)을 그대로 씁니다. */
        fun <T : Syncable> of(mapper: EntityMapper<T>, dao: SyncDao<T>, reconcile: (remote: T, local: T?) -> T = { remote, _ -> remote }): SyncedCollection<T> =
            SyncedCollection(mapper, dao::getById, dao::upsert, dao::getDirty, dao::markClean, reconcile)

        /** 공용 컬렉션: 받기만 하고 올리지 않습니다. */
        fun <T : Syncable> readOnly(mapper: EntityMapper<T>, getLocal: suspend (id: String) -> T?, upsert: suspend (T) -> Unit, reconcile: (remote: T, local: T?) -> T = { remote, _ -> remote }): SyncedCollection<T> =
            SyncedCollection(mapper, getLocal, upsert, getDirty = { emptyList() }, markClean = {}, reconcile = reconcile)
    }
}
