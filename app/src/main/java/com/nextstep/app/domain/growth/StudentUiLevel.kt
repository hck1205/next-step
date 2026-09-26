package com.nextstep.app.domain.growth

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 학생 화면의 단계. 저학년일수록 한 번에 받아들일 수 있는 정보가 적으므로 화면이 작게 시작해 학년마다 한 칸씩 자랍니다.
 *
 * - 씨앗(학령 전, 만 0~6세): 가장 큰 글씨·가장 큰 누름 영역, 할 일 2개, 별 스티커. 앉아서 공부하는 시기가 아니라 타이머가 없습니다
 * - 새싹(초1~2): 공부 시작 버튼(타이머)이 열림
 * - 떡잎(초3~4): 오늘 일정, 다시 보기(복습), 추천 영상이 열림
 * - 줄기(초5~6): 숫자 기록, 미리 보기(예습), 시험·목표, 이번 학기 배울 것, 여정 탭
 * - 가지(중1~3): 과목별 진도, 멘토 로드맵
 * - 나무(고1 이상): 여정 카드, 학습 계획 자동 배치
 *
 * 단계는 생년월일(없으면 학년)로 자동으로 정해지고, 학부모가 가족 탭에서 직접 고를 수도 있습니다([MemberEntity.uiLevel]).
 * 화면은 학년을 보지 않고 [shows]·[textScale] 같은 이 값만 읽습니다(역할 판단이 Capabilities 한 곳인 것과 같은 규칙).
 * 단계는 카드·말투·탭의 큰 틀이고, 해마다 달라지는 공부 종류·양·글씨·카드 순서는 [YearProfile]·[StudentScreen] 이 정합니다.
 */
enum class StudentUiLevel(
    val label: String,
    /** 이 단계가 시작되는 학년. 0 은 학령 전. */
    val fromGrade: Int,
    /** 글자 크기 배율. */
    val textScale: Float,
    /** "오늘 할 일"에 보이는 줄 수. */
    val taskRows: Int,
    /** 분·점수를 숫자로 보여 줄지. false 면 별과 칸으로만 보여 줍니다. */
    val showsNumbers: Boolean,
    /** 할 일 한 줄·버튼의 최소 높이(dp). */
    val touchTargetDp: Int,
    /** 하단 탭에 여정을 둘지. 어린 단계는 오늘 · 나 · 가족만. */
    val showsJourneyTab: Boolean,
    /** 기록하기(+) 시트에 보이는 항목 수(타이머 · 활동 · 할 일 · 성적 · 일정 순). */
    val recordChoices: Int,
    val words: StudentWords,
    /** 아이가 직접 쓸 때의 도움 장치(스티커판·아이용 가족·그림 기록·읽어 주기·보이는 타이머). */
    val kid: KidMode,
    /** 이 단계에서 새로 열리는 카드. 앞 단계의 카드는 그대로 남습니다. */
    val opens: Set<StudentHomeSection>,
) {
    SEED(
        "씨앗", 0, 1.4f, 2, false, 72, false, 1, StudentWords.EARLY, KidMode.EARLY,
        setOf(StudentHomeSection.TASKS, StudentHomeSection.ROUTINE, StudentHomeSection.WEEK, StudentHomeSection.YEAR),
    ),
    SPROUT(
        "새싹", 1, 1.3f, 2, false, 64, false, 2, StudentWords.EASY, KidMode.EARLY,
        setOf(StudentHomeSection.TIMER),
    ),
    SEEDLING(
        "떡잎", 3, 1.2f, 3, false, 56, false, 3, StudentWords.EASY, KidMode.MIDDLE,
        setOf(StudentHomeSection.EVENTS, StudentHomeSection.REVIEW, StudentHomeSection.RECOMMENDATION),
    ),
    STEM(
        "줄기", 5, 1.1f, 3, true, 52, true, 5, StudentWords.STANDARD, KidMode.NONE,
        setOf(StudentHomeSection.PREVIEW, StudentHomeSection.MISSION, StudentHomeSection.EXAM, StudentHomeSection.CURRICULUM),
    ),
    BRANCH(
        "가지", 7, 1.0f, 3, true, 48, true, 5, StudentWords.STANDARD, KidMode.NONE,
        setOf(StudentHomeSection.SUBJECTS, StudentHomeSection.ROADMAP),
    ),
    TREE(
        "나무", 10, 1.0f, 3, true, 48, true, 5, StudentWords.STANDARD, KidMode.NONE,
        setOf(StudentHomeSection.JOURNEY, StudentHomeSection.PLANNER),
    );

    /** 이 단계까지 열린 모든 카드. */
    val sections: Set<StudentHomeSection> by lazy { entries.take(ordinal + 1).flatMap { it.opens }.toSet() }

    fun shows(section: StudentHomeSection): Boolean = section in sections

    /** 이전 단계에서 올라왔을 때 새로 생긴 카드(화면 순서). 같거나 낮은 단계면 비어 있습니다. */
    fun newSince(previous: StudentUiLevel): List<StudentHomeSection> =
        StudentHomeSection.entries.filter { it in sections && it !in previous.sections }

    /** 설정 화면 표기. 예: "떡잎 · 초3–4". */
    val gradeSpan: String get() {
        val next = entries.getOrNull(ordinal + 1)?.fromGrade
        return when {
            fromGrade == 0 -> "학령 전"
            next == null -> "고1부터"
            else -> "${GrowthStage.fromGradeYear(fromGrade)!!.gradeLabel(fromGrade)}–${GrowthStage.fromGradeYear(next - 1)!!.gradeLabel(next - 1)}"
        }
    }

    companion object {
        /** 학년(1=초1 … 18)으로 고릅니다. 0 이하는 학령 전. */
        fun forGrade(gradeYear: Int): StudentUiLevel = entries.last { gradeYear >= it.fromGrade }

        fun fromName(name: String?): StudentUiLevel? = entries.firstOrNull { it.name == name }

        /**
         * 학생 구성원의 자동 단계. 생년월일 → 학년 순으로 판단하고, 둘 다 없으면 전체 화면(나무)을 씁니다.
         * 학령 전 아이는 씨앗, 대학원 이후는 나무입니다.
         */
        fun auto(student: MemberEntity, today: LocalDate = DateUtils.today()): StudentUiLevel = YearProfiles.of(student, today)?.level ?: TREE

        /** 실제로 쓰는 단계: 학부모가 고른 단계가 있으면 그것, 없으면 자동. */
        fun of(student: MemberEntity, today: LocalDate = DateUtils.today()): StudentUiLevel = fromName(student.uiLevel) ?: auto(student, today)
    }
}
