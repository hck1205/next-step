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

/** 학부모에게: 사실(숫자)과 챙기고 넘겨주는 사람으로서 건넬 한마디. 시키기·다그치기 대신 묻기·알아봐 주기. */
internal object ParentVoice {
    fun line(f: Finding): Pair<String, String> {
        val s = f.subjectName
        return when (f.kind) {
            STUDY_DAYS_UP -> "이번 주 ${f.now}일 공부했어요 (지난주 ${f.before}일)" to "결과보다 꾸준함을 짚어 칭찬해 주세요. \"매일 앉은 게 대단해.\""
            STUDY_STEADY -> "주 ${f.now}일, 꾸준한 흐름이에요" to "양을 늘리기보다 지금 리듬을 지켜 주는 게 좋아요."
            STUDY_DAYS_DOWN -> "공부한 날이 줄었어요 (${f.before}일 → ${f.now}일)" to "다그치기보다 이번 주가 바빴는지 먼저 물어봐 주세요."
            TASKS_WELL -> "이번 주 할 일 ${f.before}개 중 ${f.now}개를 끝냈어요" to "스스로 해낸 걸 알아봐 주세요."
            TASKS_OVERDUE -> "밀린 할 일이 ${f.now}개예요" to "하나를 같이 고르고, 나머지는 날짜를 다시 잡게 도와주세요."
            SELF_MADE_UP -> "스스로 정한 일이 늘었어요 (${f.before}% → ${f.now}%)" to "계획을 한 칸 더 맡겨 볼 때일 수 있어요. 자기주도 제안을 확인해 보세요."
            SCORE_UP -> "$s ${f.before}점 → ${f.now}점" to "무엇이 도움이 됐는지 아이에게 물어봐 주세요."
            SCORE_DOWN -> "$s ${f.before}점 → ${f.now}점" to "점수보다 어디가 헷갈렸는지 이야기해 주세요. 오답 한 번 보기면 충분해요."
            SUBJECT_GAP -> "${Josa.withObject(s)} ${f.now}일째 공부하지 않았어요" to "다가오는 시험이나 과제가 없는지 가볍게 확인해 주세요."
            REVIEW_BACKLOG -> "$s 복습이 밀린 단원 ${f.now}개" to "주말에 한 단원만 같이 봐도 부담이 줄어요."
        }
    }
}
