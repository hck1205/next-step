package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import java.time.LocalDate

/** 목표의 세부 할 일 목록. 지울 수 있는 건 아직 안 끝낸 할 일뿐입니다([canDelete]). */
@Composable
internal fun SubTaskListCard(
    tasks: List<TaskEntity>, subjects: List<SubjectEntity>, today: LocalDate, canCheck: Boolean, canDelete: Boolean,
    onToggle: (TaskEntity) -> Unit, onDelete: (TaskEntity) -> Unit,
) {
    AppCard {
        if (tasks.isEmpty()) EmptyState("세부 할 일이 없어요. 작게 나눠 넣으면 달성률이 보여요.")
        else Column {
            tasks.forEach { t ->
                SubTaskRow(
                    t, subjects, today, canCheck = canCheck, onToggle = { onToggle(t) },
                    onDelete = if (canDelete && !t.done) ({ onDelete(t) }) else null,
                )
            }
        }
    }
}
