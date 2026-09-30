package com.nextstep.app.domain.feedback

/**
 * 기록에서 찾은 사실 하나. [now]·[before] 는 종류마다 뜻이 다릅니다(공부한 날, 끝낸/전체 할 일, 스스로 정한 몫 %, 점수, 안 본 날수, 밀린 단원 수).
 * 과목에 관한 사실이면 [subjectId]·[subjectName] 이 있습니다(멘토에게는 담당 과목 것만).
 */
data class Finding(
    val kind: FeedbackKind,
    val now: Int,
    val before: Int = 0,
    val subjectId: String? = null,
    val subjectName: String = "",
)
