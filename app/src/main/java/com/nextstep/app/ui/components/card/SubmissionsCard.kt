package com.nextstep.app.ui.components.card

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.stats.Submissions

/** 낸 과제의 상태 몫: 끝냄 · 기한 전 · 밀림(쌓은 막대 + 이름과 몫). 멘토 오늘 화면의 카드. */
@Composable
fun SubmissionsCard(s: Submissions) {
    AppCard { SubmissionsBar(s) }
}
