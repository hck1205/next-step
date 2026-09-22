package com.nextstep.app.ui.progress

import androidx.lifecycle.ViewModel
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus

/** SubjectDetail 화면의 사용자 의도. Content 는 이 이벤트만 내보내고 ViewModel 이 처리합니다. */
sealed interface SubjectDetailEvent {
    data class AddTopics(val raw: String) : SubjectDetailEvent
    data class SetStatus(val topic: TopicEntity, val status: TopicStatus) : SubjectDetailEvent
    data class SetConfidence(val topic: TopicEntity, val value: Int) : SubjectDetailEvent
    data class Rename(val topic: TopicEntity, val title: String) : SubjectDetailEvent
    data class Delete(val topic: TopicEntity) : SubjectDetailEvent
    data class SetClassProgress(val upToOrderIndex: Int) : SubjectDetailEvent
    data class AddTask(val topic: TopicEntity, val type: TaskType, val createdByRole: String) : SubjectDetailEvent
    data class UpdateSubject(val subject: SubjectEntity) : SubjectDetailEvent
}
