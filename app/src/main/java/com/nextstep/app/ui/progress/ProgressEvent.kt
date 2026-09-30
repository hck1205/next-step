package com.nextstep.app.ui.progress

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.SubjectEntity

/** Progress 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface ProgressEvent {
    data class AddSubject(val name: String, val color: Long, val goalMinutes: Int, val teacher: String) : ProgressEvent
    data class UpdateSubject(val subject: SubjectEntity) : ProgressEvent
    data class DeleteSubject(val id: String) : ProgressEvent
}
