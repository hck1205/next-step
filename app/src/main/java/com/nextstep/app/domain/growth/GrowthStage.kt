package com.nextstep.app.domain.growth

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.model.Role
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 신생아부터 대학원생까지의 성장 단계. 같은 "배움"이라도 시기마다 필요한 입력의 종류와 어른의 역할이 다릅니다.
 *
 * 판정 우선순위: 생년월일(정확) → 학년(대략). 학령기는 한국 학제(출생연도 + 7년 3월 초등 입학)를 따릅니다.
 * 단계는 콘텐츠 추천 학년대, 학습 계획 기본값, 여정(이정표) 카탈로그, 부모·멘토 가이드를 결정합니다.
 */
enum class GrowthStage(
    val label: String,
    /** 학령 전·후 단계의 나이 범위(개월). 학령기는 학년으로 정하므로 비어 있습니다. */
    val ageMonths: IntRange,
    /** 학년 범위(1=초1 … 12=고3, 13~16=대학, 17~18=대학원). 학령 전 단계는 비어 있습니다. */
    val gradeRange: IntRange,
    val gradeLevel: GradeLevel,
    /** 한 번에 집중할 수 있는 학습 시간(분). 0 이면 앉아서 하는 학습을 권하지 않는 시기. */
    val sessionMinutes: Int,
    val sessionsPerDay: Int,
    val focus: String,
) {
    NEWBORN("영아", 0..11, IntRange.EMPTY, GradeLevel.ALL, 0, 0, "애착과 감각. 잘 먹고 잘 자고, 눈 맞추고 말 걸어 주는 시기"),
    TODDLER("유아", 12..35, IntRange.EMPTY, GradeLevel.ALL, 0, 0, "말이 트이는 시기. 모국어 대화와 그림책, 몸 놀이가 전부"),
    PRESCHOOL("유치원기", 36..95, IntRange.EMPTY, GradeLevel.ELEMENTARY, 15, 1, "언어 민감기. 놀이로 배우고, 외국어 소리에 자연스럽게 노출되는 시기"),
    EARLY_ELEMENTARY("초등 저학년", IntRange.EMPTY, 1..3, GradeLevel.ELEMENTARY, 20, 1, "공부보다 경험과 호기심. 읽기·놀이·몸으로 배우는 시기"),
    UPPER_ELEMENTARY("초등 고학년", IntRange.EMPTY, 4..6, GradeLevel.ELEMENTARY, 30, 2, "습관과 기초. 스스로 계획하고 끝내는 경험을 쌓는 시기"),
    MIDDLE("중등", IntRange.EMPTY, 7..9, GradeLevel.MIDDLE, 45, 2, "개념의 뼈대. 과목별 원리를 이해하고 약점을 메우는 시기"),
    HIGH("고등", IntRange.EMPTY, 10..12, GradeLevel.HIGH, 60, 3, "전략과 자기주도. 목표에 맞춰 시간을 배분하고 깊이 파는 시기"),
    UNIVERSITY("대학생", IntRange.EMPTY, 13..16, GradeLevel.HIGH, 90, 2, "탐색과 증명. 전공·경험·사람을 넓히고 자기 결과물을 만드는 시기"),
    GRADUATE("대학원생", IntRange.EMPTY, 17..18, GradeLevel.HIGH, 120, 2, "깊이와 산출. 한 문제를 오래 붙들고 글로 남기는 시기");

    val isSchoolAge: Boolean get() = !gradeRange.isEmpty()

    /** 학년 표기. 예: 초3, 중1, 고2, 대2, 석·박1. */
    fun gradeLabel(gradeYear: Int): String = when (this) {
        EARLY_ELEMENTARY, UPPER_ELEMENTARY -> "초$gradeYear"
        MIDDLE -> "중${gradeYear - 6}"
        HIGH -> "고${gradeYear - 9}"
        UNIVERSITY -> "대${gradeYear - 12}"
        GRADUATE -> "대학원 ${gradeYear - 16}년차"
        else -> label
    }

    companion object {
        const val MIN_GRADE = 1
        const val MAX_GRADE = 18

        /** 초등 입학 연도 = 출생연도 + 7 (그 해 3월). */
        const val ELEMENTARY_ENTRY_YEARS_AFTER_BIRTH = 7
        private const val SCHOOL_YEAR_START_MONTH = 3

        fun fromGradeYear(gradeYear: Int?): GrowthStage? = gradeYear?.let { y -> entries.firstOrNull { y in it.gradeRange } }

        fun fromAgeMonths(months: Int): GrowthStage? = entries.firstOrNull { months in it.ageMonths }

        /**
         * 생년월일로 단계를 정합니다. 학령기는 학제(3월 시작)로, 그 전은 개월 수로 판정합니다.
         * 대학·대학원은 재학을 가정한 값이라 실제와 다르면 학년을 직접 입력해 덮어씁니다.
         */
        fun fromBirthDate(birthDate: LocalDate, today: LocalDate): GrowthStage? {
            schoolGradeYear(birthDate, today)?.let { return fromGradeYear(it) }
            val months = ChronoUnit.MONTHS.between(birthDate, today).toInt()
            return if (months < 0) null else fromAgeMonths(months)
        }

        /** 오늘 기준 학년(1~18). 학령 전이면 null, 대학원 이후도 null. */
        fun schoolGradeYear(birthDate: LocalDate, today: LocalDate): Int? {
            val schoolYear = if (today.monthValue >= SCHOOL_YEAR_START_MONTH) today.year else today.year - 1
            val grade = schoolYear - (birthDate.year + ELEMENTARY_ENTRY_YEARS_AFTER_BIRTH) + 1
            return grade.takeIf { it in MIN_GRADE..MAX_GRADE }
        }

        /** "만 3세 4개월" 형식. 태어나기 전이면 "출생 전". */
        fun ageLabel(birthDate: LocalDate, today: LocalDate): String {
            val months = ChronoUnit.MONTHS.between(birthDate, today).toInt()
            if (months < 0) return "출생 전"
            return "만 ${months / 12}세 ${months % 12}개월"
        }

        /** 구성원의 학생 행에서 단계를 읽습니다. 생년월일이 있으면 그것을, 없으면 학년을 씁니다. */
        fun of(members: List<MemberEntity>, today: LocalDate = LocalDate.now()): GrowthStage? {
            val student = members.firstOrNull { it.role == Role.STUDENT.name } ?: return null
            student.birthDate?.let { return fromBirthDate(LocalDate.ofEpochDay(it), today) }
            return fromGradeYear(student.gradeYear.takeIf { it > 0 })
        }

        /** 온보딩·설정에서 고를 수 있는 학년 목록과 표기 (학령기 + 대학·대학원). */
        fun gradeOptions(): List<Pair<Int, String>> = (MIN_GRADE..MAX_GRADE).map { y -> y to fromGradeYear(y)!!.gradeLabel(y) }
    }
}
