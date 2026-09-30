package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.YearArea.CAREER
import com.nextstep.app.domain.year.YearArea.ENGLISH
import com.nextstep.app.domain.year.YearArea.EXAM
import com.nextstep.app.domain.year.YearArea.GUIDE
import com.nextstep.app.domain.year.YearArea.KOREAN
import com.nextstep.app.domain.year.YearArea.MATH
import com.nextstep.app.domain.year.YearArea.RECORD
import com.nextstep.app.domain.year.YearDoer.MENTOR
import com.nextstep.app.domain.year.YearTerm.ALL_YEAR
import com.nextstep.app.domain.year.YearTerm.FIRST
import com.nextstep.app.domain.year.YearTerm.SECOND

/**
 * 해마다 멘토(과외·학원·담임 등 학습을 돕는 어른)가 할 일: 진단 → 계획 → 피드백 → 부모와 공유.
 * 가르치는 양보다 "무엇이 막혔는지 찾는 것"을 먼저 둡니다. 학령 전에는 멘토 할 일이 없습니다.
 */
internal object MentorPlans {
    fun forYear(key: String): List<YearTask> = when (key) {
        "e1", "e2" -> LOWER + byYear[key].orEmpty()
        "e3", "e4" -> MIDDLE + byYear[key].orEmpty()
        "e5", "e6" -> UPPER + byYear[key].orEmpty()
        "m1", "m2", "m3" -> SECONDARY + byYear[key].orEmpty()
        "h1", "h2", "h3" -> HIGH + byYear[key].orEmpty()
        else -> emptyList()
    }

    private val SHARE = x(GUIDE, ALL_YEAR, "부모와 짧게 공유", "주 1회: 잘한 점 하나 · 다음 목표 하나")
    private val PRAISE = x(GUIDE, ALL_YEAR, "과정 칭찬", "\"다시 풀어 봤구나\"처럼 노력·전략을 구체적으로")

    private val LOWER = listOf(
        x(GUIDE, FIRST, "학기 초 진단", "한글 해득·수 세기 수준 확인(기초학력 진단 결과 활용)"),
        x(KOREAN, ALL_YEAR, "읽기 유창성 기록", "월 1회 1분 동안 소리 내어 읽은 글자 수"),
        x(MATH, ALL_YEAR, "연산은 정확도 먼저", "속도보다 틀린 유형 기록"),
        SHARE, PRAISE,
    )
    private val MIDDLE = listOf(
        x(GUIDE, FIRST, "학업성취도 결과 해석", "3~4월 자율평가 결과로 약한 영역 하나를 학기 목표로"),
        x(KOREAN, ALL_YEAR, "독해 전략 코칭", "문단 중심 문장 찾기 → 한 줄 요약"),
        x(MATH, ALL_YEAR, "오답 이유 한 줄", "개념 · 실수 · 문제 이해로 나누어 적게"),
        x(GUIDE, ALL_YEAR, "공부 시간 기록 보기", "앱의 주간 공부 시간으로 계획 조정"),
        x(GUIDE, SECOND, "학기 상담", "학기에 한 번 부모와 15분"),
        SHARE, PRAISE,
    )
    private val UPPER = listOf(
        x(GUIDE, FIRST, "학업성취도 결과 해석", "약한 영역 하나를 학기 목표로"),
        x(MATH, ALL_YEAR, "선행 적정성 점검", "지금 단원을 80% 이상 맞힐 때만 다음 학기로"),
        x(RECORD, ALL_YEAR, "수행평가 기준표 분석", "채점 기준을 학생 말로 바꿔 보기"),
        x(ENGLISH, ALL_YEAR, "영어 읽기 수준 점검", "학기마다 읽기 지수(AR·렉사일 등)로"),
        x(GUIDE, ALL_YEAR, "주간 계획 코칭", "학생이 먼저 쓰고 멘토가 조정"),
        x(GUIDE, SECOND, "학기 상담", "학기에 한 번 부모와 15분"),
        SHARE,
    )
    private val SECONDARY = listOf(
        x(EXAM, ALL_YEAR, "시험 뒤 오답 분석", "과목별로 개념 · 실수 · 시간 부족 나누기"),
        x(EXAM, ALL_YEAR, "시험 4주 전 계획표", "범위 확인 → 교과서 → 학습지 → 기출 순서"),
        x(GUIDE, ALL_YEAR, "공부·수면 균형 점검", "주간 공부 시간과 잠 8시간을 같이 보기"),
        x(GUIDE, FIRST, "학기 상담", "학기에 한 번 부모와 15분"),
        SHARE,
    )
    private val HIGH = listOf(
        x(EXAM, ALL_YEAR, "학력평가·모의고사 분석", "영역별 틀린 유형과 시간 배분"),
        x(RECORD, ALL_YEAR, "세특 연계 탐구 제안", "수업 내용에서 시작하는 질문 하나"),
        x(GUIDE, ALL_YEAR, "주간 계획·시간 관리", "공부 시간 기록으로 과목 배분 조정"),
        x(GUIDE, FIRST, "학기 상담", "학기에 한 번 부모와 15분"),
        SHARE,
    )

    private val byYear: Map<String, List<YearTask>> = mapOf(
        "e2" to listOf(x(MATH, SECOND, "곱셈구구 이해 확인", "외운 것보다 \"몇씩 몇 묶음\"으로 설명하게")),
        "e3" to listOf(x(MATH, FIRST, "분수 개념 점검", "그림으로 설명할 수 있는지"), x(ENGLISH, FIRST, "파닉스 점검", "처음 보는 단어 소리 내어 읽기")),
        "e4" to listOf(x(MATH, SECOND, "분수·소수 연결", "수직선에 함께 나타내기")),
        "e5" to listOf(x(MATH, FIRST, "약수·배수 → 통분 연결", "통분이 막히면 최소공배수부터 다시")),
        "e6" to listOf(x(MATH, FIRST, "비와 비율 → 중1 방정식 연결", "비율 문장제 풀이 과정 말하기"), x(GUIDE, SECOND, "중학 준비 로드맵", "과목별 공부법 한 장으로")),
        "m1" to listOf(x(CAREER, FIRST, "자유학기 탐구 주제", "관심사로 작은 탐구 하나"), x(MATH, ALL_YEAR, "정수·방정식 오답 유형", "부호 실수부터 잡기")),
        "m2" to listOf(x(MATH, SECOND, "함수 개념 점검", "표·식·그래프를 오가며 설명"), x(CAREER, SECOND, "고입 방향 첫 이야기", "관심 분야와 고교 유형")),
        "m3" to listOf(x(CAREER, FIRST, "고교 유형별 준비 조언", "지원 조건·내신 반영 확인"), x(MATH, SECOND, "적정 선행 범위", "공통수학1 개념 한 바퀴면 충분")),
        "h1" to listOf(x(EXAM, FIRST, "첫 중간고사 전략", "과목별 출제 방식 파악"), x(CAREER, SECOND, "선택과목 설계 조언", "진로 · 성취도 · 수능 개편 함께")),
        "h2" to listOf(x(RECORD, ALL_YEAR, "탐구 보고서 지도", "학기마다 한 편, 질문 → 자료 → 결론"), x(CAREER, SECOND, "수시·정시 방향 설정", "내신·모의고사 추이로")),
        "h3" to listOf(
            x(EXAM, FIRST, "6월 모평 분석", "수시 지원선과 수능 최저 가능성"), x(EXAM, SECOND, "9월 모평 뒤 최종 전략", "남은 기간 과목 배분"),
            x(CAREER, SECOND, "면접 준비", "학생부 기반 질문 연습"), x(GUIDE, ALL_YEAR, "마음·컨디션 점검", "주 1회 짧게"),
        ),
    )

    private fun x(area: YearArea, term: YearTerm, title: String, how: String) = YearTask(area, term, title, how, MENTOR)
}
