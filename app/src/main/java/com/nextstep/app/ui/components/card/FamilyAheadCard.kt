package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.ui.components.row.FamilyEventRow
import java.time.LocalDate

/**
 * 오늘 화면의 "가족 일정": 오늘 것과, 미리 보기(며칠 전부터)에 든 다가오는 것(FamilyCalendar.ahead).
 * [compact] 면 [COMPACT_ROWS] 줄까지. 아래 "가족 달력"은 기록 › 우리 가족으로 갑니다.
 */
@Composable
fun FamilyAheadCard(items: List<FamilyOccurrence>, members: List<MemberEntity>, today: LocalDate, compact: Boolean, onOpen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.take(if (compact) COMPACT_ROWS else items.size).forEach { occ ->
            FamilyEventRow(occ, members, dayLabel = FamilyCalendar.dayLabel(occ.date, today))
        }
        if (!compact) TextButton(onClick = onOpen) { Text("가족 달력") }
    }
}

private const val COMPACT_ROWS = 2
