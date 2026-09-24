package com.nextstep.app.ui.roadmap

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.domain.stats.SubjectProgress
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.ratio

/** 로드맵 화면 상태. 진행·완료 분리와 추천은 ViewModel 이 [derive] 로 한 번 계산합니다. */
data class RoadmapUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val items: List<RoadmapItemEntity> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val topics: List<TopicEntity> = emptyList(),
    val studentName: String = "",
    val contents: List<ContentEntity> = emptyList(),
    val active: List<RoadmapItemEntity> = emptyList(),
    val done: List<RoadmapItemEntity> = emptyList(),
    val completion: Float = 0f,
    /** 멘토가 로드맵을 짤 때 참고할 추천: 복습 밀린 단원, 다음 예습 단원. */
    val suggestions: List<Pair<SubjectEntity, String>> = emptyList(),
    private val contentById: Map<String, ContentEntity> = emptyMap(),
) {
    fun contentOf(item: RoadmapItemEntity): ContentEntity? = item.contentId?.let { contentById[it] }

    companion object {
        fun derive(subjects: List<SubjectEntity>, items: List<RoadmapItemEntity>, progress: List<SubjectProgress>, topics: List<TopicEntity>, studentName: String, contents: List<ContentEntity>): RoadmapUiState {
            val done = items.filter { it.status == RoadmapStatus.DONE }
            return RoadmapUiState(
                subjects = subjects, items = items, progress = progress, topics = topics, studentName = studentName, contents = contents,
                active = items.filter { it.status != RoadmapStatus.DONE },
                done = done,
                completion = ratio(done.size, items.size),
                suggestions = progress.flatMap { p ->
                    p.reviewQueue.take(1).map { p.subject to "복습 보강: ${it.title}" } + p.previewQueue.take(1).map { p.subject to "선행 예습: ${it.title}" }
                }.take(UiDefaults.MAX_SUGGESTIONS),
                contentById = contents.associateBy { it.id },
            )
        }
    }
}
