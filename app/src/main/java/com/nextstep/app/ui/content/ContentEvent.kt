package com.nextstep.app.ui.content

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel

/** Content 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface ContentEvent {
    data class SetQuery(val q: String) : ContentEvent
    data class SetSubject(val key: String?) : ContentEvent
    data class SetType(val t: ContentType?) : ContentEvent
    data class SetLevel(val l: GradeLevel?) : ContentEvent
    data object ToggleHideWatched : ContentEvent
    data class SetUrl(val url: String) : ContentEvent
    data object Analyze : ContentEvent
    data object ResetAdd : ContentEvent
    data class Save(val title: String, val channel: String, val subjectKey: String, val level: GradeLevel, val type: ContentType, val keywords: String, val summary: String, val durationMinutes: Int) : ContentEvent
    data class Update(val content: ContentEntity) : ContentEvent
    data class Rate(val id: String, val stars: Int) : ContentEvent
    data class SetWatched(val id: String, val watched: Boolean) : ContentEvent
    data class Delete(val id: String) : ContentEvent
}
