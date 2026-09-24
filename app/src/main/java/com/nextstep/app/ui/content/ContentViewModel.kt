package com.nextstep.app.ui.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.repository.ContentRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.content.ContentRecommender
import com.nextstep.app.domain.stats.StudyStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.ui.common.asUiState

class ContentViewModel(
    private val streams: FamilyDataStreams,
    private val contents: ContentRepository,
) : ViewModel() {
    private val filter = MutableStateFlow(ContentFilter())
    private val add = MutableStateFlow(AddContentState())

    private val base = combine(streams.subjects, streams.contents, streams.topics, streams.grades, combine(streams.events, streams.members) { e, m -> e to m }) { subjects, contents, topics, grades, (events, members) ->
        val progress = StudyStats.subjectProgress(topics, subjects)
        val level = GrowthStage.of(members)?.gradeLevel ?: GradeLevel.ALL
        ContentUiState(
            subjects = subjects, all = contents,
            subjectKeys = (subjects.map { it.name } + contents.map { it.subjectKey }).filter { it.isNotBlank() }.distinct(),
            recommendations = ContentRecommender.recommend(contents, subjects, progress, StudyStats.subjectScores(grades, subjects), StudyStats.upcomingExams(events, emptyList()), gradeLevel = level, limit = 5),
        )
    }

    val state: StateFlow<ContentUiState> = combine(base, filter, add) { s, f, a -> s.copy(filter = f, add = a, filtered = s.all.filter(f::matches)) }
        .asUiState(viewModelScope, ContentUiState())

    fun setQuery(q: String) = filter.value.let { filter.value = it.copy(query = q) }
    fun setSubject(key: String?) = filter.value.let { filter.value = it.copy(subjectKey = key) }
    fun setType(t: ContentType?) = filter.value.let { filter.value = it.copy(type = t) }
    fun setLevel(l: GradeLevel?) = filter.value.let { filter.value = it.copy(level = l) }
    fun toggleHideWatched() = filter.value.let { filter.value = it.copy(hideWatched = !it.hideWatched) }

    // ---- 등록 흐름: URL 입력 → 메타데이터·자동 분류 → 사용자가 확인·수정 → 저장
    fun setUrl(url: String) { add.value = add.value.copy(url = url, error = null) }

    fun analyze() = viewModelScope.launch {
        val url = add.value.url.trim()
        if (url.isBlank()) return@launch
        add.value = add.value.copy(loading = true, error = null)
        contents.prepare(url)
            .onSuccess { add.value = add.value.copy(loading = false, draft = it) }
            .onFailure { add.value = add.value.copy(loading = false, error = it.message ?: "링크를 분석하지 못했어요") }
    }

    fun resetAdd() { add.value = AddContentState() }

    fun save(title: String, channel: String, subjectKey: String, level: GradeLevel, type: ContentType, keywords: String, summary: String, durationMinutes: Int) = viewModelScope.launch {
        val d = add.value.draft ?: return@launch
        contents.save(
            ContentEntity(
                familyId = "", url = d.url, videoId = d.videoId, title = title.trim(), channel = channel.trim(), thumbnailUrl = d.thumbnailUrl,
                subjectKey = subjectKey.trim(), gradeLevel = level, contentType = type, keywords = keywords, summary = summary.trim(), durationMinutes = durationMinutes,
            ),
        )
        add.value = AddContentState()
    }

    fun update(content: ContentEntity) = viewModelScope.launch { contents.save(content) }
    fun rate(id: String, stars: Int) = viewModelScope.launch { contents.rate(id, stars) }
    fun setWatched(id: String, watched: Boolean) = viewModelScope.launch { contents.setWatched(id, watched) }
    fun delete(id: String) = viewModelScope.launch { contents.delete(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ContentEvent) {
        when (event) {
            is ContentEvent.SetQuery -> setQuery(event.q)
            is ContentEvent.SetSubject -> setSubject(event.key)
            is ContentEvent.SetType -> setType(event.t)
            is ContentEvent.SetLevel -> setLevel(event.l)
            ContentEvent.ToggleHideWatched -> toggleHideWatched()
            is ContentEvent.SetUrl -> setUrl(event.url)
            ContentEvent.Analyze -> analyze()
            ContentEvent.ResetAdd -> resetAdd()
            is ContentEvent.Save -> save(event.title, event.channel, event.subjectKey, event.level, event.type, event.keywords, event.summary, event.durationMinutes)
            is ContentEvent.Update -> update(event.content)
            is ContentEvent.Rate -> rate(event.id, event.stars)
            is ContentEvent.SetWatched -> setWatched(event.id, event.watched)
            is ContentEvent.Delete -> delete(event.id)
        }
    }

}
