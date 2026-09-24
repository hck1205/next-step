package com.nextstep.app.ui.roadmap.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 전체 진행률과 역할별 안내 문장. */
@Composable
internal fun RoadmapSummaryCard(completion: Float, doneCount: Int, total: Int, caps: Capabilities) {
    AppCard {
        Column {
            LabeledProgress("전체 진행률", completion, MaterialTheme.colorScheme.primary, trailing = "$doneCount/$total 완료")
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    caps.canEditRoadmap -> "학생이 어떤 순서로, 어떤 자료로, 언제까지 공부할지 큐레이팅하세요. 학생 홈과 학습 계획에 그대로 반영됩니다."
                    caps.isStudent -> "멘토가 제안한 순서예요. 시작하면 '진행 중', 끝내면 '완료'로 바꿔 주세요."
                    else -> "멘토가 제안한 학습 순서와 자녀의 진행 상황이에요."
                },
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
