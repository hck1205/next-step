package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.RoadmapTemplateDao
import com.nextstep.app.data.local.entity.RoadmapTemplateEntity
import com.nextstep.app.data.repository.RoadmapTemplateRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.domain.roadmap.RoadmapTemplates
import com.nextstep.app.domain.roadmap.TemplateItem
import kotlinx.coroutines.flow.Flow

class RoomRoadmapTemplateRepository(private val dao: RoadmapTemplateDao, private val time: TimeSource) : RoadmapTemplateRepository {
    override val templates: Flow<List<RoadmapTemplateEntity>> = dao.observeAll()

    override suspend fun save(name: String, subjectName: String, items: List<TemplateItem>) {
        if (name.isBlank() || items.isEmpty()) return
        dao.upsert(RoadmapTemplateEntity(name = name.trim(), subjectName = subjectName, items = RoadmapTemplates.encode(items), count = items.size, createdAt = time.now()))
    }

    override suspend fun delete(id: String) = dao.delete(id)
}
