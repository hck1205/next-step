package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.reward.RewardKind
import com.nextstep.app.domain.reward.RewardView
import com.nextstep.app.ui.components.row.RewardRow

/**
 * 오늘 화면(학부모·멘토): 아이가 이뤄서 받을 차례가 된 보상. "줬어요"를 누르면 받음으로 남고 아이 화면에도 보입니다.
 * 약속을 지키는 것이 보상의 전부라, 받을 차례가 있을 때만 나타납니다.
 */
@Composable
fun RewardDueCard(due: List<RewardView>, onGive: (String) -> Unit, onOpenGoal: (String) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("약속한 보상을 줄 때예요", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            due.forEach { v ->
                RewardRow(v, onGive = { onGive(v.reward.id) }, onOpen = if (v.kind == RewardKind.GOAL) ({ onOpenGoal(v.reward.targetId) }) else null)
            }
        }
    }
}
