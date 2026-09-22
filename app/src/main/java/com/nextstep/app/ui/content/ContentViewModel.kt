package com.nextstep.app.ui.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.ContentEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.repository.ContentDraft
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.ContentRecommendation
import com.nextstep.app.domain.ContentRecommender
import com.nextstep.app.domain.StudyStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ContentFilter(
    val query: String = "",
    val subjectKey: String? = null,
    val type: ContentType? = null,
    val level: GradeLevel? = null,
    val hideWatched: Boolean = false,
)

/** 링크 등록 다이얼로그 상태. */
data class AddContentState(
    val url: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val draft: ContentDraft? = null,
)

data class ContentUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val all: List<ContentEntity> = emptyList(),
    val filter: ContentFilter = ContentFilter(),
    val recommendations: List<ContentRecommendation> = emptyList(),
    val add: AddContentState = AddContentState(),
) {
    val subjectKeys: List<String> get() = (subjects.map { it.name } + all.map { it.subjectKey }).filter { it.isNotBlank() }.distinct()
    val filtered: List<ContentEntity> get() = all.filter { c ->
        (filter.subjectKey == null || c.subjectKey == filter.subjectKey) &&
            (filter.type == null || c.contentType == filter.type) &&
            (filter.level == null || c.gradeLevel == filter.level || c.gradeLevel == GradeLevel.ALL) &&
            (!filter.hideWatched || !c.watched) &&
            (filter.query.isBlank() || listOf(c.title, c.channel, c.keywords, c.summary).any { it.contains(filter.query, ignoreCase = true) })
    }
}

class ContentViewModel(private val repository: StudyRepository) : ViewModel() {
    private val filter = MutableStateFlow(ContentFilter())
    private val add = MutableStateFlow(AddContentState())

    private val base = combine(repository.subjects, repository.contents, repository.topics, repository.grades, repository.events) { subjects, contents, topics, grades, events ->
        val progress = StudyStats.subjectProgress(topics, subjects)
        ContentUiState(
            subjects = subjects, all = contents,
            recommendations = ContentRecommender.recommend(contents, subjects, progress, StudyStats.subjectScores(grades, subjects), StudyStats.upcomingExams(events, emptyList()), limit = 5),
        )
    }

    val state: StateFlow<ContentUiState> = combine(base, filter, add) { s, f, a -> s.copy(filter = f, add = a) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContentUiState())

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
        repository.prepareContent(url)
            .onSuccess { add.value = add.value.copy(loading = false, draft = it) }
            .onFailure { add.value = add.value.copy(loading = false, error = it.message ?: "링크를 분석하지 못했어요") }
    }

    fun resetAdd() { add.value = AddContentState() }

    fun save(title: String, channel: String, subjectKey: String, level: GradeLevel, type: ContentType, keywords: String, summary: String, durationMinutes: Int) = viewModelScope.launch {
        val d = add.value.draft ?: return@launch
        repository.saveContent(
            ContentEntity(
                familyId = "", url = d.url, videoId = d.videoId, title = title.trim(), channel = channel.trim(), thumbnailUrl = d.thumbnailUrl,
                subjectKey = subjectKey.trim(), gradeLevel = level, contentType = type, keywords = keywords, summary = summary.trim(), durationMinutes = durationMinutes,
            ),
        )
        add.value = AddContentState()
    }

    fun update(content: ContentEntity) = viewModelScope.launch { repository.saveContent(content) }
    fun rate(id: String, stars: Int) = viewModelScope.launch { repository.rateContent(id, stars) }
    fun setWatched(id: String, watched: Boolean) = viewModelScope.launch { repository.setContentWatched(id, watched) }
    fun delete(id: String) = viewModelScope.launch { repository.deleteContent(id) }
}
