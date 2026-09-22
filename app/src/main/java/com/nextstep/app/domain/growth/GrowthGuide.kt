package com.nextstep.app.domain.growth

import com.nextstep.app.domain.planner.PlanOptions
import java.time.LocalDate
import java.time.LocalTime

/**
 * 성장 단계별 가이드 원천. 어릴수록 경험·습관·정서에, 클수록 개념·전략·자기주도에 무게를 둡니다.
 * 텍스트는 제품 원칙("감시가 아니라 역할 분담", "결과보다 과정")과 같은 톤을 유지합니다.
 */
object GrowthGuide {

    fun forStage(stage: GrowthStage): StageGuide = when (stage) {
        GrowthStage.EARLY_ELEMENTARY -> StageGuide(
            stage = stage,
            inputPrinciple = "이 시기의 양질의 입력은 문제집이 아니라 경험입니다. 책을 읽어 주고, 함께 만들고, 밖에서 몸으로 배운 것이 나중의 학습 체력이 됩니다. 앉아서 공부하는 시간은 짧게, 대신 매일.",
            parentTips = listOf("하루 20분 함께 읽기. 아이가 고른 책이면 더 좋아요", "결과 대신 시도한 것을 말로 되짚어 주세요", "학습 기록은 부모가 대신 남겨도 괜찮은 시기예요"),
            mentorTips = listOf("영상·자료는 10분 안팎, 놀이·이야기 형식 위주로 고르세요", "과제는 하루 하나, 완료 경험이 목적입니다", "학급 진도보다 흥미가 우선. 앞서가기보다 좋아하는 주제를 깊게"),
            experiences = listOf("도서관에서 책 고르기", "박물관·과학관 나들이", "요리·만들기 같이 하기", "자연 관찰 일기", "보드게임으로 규칙 배우기"),
            praiseStyle = "과정과 시도를 구체적으로. '끝까지 앉아 있었네', '스스로 골랐네' 처럼",
            planOptions = PlanOptions(days = 7, startTime = LocalTime.of(16, 30), sessionMinutes = stage.sessionMinutes, breakMinutes = 10, sessionsPerDay = stage.sessionsPerDay, includeWeekend = false),
        )
        GrowthStage.UPPER_ELEMENTARY -> StageGuide(
            stage = stage,
            inputPrinciple = "습관이 만들어지는 시기입니다. 무엇을 얼마나 아는가보다 '스스로 계획하고 끝내는' 경험이 양질의 입력입니다. 학습량은 서서히, 자율은 빠르게 늘리세요.",
            parentTips = listOf("계획은 아이가 세우고 부모는 확인만", "잘한 날보다 꾸준한 주를 칭찬하세요", "체험과 독서 시간을 학습 시간과 같은 비중으로"),
            mentorTips = listOf("개념 영상은 15분 이내, 바로 풀어보는 문제와 짝지어 주세요", "로드맵은 2주 단위, 학생이 체크할 수 있게 작게", "약한 과목보다 좋아하는 과목에서 성공 경험을 먼저"),
            experiences = listOf("주간 계획표를 직접 쓰기", "관심 주제 하나로 작은 발표·전시 만들기", "지역 체험 프로그램·캠프", "친구와 함께 하는 프로젝트", "긴 책 한 권 완독"),
            praiseStyle = "꾸준함과 계획을 지킨 것을. '이번 주 4일이나 했네', '계획대로 했구나'",
            planOptions = PlanOptions(days = 7, startTime = LocalTime.of(17, 0), sessionMinutes = stage.sessionMinutes, breakMinutes = 10, sessionsPerDay = stage.sessionsPerDay, includeWeekend = true),
        )
        GrowthStage.MIDDLE -> StageGuide(
            stage = stage,
            inputPrinciple = "과목의 뼈대(개념)가 세워지는 시기입니다. 양질의 입력은 '왜 그런지'를 설명하는 개념 강의와, 틀린 이유를 스스로 적는 오답 정리입니다. 양보다 이해의 깊이가 이후 3년을 좌우합니다.",
            parentTips = listOf("점수보다 '어디가 헷갈렸는지' 대화로 물어보세요", "수면·운동 시간을 지켜 주는 것이 최고의 지원", "친구·진로 고민이 학습을 흔드는 시기, 메모로 짧게 응원"),
            mentorTips = listOf("개념 강의 → 대표 유형 → 오답 정리 순서로 로드맵을 짜세요", "약한 과목의 학급 진도를 먼저 따라잡게, 예습은 그다음", "시험 3주 전부터 시험 대비 유형 콘텐츠로 전환"),
            experiences = listOf("진로 관련 직업인 인터뷰·체험", "동아리·대회 하나 끝까지 참여", "혼자 여행 계획 세워 보기", "봉사 활동", "긴 글 쓰기(독후감·에세이)"),
            praiseStyle = "이해와 노력을. '틀린 이유를 스스로 찾았네', '어려운 단원을 붙잡았구나'",
            planOptions = PlanOptions(days = 7, startTime = LocalTime.of(19, 0), sessionMinutes = stage.sessionMinutes, breakMinutes = 10, sessionsPerDay = stage.sessionsPerDay, includeWeekend = true),
        )
        GrowthStage.HIGH -> StageGuide(
            stage = stage,
            inputPrinciple = "전략의 시기입니다. 양질의 입력은 목표에 맞춘 선택과 집중, 그리고 자기 데이터(성적 추이·시간 분포)를 보고 스스로 조정하는 경험입니다. 어른의 역할은 지시가 아니라 판단을 돕는 정보 제공입니다.",
            parentTips = listOf("계획과 배분은 자녀에게, 부모는 컨디션과 정서를 살피세요", "결과가 나온 날보다 준비하는 날에 응원을", "진로·입시 정보는 함께 찾되 결정은 본인이"),
            mentorTips = listOf("과목별 목표 등급에 맞춰 시간 배분을 함께 설계하세요", "기출·모의고사 유형 콘텐츠와 심화 개념을 섞어 큐레이팅", "로드맵은 시험 일정 역산, 주 단위 점검"),
            experiences = listOf("관심 분야 대학 강의·공개 강좌 듣기", "탐구 보고서·프로젝트 하나 완성", "학과·직업 탐방", "스터디 그룹 운영해 보기", "긴 호흡의 독서와 요약"),
            praiseStyle = "판단과 자기주도를. '스스로 배분을 바꿨네', '데이터 보고 조정했구나'",
            planOptions = PlanOptions(days = 7, startTime = LocalTime.of(20, 0), sessionMinutes = stage.sessionMinutes, breakMinutes = 10, sessionsPerDay = stage.sessionsPerDay, includeWeekend = true),
        )
    }

    /** 오늘 보여줄 팁·경험 하나. 매일 바뀌되 같은 날에는 같은 것을 보여 줍니다. */
    fun <T> pickForDay(items: List<T>, day: LocalDate): T? = if (items.isEmpty()) null else items[(day.toEpochDay() % items.size).toInt().let { if (it < 0) it + items.size else it }]

    /** 단계가 없을 때(학년 미입력) 쓰는 기본 계획 옵션. */
    fun defaultPlanOptions(stage: GrowthStage?): PlanOptions = stage?.let { forStage(it).planOptions } ?: PlanOptions()
}
