package com.nextstep.app.domain.notice

/** 알림 한 장: 제목 한 줄과 본문 줄([lines], 많아야 NoticeComposer.MAX_LINES). */
data class Notice(val kind: NoticeKind, val title: String, val lines: List<String>) {
    val body: String get() = lines.joinToString("\n")
}
