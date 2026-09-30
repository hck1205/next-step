package com.nextstep.app.ui.home.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.nextstep.app.domain.planner.StudyPlan
import com.nextstep.app.domain.time.DateUtils

/** 학습 계획을 만든 뒤 결과 한 줄. 비어 있으면 왜 비었는지 안내. */
@Composable
internal fun PlanResultDialog(plan: StudyPlan, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (plan.isEmpty) "배치할 항목이 없어요" else "학습 계획 완성") },
        text = {
            Text(
                if (plan.isEmpty) "복습·예습할 단원이나 로드맵 항목이 없거나, 빈 시간이 없어요. 커리큘럼에서 단원과 학급 진도를 등록해 보세요."
                else {
                    val first = plan.events.first()
                    "${plan.events.size}개의 자습 일정과 할 일을 캘린더에 넣었어요. 첫 일정: ${DateUtils.formatDate(DateUtils.toLocalDate(first.startAt))} ${DateUtils.formatTime(first.startAt)} ${first.title}"
                },
            )
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("확인") } },
    )
}
