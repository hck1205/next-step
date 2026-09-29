package com.nextstep.app.domain.roadmap

/** 로드맵 템플릿의 한 줄: 제목 · 설명 · 자료, 그리고 시작일로부터 며칠째가 목표일인지([dayOffset], 없으면 기한 없음). */
data class TemplateItem(val title: String, val description: String = "", val resource: String = "", val dayOffset: Int? = null)
