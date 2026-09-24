package com.nextstep.app.domain.mission

import com.nextstep.app.domain.journey.GoalArea
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * 시험·수행평가·동아리·수능·수시를 이루기 위한 세부 단계 설계. 날짜는 목표일에서 거꾸로 계산합니다.
 * 교육 현장의 일반적인 준비 순서를 따르며, 가족이 단계를 더하거나 건너뛸 수 있습니다.
 */
object MissionCatalog {
    private fun s(daysBefore: Int, title: String, detail: String = "") = MissionStepTemplate(daysBefore, title, detail)

    val templates: Map<MissionKind, MissionTemplate> = listOf(
        MissionTemplate(MissionKind.UNIT_TEST, GoalArea.EXAM, listOf(
            s(5, "단원 범위 확인", "교과서 쪽수와 익힘책"),
            s(3, "교과서 문제 다시 풀기"),
            s(1, "틀린 문제 한 번 더"),
            s(-1, "결과 보고 잘한 점 한 가지 말하기"),
        )),
        MissionTemplate(MissionKind.EXAM, GoalArea.EXAM, listOf(
            s(28, "시험 범위·일정 확인", "과목별 범위표 만들기"),
            s(25, "과목별 공부 계획표", "하루 두 과목까지"),
            s(21, "1회독: 교과서·필기 개념 정리"),
            s(14, "2회독: 학교 프린트·문제집"),
            s(10, "기출 문제 풀이"),
            s(7, "오답 노트 정리"),
            s(4, "암기 과목 최종 점검"),
            s(2, "실전 모의 1회", "시간 재고 풀기"),
            s(1, "요약 한 장 복습, 일찍 자기"),
            s(-3, "결과 기록·오답 분석", "다음 시험 계획에 반영"),
        )),
        MissionTemplate(MissionKind.PERFORMANCE, GoalArea.PERFORMANCE, listOf(
            s(14, "평가 기준표(루브릭) 받기"),
            s(12, "주제 정하고 선생님께 확인"),
            s(9, "자료 조사·출처 정리"),
            s(6, "초안 작성"),
            s(3, "피드백 받고 고치기"),
            s(1, "발표 연습·최종 점검"),
            s(0, "제출·발표"),
        )),
        MissionTemplate(MissionKind.CLUB, GoalArea.CLUB, listOf(
            s(270, "동아리 고르기·가입", "진로와 이어지는 곳 우선"),
            s(240, "연간 활동 계획 세우기", "내 역할 정하기"),
            s(180, "1학기 활동 기록", "한 일·배운 점·역할"),
            s(120, "탐구 주제 정하기", "진로와 연결"),
            s(60, "결과물 만들기", "보고서·발표·작품"),
            s(21, "활동 기록 정리", "학생부 기재 요청 준비"),
            s(0, "활동 마무리·기재 확인"),
        )),
        MissionTemplate(MissionKind.CSAT, GoalArea.ADMISSION, listOf(
            s(300, "영역별 기본서 1회독"),
            s(240, "3월 학력평가 분석", "약점 영역 기록"),
            s(165, "6월 모의평가 분석", "수시·정시 방향 정하기"),
            s(120, "여름방학 약점 집중"),
            s(70, "9월 모의평가 분석", "수시 최저 기준 점검"),
            s(45, "EBS 연계 교재 마무리"),
            s(21, "실전 모의 주 3회", "시험 시간표대로"),
            s(7, "수험표·준비물·시험장 확인"),
            s(1, "컨디션 관리, 일찍 자기"),
            s(0, "수능"),
        )),
        MissionTemplate(MissionKind.EARLY_ADMISSION, GoalArea.ADMISSION, listOf(
            s(180, "희망 대학·학과 후보 정하기"),
            s(150, "전형 요강 확인", "최저 기준·반영 과목"),
            s(120, "학생부 점검", "세특·동아리·진로 활동"),
            s(90, "탐구 활동 보완"),
            s(60, "지원 6곳 확정", "상향·적정·안정"),
            s(30, "서류·자기소개 준비"),
            s(7, "원서 최종 확인"),
            s(0, "원서 접수"),
            s(-45, "면접 준비", "서류 통과 대학"),
        )),
    ).associateBy { it.kind }

    fun of(kind: MissionKind): MissionTemplate = templates.getValue(kind)

    /**
     * 날짜를 고를 때 처음 보여 줄 값. 수능은 11월 둘째 목요일, 수시는 9월 둘째 월요일, 동아리는 12월 20일,
     * 나머지는 준비 기간만큼 뒤. 이미 지났으면 다음 해. 실제 일정에 맞게 가족이 고칩니다.
     */
    fun suggestedDate(kind: MissionKind, today: LocalDate): LocalDate {
        fun yearly(build: (Int) -> LocalDate): LocalDate = build(today.year).let { if (it.isBefore(today)) build(today.year + 1) else it }
        return when (kind) {
            MissionKind.CSAT -> yearly { LocalDate.of(it, 11, 1).with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.THURSDAY)) }
            MissionKind.EARLY_ADMISSION -> yearly { LocalDate.of(it, 9, 1).with(TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY)) }
            MissionKind.CLUB -> yearly { LocalDate.of(it, 12, 20) }
            else -> today.plusDays(of(kind).spanDays.toLong())
        }
    }
}
