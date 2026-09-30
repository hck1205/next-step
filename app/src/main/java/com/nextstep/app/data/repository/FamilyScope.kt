package com.nextstep.app.data.repository

import com.nextstep.app.data.prefs.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/** 현재 기기가 속한 가족(학생 1명 단위)의 범위. 모든 저장소가 이 범위 안에서 읽고 씁니다. */
interface FamilyScope {
    val profile: Flow<UserProfile>
    val familyId: Flow<String?>
    suspend fun currentProfile(): UserProfile
    /** 가족이 설정되지 않았으면 [IllegalStateException]. 온보딩 전에는 쓰기가 일어나지 않아야 합니다. */
    suspend fun requireFamilyId(): String
}

/** 가족 ID 가 바뀌면 자동으로 새 쿼리로 갈아타는 목록 스트림. 가족이 없으면 빈 목록. */
@OptIn(ExperimentalCoroutinesApi::class)
fun <T> FamilyScope.scopedList(query: (familyId: String) -> Flow<List<T>>): Flow<List<T>> =
    familyId.flatMapLatest { id -> if (id == null) flowOf(emptyList()) else query(id) }
