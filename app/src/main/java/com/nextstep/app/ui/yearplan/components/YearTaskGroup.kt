package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.nextstep.app.domain.year.YearDoer
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.yearplan.YearTaskView

/**
 * 한 묶음(학기 또는 앞서 가기)의 할 일을 카드 한 장에 줄로 담습니다. 안 한 줄이 위, 끝낸 줄은 "끝낸 것 n개"로 접어 둡니다.
 * 할 일마다 카드 한 장씩이던 긴 목록을 절반 넘게 줄입니다.
 */
@Composable
internal fun YearTaskGroup(
    views: List<YearTaskView>, key: String, minHeightDp: Int, showArea: Boolean, showsAllDoers: Boolean,
    onToggle: (YearTaskView) -> Unit, onOpen: (YearTaskView) -> Unit,
) {
    var showDone by rememberSaveable(key) { mutableStateOf(false) }
    val (done, left) = views.partition { it.done }
    val shown = if (showDone) left + done else left
    AppCard {
        Column {
            shown.forEachIndexed { i, v ->
                if (i > 0) HorizontalDivider()
                YearTaskRow(
                    v, minHeightDp = minHeightDp, showArea = showArea, showDoer = showsAllDoers || v.task.who != YearDoer.CHILD,
                    onToggle = { onToggle(v) }, onOpen = { onOpen(v) },
                )
            }
            if (done.isNotEmpty()) {
                TextButton(onClick = { showDone = !showDone }) { Text(if (showDone) "끝낸 것 접기" else "끝낸 것 ${done.size}개 보기") }
            }
        }
    }
}
