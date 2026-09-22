package com.nextstep.app.ui.roadmap

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.domain.stats.SubjectProgress

data class RoadmapUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val items: List<RoadmapItemEntity> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val topics: List<TopicEntity> = emptyList(),
    val studentName: String = "",
    val contents: List<ContentEntity> = emptyList(),
) {
    fun contentOf(item: RoadmapItemEntity): ContentEntity? = item.contentId?.let { id -> contents.firstOrNull { it.id == id } }
    val active: List<RoadmapItemEntity> get() = items.filter { it.status != RoadmapStatus.DONE }
    val done: List<RoadmapItemEntity> get() = items.filter { it.status == RoadmapStatus.DONE }
    val completion: Float get() = if (items.isEmpty()) 0f else done.size.toFloat() / items.size

    /** 멘토가 로드맵을 짤 때 참고할 추천: 복습 밀린 단원, 다음 예습 단원. */
    val suggestions: List<Pair<SubjectEntity, String>> get() = progress.flatMap { p ->
        p.reviewQueue.take(1).map { p.subject to "복습 보강: ${it.title}" } + p.previewQueue.take(1).map { p.subject to "선행 예습: ${it.title}" }
    }
}
