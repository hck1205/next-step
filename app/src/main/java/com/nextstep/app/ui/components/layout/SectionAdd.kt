package com.nextstep.app.ui.components.layout

/** 섹션의 "만들기" 한 가지(목표 만들기 · 성적 추가 …). 기록 탭에서는 상단 바 오른쪽에 올라갑니다. */
data class SectionAdd(val label: String, val onClick: () -> Unit)
