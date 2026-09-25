package com.nextstep.app.domain.stats

/** 학습량·자기주도·경험의 균형 요약. 숫자 대신 문장으로 판단을 전달합니다. */
data class BalanceReport(
    val weekMinutes: Int,
    /** 성장 단계의 주간 권장 상한(분). 0 이면 앉아서 하는 학습을 권하지 않는 시기. */
    val recommendedWeekMinutes: Int,
    val studyVerdict: BalanceVerdict,
    /** 최근 30일 할 일 중 학생이 스스로 만든 비율. 할 일이 없으면 null. */
    val selfDirectedRatio: Float?,
    /** 이번 구간에 기록한 활동 수. */
    val experiencesThisPeriod: Int,
    val streak: Int,
    /** 이번 주(월~일) 학원·수업 일정 시간(분). 영유아기 과열 가드에 씁니다. */
    val classWeekMinutes: Int = 0,
    /** 영유아기 학원·수업 주간 상한(분). 학령기는 null(학교가 있어 학습 권장선으로 봅니다). */
    val classCapWeekMinutes: Int? = null,
    val classVerdict: BalanceVerdict = BalanceVerdict.NONE,
) {
    /** 첫 화면의 상태 문장. 비교 없이 아이의 이번 주만 말합니다. */
    val headline: String get() = when {
        classVerdict == BalanceVerdict.LESS -> "학원·수업이 많아요. 놀 시간이 남아 있는지 봐 주세요"
        recommendedWeekMinutes == 0 && studyVerdict == BalanceVerdict.LESS -> "지금은 놀이와 대화가 공부예요. 앉아서 하는 공부는 줄여도 돼요"
        recommendedWeekMinutes == 0 -> "지금은 놀이와 대화가 공부예요"
        studyVerdict == BalanceVerdict.LESS -> "이번 주는 조금 쉬어도 돼요"
        studyVerdict == BalanceVerdict.WITHIN && streak >= 3 -> "잘 가고 있어요. 흐름을 지키고 있어요"
        studyVerdict == BalanceVerdict.WITHIN -> "잘 가고 있어요"
        weekMinutes == 0 -> "이번 주는 아직 시작 전이에요"
        else -> "천천히 가고 있어요. 괜찮아요"
    }

    val studyLine: String get() = when (studyVerdict) {
        BalanceVerdict.NONE -> "앉아서 하는 학습 계획은 아직 없어요"
        BalanceVerdict.LESS -> if (recommendedWeekMinutes == 0) "이 시기엔 앉아서 하는 공부를 권하지 않아요" else "권장선을 넘었어요. 늘리기보다 줄이는 쪽이 좋아요"
        BalanceVerdict.WITHIN -> "권장선 안 · 늘릴 필요 없어요"
        BalanceVerdict.MORE -> "권장선보다 적어요 · 조금 더 해도 좋아요"
    }

    /** 학원·수업 한 줄. 영유아기가 아니면 null. */
    val classLine: String? get() = classCapWeekMinutes?.let { cap ->
        if (classVerdict == BalanceVerdict.LESS) "주 권장 ${cap / MINUTES_PER_HOUR}시간을 넘었어요. 하나를 놀이로 바꿔 보세요"
        else "주 ${cap / MINUTES_PER_HOUR}시간 안이면 충분해요. 늘릴 필요 없어요"
    }

    private companion object {
        const val MINUTES_PER_HOUR = 60
    }
}
