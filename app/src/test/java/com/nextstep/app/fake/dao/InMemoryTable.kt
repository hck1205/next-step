package com.nextstep.app.fake.dao

import com.nextstep.app.data.local.entity.Syncable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** 메모리 DAO 의 공통 뼈대. Room 의 고정 계약(observeAll/getById/upsert/getDirty/markClean)을 그대로 흉내 냅니다. */
open class InMemoryTable<T : Syncable> {
    protected val rows = MutableStateFlow<Map<String, T>>(emptyMap())
    val all: List<T> get() = rows.value.values.toList()

    fun observeAll(familyId: String): Flow<List<T>> = rows.map { m -> m.values.filter { it.familyId == familyId && !it.deleted } }
    suspend fun getById(id: String): T? = rows.value[id]
    suspend fun upsert(item: T) { rows.value = rows.value + (item.id to item) }
    suspend fun upsertAll(items: List<T>) { rows.value = rows.value + items.associateBy { it.id } }
    suspend fun getDirty(familyId: String): List<T> = rows.value.values.filter { it.familyId == familyId && it.dirty }
    suspend fun markClean(ids: List<String>) = Unit
    fun seed(vararg items: T) { rows.value = rows.value + items.associateBy { it.id } }
}
