package com.nextstep.app.ui.components.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 차트 색. 역할이 다른 색은 섞지 않습니다: [series] 는 서로 다른 것(누가 준 할 일 등, 3색까지),
 * [heat] 는 양(남색 한 색의 진하기 0~4), [muted] 는 비교용 회색, [track] 은 막대 바탕.
 * 범주 3색은 라이트(흰 바탕)·다크(#111A2E 바탕) 각각 색각 차이·밝기·대비 검사를 통과한 값입니다.
 */
data class ChartPalette(
    val series: List<Color>,
    val heat: List<Color>,
    val track: Color,
    val muted: Color,
    val grid: Color,
    val ink: Color,
) {
    /** 한 계열 차트와 강조할 하나의 색. */
    val accent: Color get() = series.first()

    companion object {
        /** 지금 테마(밝은·어두운)에 맞는 팔레트. 격자와 글자색은 테마에서 가져옵니다. */
        @Composable
        fun current(): ChartPalette {
            val cs = MaterialTheme.colorScheme
            val base = if (cs.surface.luminance() < DARK_LUMINANCE) DARK else LIGHT
            return base.copy(grid = cs.outlineVariant, ink = cs.onSurface)
        }

        private const val DARK_LUMINANCE = 0.5f

        private val LIGHT = ChartPalette(
            series = listOf(Color(0xFF4F46E5), Color(0xFFC2610C), Color(0xFF0E8A5F)),
            heat = listOf(0xFFF1F5F9, 0xFFC7CBF9, 0xFF9EA3F2, 0xFF6D68EA, 0xFF3730A3).map { Color(it) },
            track = Color(0xFFF1F5F9), muted = Color(0xFFCBD5E1), grid = Color(0xFFE2E8F0), ink = Color(0xFF0F172A),
        )

        private val DARK = ChartPalette(
            series = listOf(Color(0xFF7C83F0), Color(0xFFD9732E), Color(0xFF26A186)),
            heat = listOf(0xFF1E293B, 0xFF2B2F66, 0xFF3C3F95, 0xFF5B5FD0, 0xFFA5B4FC).map { Color(it) },
            track = Color(0xFF1E293B), muted = Color(0xFF475569), grid = Color(0xFF334155), ink = Color(0xFFE2E8F0),
        )
    }
}
