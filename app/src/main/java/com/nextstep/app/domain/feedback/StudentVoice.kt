package com.nextstep.app.domain.feedback

import com.nextstep.app.domain.feedback.FeedbackKind.REVIEW_BACKLOG
import com.nextstep.app.domain.feedback.FeedbackKind.SCORE_DOWN
import com.nextstep.app.domain.feedback.FeedbackKind.SCORE_UP
import com.nextstep.app.domain.feedback.FeedbackKind.SELF_MADE_UP
import com.nextstep.app.domain.feedback.FeedbackKind.STUDY_DAYS_DOWN
import com.nextstep.app.domain.feedback.FeedbackKind.STUDY_DAYS_UP
import com.nextstep.app.domain.feedback.FeedbackKind.STUDY_STEADY
import com.nextstep.app.domain.feedback.FeedbackKind.SUBJECT_GAP
import com.nextstep.app.domain.feedback.FeedbackKind.TASKS_OVERDUE
import com.nextstep.app.domain.feedback.FeedbackKind.TASKS_WELL
import com.nextstep.app.domain.text.Josa

/** 학생에게: 격려와 다음 한 걸음. 숫자를 보지 않는 나이([numbers] = false)에는 숫자 없이 말합니다. 다그치지 않습니다. */
internal object StudentVoice {
    fun line(f: Finding, numbers: Boolean): Pair<String, String> {
        val s = f.subjectName
        return when (f.kind) {
            STUDY_DAYS_UP -> (if (numbers) "이번 주 ${f.now}일 공부했어요" else "이번 주에 더 자주 공부했어요") to "지난주보다 늘었어요. 이 흐름 그대로 가요!"
            STUDY_STEADY -> "꾸준히 하고 있어요" to "같은 시간에 시작하는 습관이 생기고 있어요."
            STUDY_DAYS_DOWN -> "이번 주는 공부한 날이 적었어요" to "괜찮아요. 내일은 10분만 해 볼까요?"
            TASKS_WELL -> "할 일을 잘 끝냈어요" to (if (numbers) "이번 주 할 일 ${f.before}개 중 ${f.now}개를 끝냈어요." else "이번 주 할 일을 거의 다 했어요.")
            TASKS_OVERDUE -> (if (numbers) "밀린 할 일이 ${f.now}개 있어요" else "밀린 할 일이 조금 쌓였어요") to "가장 쉬운 것 하나부터 끝내면 금방 줄어요."
            SELF_MADE_UP -> "스스로 정한 일이 늘었어요" to (if (numbers) "끝낸 일 중 ${f.now}%를 내가 정했어요." else "내가 정한 일을 해냈어요. 멋져요!")
            SCORE_UP -> "$s 점수가 올랐어요" to "어떻게 공부했는지 기억해 두면 다음에도 쓸 수 있어요."
            SCORE_DOWN -> "${Josa.withTopic(s)} 한 번 더 보면 좋아요" to "틀린 문제를 다시 풀어 보면 다음엔 달라져요."
            SUBJECT_GAP -> "${Josa.withObject(s)} 한동안 안 봤어요" to "오늘 10분만 펼쳐 봐요."
            REVIEW_BACKLOG -> "$s 복습할 단원이 쌓였어요" to "하루 한 단원씩이면 금방 끝나요."
        }
    }
}
