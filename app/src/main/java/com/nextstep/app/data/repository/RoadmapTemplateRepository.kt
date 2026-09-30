package com.nextstep.app.data.repository

import com.nextstep.app.data.local.entity.RoadmapTemplateEntity
import com.nextstep.app.domain.roadmap.TemplateItem
import kotlinx.coroutines.flow.Flow

/** 멘토 기기의 로드맵 템플릿 서랍(가족 동기화 아님). */
interface RoadmapTemplateRepository {
    val templates: Flow<List<RoadmapTemplateEntity>>
    /** 이름과 항목으로 저장합니다. 이름이 비었거나 항목이 없으면 무시. */
    suspend fun save(name: String, subjectName: String, items: List<TemplateItem>)
    suspend fun delete(id: String)
}
