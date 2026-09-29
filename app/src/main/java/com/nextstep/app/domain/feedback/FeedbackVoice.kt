package com.nextstep.app.domain.feedback

/**
 * 같은 사실([Finding])을 듣는 사람의 말로 바꿉니다. 사실은 한 벌이라 학생·학부모·멘토가 같은 이야기를 각자의 자리에서 합니다.
 * [numbers] 는 학생 화면이 숫자를 보는 나이인지(StudentUiLevel.showsNumbers). 어른에게는 늘 숫자로.
 */
object FeedbackVoice {
    fun line(f: Finding, audience: FeedbackAudience, numbers: Boolean = true): FeedbackLine {
        val (title, detail) = when (audience) {
            FeedbackAudience.STUDENT -> StudentVoice.line(f, numbers)
            FeedbackAudience.PARENT -> ParentVoice.line(f)
            FeedbackAudience.MENTOR -> MentorVoice.line(f)
        }
        return FeedbackLine(title, detail, f.kind.good)
    }

    /** 보는 사람의 피드백 줄([FeedbackEngine.forAudience] 로 고른 것). */
    fun lines(findings: List<Finding>, audience: FeedbackAudience, mentorSubjects: Set<String>? = null, numbers: Boolean = true): List<FeedbackLine> =
        FeedbackEngine.forAudience(findings, audience, mentorSubjects).map { line(it, audience, numbers) }
}
