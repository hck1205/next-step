package com.nextstep.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextstep.app.domain.access.Capabilities

/** 하단 탭 하나. */
data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector) {
    companion object {
        /**
         * 모든 역할이 같은 뼈대를 씁니다: 오늘 · 여정 · 기록(학생은 "나") · 가족, 가운데 + 는 기록하기 시트.
         * 화면마다 질문 하나("지금 뭐 하지?", "다음은?", "어떻게 하고 있지?", "누구와?")에만 답하고, 새 기능은 탭이 아니라 카드·항목·세그먼트로 들어갑니다.
         */
        fun of(caps: Capabilities): List<TopLevelDestination> = listOf(
            TopLevelDestination(Routes.HOME, "오늘", Icons.Default.WbSunny),
            // 학생은 여러 해 타임라인(여정) 대신 "올해": 올해 할 일을 분류 탭으로 잘게 나눈 목록. 여정은 올해 화면 위 버튼으로.
            if (caps.isStudent) TopLevelDestination(Routes.YEAR, "올해", Icons.Default.Checklist) else TopLevelDestination(Routes.JOURNEY, "여정", Icons.Default.Timeline),
            TopLevelDestination(Routes.RECORDS, if (caps.isStudent) "나" else "기록", Icons.Default.BarChart),
            TopLevelDestination(Routes.FAMILY, "가족", Icons.Default.Group),
        )
    }
}
