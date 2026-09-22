package com.nextstep.app.domain.growth

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.Role

/**
 * 자녀의 성장 단계. 같은 "공부"라도 시기마다 필요한 입력의 종류와 양, 어른의 역할이 다릅니다.
 * 학년(1~12) 하나로 단계를 정하고, 단계가 콘텐츠 추천 학년대·학습 계획 기본값·부모/멘토 가이드를 결정합니다.
 */
enum class GrowthStage(
    val label: String,
    val gradeRange: IntRange,
    /** 콘텐츠 저장소 필터에 대응하는 학년대. */
    val gradeLevel: GradeLevel,
    /** 한 번에 집중할 수 있는 학습 시간(분). 학습 계획 기본값. */
    val sessionMinutes: Int,
    /** 하루 권장 자습 회수. */
    val sessionsPerDay: Int,
    /** 이 시기의 핵심 한 줄. */
    val focus: String,
) {
    EARLY_ELEMENTARY("초등 저학년", 1..3, GradeLevel.ELEMENTARY, 20, 1, "공부보다 경험과 호기심. 읽기·놀이·몸으로 배우는 시기"),
    UPPER_ELEMENTARY("초등 고학년", 4..6, GradeLevel.ELEMENTARY, 30, 2, "습관과 기초. 스스로 계획하고 끝내는 경험을 쌓는 시기"),
    MIDDLE("중등", 7..9, GradeLevel.MIDDLE, 45, 2, "개념의 뼈대. 과목별 원리를 이해하고 약점을 메우는 시기"),
    HIGH("고등", 10..12, GradeLevel.HIGH, 60, 3, "전략과 자기주도. 목표에 맞춰 시간을 배분하고 깊이 파는 시기");

    /** 학년 표기. 예: 초3, 중1, 고2. */
    fun gradeLabel(gradeYear: Int): String = when (this) {
        EARLY_ELEMENTARY, UPPER_ELEMENTARY -> "초${gradeYear}"
        MIDDLE -> "중${gradeYear - 6}"
        HIGH -> "고${gradeYear - 9}"
    }

    companion object {
        const val MIN_GRADE = 1
        const val MAX_GRADE = 12

        /** 학년(1~12)으로 단계를 정합니다. 모르면(0 또는 범위 밖) null. */
        fun fromGradeYear(gradeYear: Int?): GrowthStage? = gradeYear?.let { y -> entries.firstOrNull { y in it.gradeRange } }

        /** 구성원 목록에서 학생의 학년을 읽어 단계를 정합니다. */
        fun of(members: List<MemberEntity>): GrowthStage? = fromGradeYear(members.firstOrNull { it.role == Role.STUDENT.name }?.gradeYear)

        /** 온보딩·설정에서 고를 수 있는 학년 목록과 표기. */
        fun gradeOptions(): List<Pair<Int, String>> = (MIN_GRADE..MAX_GRADE).map { y -> y to fromGradeYear(y)!!.gradeLabel(y) }
    }
}
