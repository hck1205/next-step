package com.nextstep.app.domain.year

/**
 * 그 일을 누가 하는지. 어릴수록 부모가 해 주는 일이 대부분이고(만 0세는 전부), 학년이 오를수록 스스로 하는 일이 늘어납니다.
 * 화면은 이 값을 칩으로 보여 주어 "0세가 그림책을 읽는다" 같은 오해가 없게 합니다.
 */
enum class YearDoer(val label: String) {
    PARENT("엄마·아빠가"),
    TOGETHER("같이"),
    CHILD("스스로"),
    MENTOR("멘토가"),
}
