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

/** 멘토에게: 짧은 사실과 가르치는 사람으로서의 조정(과제 양·난도·로드맵). 가족의 일은 오지 않습니다(FeedbackEngine.forAudience). */
internal object MentorVoice {
    fun line(f: Finding): Pair<String, String> {
        val s = f.subjectName
        return when (f.kind) {
            STUDY_DAYS_UP -> "주 ${f.now}일 공부 (지난주 ${f.before}일)" to "흐름이 좋아요. 과제 난도를 한 단계 올려도 돼요."
            STUDY_STEADY -> "주 ${f.now}일 꾸준히 공부" to "지금 과제 양이 맞는 편이에요."
            STUDY_DAYS_DOWN -> "공부 일수 감소 (${f.before}일 → ${f.now}일)" to "다음 과제는 짧게 나눠 주세요."
            TASKS_WELL -> "할 일 ${f.now}/${f.before} 완료" to "다음 로드맵 항목으로 넘어가도 돼요."
            TASKS_OVERDUE -> "밀린 할 일 ${f.now}개" to "새 과제보다 밀린 것 정리를 먼저 권해 주세요."
            SELF_MADE_UP -> "스스로 정한 일 ${f.now}%" to "스스로 계획하는 힘이 자라고 있어요."
            SCORE_UP -> "$s ${f.before}점 → ${f.now}점" to "효과 있던 방식을 로드맵에 남겨 두세요."
            SCORE_DOWN -> "$s ${f.before}점 → ${f.now}점" to "틀린 유형으로 복습 과제를 내 보세요."
            SUBJECT_GAP -> "$s ${f.now}일째 기록 없음" to "짧은 확인 과제로 다시 이어 주세요."
            REVIEW_BACKLOG -> "$s 복습 밀린 단원 ${f.now}개" to "복습 과제로 나눠 내 주세요."
        }
    }
}
