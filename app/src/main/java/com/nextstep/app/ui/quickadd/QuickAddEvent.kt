package com.nextstep.app.ui.quickadd

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.entry.EventDraft
import com.nextstep.app.domain.entry.GradeDraft
import com.nextstep.app.domain.growth.KidRecord
import java.time.LocalDate

/** 기록하기 시트의 사용자 의도. 각 항목은 필수 입력 3개 이하의 대화상자 하나로 끝납니다. */
sealed interface QuickAddEvent {
    data class SaveActivity(val activity: ActivityEntity) : QuickAddEvent
    data class SaveTask(val title: String, val subjectId: String?, val type: TaskType, val due: LocalDate) : QuickAddEvent
    data class SaveGrade(val draft: GradeDraft) : QuickAddEvent
    data class SaveEvent(val draft: EventDraft) : QuickAddEvent
    /** 아이용: 그림 타일 한 번으로 오늘 활동 저장. */
    data class KidRecordTap(val record: KidRecord) : QuickAddEvent
    data object ClearMessage : QuickAddEvent
}
