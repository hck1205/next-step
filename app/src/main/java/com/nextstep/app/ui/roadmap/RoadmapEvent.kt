package com.nextstep.app.ui.roadmap

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.RoadmapStatus
import java.time.LocalDate

/** Roadmap 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface RoadmapEvent {
    data class Save(val existing: RoadmapItemEntity?, val subjectId: String?, val title: String, val description: String, val resource: String, val targetDate: LocalDate?, val contentId: String?) : RoadmapEvent
    data class SetStatus(val id: String, val status: RoadmapStatus) : RoadmapEvent
    data class Delete(val id: String) : RoadmapEvent
    data class AddSuggestion(val subject: SubjectEntity, val title: String) : RoadmapEvent
}
