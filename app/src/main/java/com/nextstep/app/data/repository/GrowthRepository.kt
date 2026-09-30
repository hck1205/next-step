package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.local.entity.ObservationEntity
import kotlinx.coroutines.flow.Flow

/** 성장 기록(키·몸무게·시력)과 소질 관찰 메모. */
interface GrowthRepository {
    val records: Flow<List<GrowthRecordEntity>>
    val observations: Flow<List<ObservationEntity>>

    /** 값이 전부 비어 있으면 무시. familyId 가 비어 있으면 현재 가족으로 채웁니다. */
    suspend fun saveRecord(record: GrowthRecordEntity)
    suspend fun deleteRecord(id: String)
    /** 본문이 비면 무시. 작성자는 현재 프로필로 채웁니다. 강도는 1~3 으로 잘라 냅니다. */
    suspend fun addObservation(observation: ObservationEntity)
    suspend fun deleteObservation(id: String)
}
