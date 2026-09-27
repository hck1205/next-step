package com.nextstep.app.ui.components.chart

import androidx.compose.ui.graphics.Color

/** 목표 대비 막대 한 줄: [value] 가 막대, [goal] 이 세로 선. [mark] 는 줄 앞 작은 색 표시(과목 색 등, 없으면 이름만). */
data class BulletRow(val name: String, val value: Int, val goal: Int, val valueText: String, val goalText: String, val mark: Color? = null)
