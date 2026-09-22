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
        GrowthStage.NEWBORN -> StageGuide(
            stage = stage,
            inputPrinciple = "이 시기의 양질의 입력은 반응입니다. 울면 안아 주고, 눈을 맞추고, 말을 많이 걸어 주는 것이 뇌와 정서의 기초를 만듭니다. 학습이라 부를 것은 없고, 행정·건강 일정(출생신고, 예방접종, 영유아검진, 어린이집 대기)이 준비의 전부입니다.",
            parentTips = listOf("하루에 여러 번 아이 이름을 부르며 말 걸기", "예방접종·영유아검진 일정을 여정에서 미리 확인", "어린이집 입소 대기는 태어나자마자 신청해 두세요"),
            mentorTips = listOf("이 시기엔 멘토보다 부모의 휴식이 중요합니다", "아이보다 부모에게 정보를 주세요"),
            experiences = listOf("매일 같은 자장가·그림책 한 권", "거울 보기·소리 나는 장난감", "산책하며 바깥 공기 쐬기", "다양한 질감 만져 보기", "가족 얼굴 자주 보여 주기"),
            praiseStyle = "아이가 아니라 돌보는 부모 자신을 격려하세요",
            planOptions = PlanOptions(days = 0, sessionsPerDay = 0, sessionMinutes = 0),
        )
        GrowthStage.TODDLER -> StageGuide(
            stage = stage,
            inputPrinciple = "말이 폭발하는 시기입니다. 양질의 입력은 풍부한 모국어 대화와 그림책, 그리고 몸으로 노는 시간입니다. 화면 노출은 최소로, 외국어는 노래·소리 정도로 가볍게. 이 시기 어휘량이 이후 읽기 능력의 토대가 됩니다.",
            parentTips = listOf("아이 말에 한 단어 더 붙여 되돌려 주기(\"공\" → \"빨간 공이네\")", "하루 그림책 3권, 같은 책 반복도 좋아요", "만 2세 전후 언어·발달 검진 시기를 놓치지 마세요"),
            mentorTips = listOf("놀이 교실·문화센터 수준의 짧은 활동만", "글자·숫자 조기 학습보다 대화량이 우선"),
            experiences = listOf("놀이터·모래·물놀이", "동네 도서관 유아 코너", "동물원·시장 구경", "노래·율동 따라 하기", "또래와 짧은 놀이 시간"),
            praiseStyle = "말과 시도를 따라 말해 주며. \"혼자 신발 신었네!\"",
            planOptions = PlanOptions(days = 0, sessionsPerDay = 0, sessionMinutes = 0),
        )
        GrowthStage.PRESCHOOL -> StageGuide(
            stage = stage,
            inputPrinciple = "언어 민감기(대략 만 3~7세)입니다. 소리를 구별하고 흉내 내는 능력이 가장 좋은 때라 외국어는 '공부'가 아니라 노래·영상·놀이로 매일 조금씩 노출하는 것이 양질의 입력입니다. 한글은 만 5~6세 관심이 생길 때 놀이로. 유치원·초등 입학 준비 일정이 이 시기의 핵심 행정입니다.",
            parentTips = listOf("영어 노래·짧은 영상 하루 15분, 부담 없이 반복", "한글은 아이가 글자를 묻기 시작할 때부터", "유치원 지원(11월)과 초등 예비소집(1월)을 여정에서 확인"),
            mentorTips = listOf("15분 단위 놀이형 활동, 앉히기보다 움직이게", "소리 중심의 언어 노출 자료를 고르세요", "숫자·글자는 생활 속 놀이(가게 놀이, 간판 읽기)로"),
            experiences = listOf("유치원·어린이집 친구와 역할놀이", "자연 관찰·텃밭 가꾸기", "그림 그리기·만들기", "동화 구연·인형극 보기", "자전거·수영 같은 몸 쓰기"),
            praiseStyle = "호기심과 시도를. \"어떻게 알았어?\", \"끝까지 만들었네\"",
            planOptions = PlanOptions(days = 5, startTime = LocalTime.of(16, 0), sessionMinutes = stage.sessionMinutes, breakMinutes = 10, sessionsPerDay = stage.sessionsPerDay, includeWeekend = false),
        )
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
        GrowthStage.UNIVERSITY -> StageGuide(
            stage = stage,
            inputPrinciple = "탐색의 시기입니다. 양질의 입력은 강의보다 경험입니다. 전공 밖 수업, 프로젝트, 인턴, 사람, 그리고 실패해 본 기억이 진로를 만듭니다. 부모의 역할은 지원과 경청으로 바뀌고, 멘토는 진로·커리어 코치에 가까워집니다.",
            parentTips = listOf("진로 이야기는 묻기보다 들어 주세요", "재정(등록금·생활비·장학금)을 함께 계획하되 결정은 본인이", "휴학·전과·교환학생 같은 선택을 실패로 보지 마세요"),
            mentorTips = listOf("학기마다 결과물 하나(포트폴리오·논문·프로젝트)를 목표로", "인턴·대외활동·자격은 3학년 전에 시작하도록 일정을 역산", "로드맵은 학기 단위, 과목보다 역량 중심"),
            experiences = listOf("교환학생·어학연수", "인턴십·현장실습", "학회·해커톤·공모전", "동아리 운영·리더 경험", "혼자 떠나는 여행"),
            praiseStyle = "선택과 책임을. '스스로 정하고 끝냈네'",
            planOptions = PlanOptions(days = 7, startTime = LocalTime.of(19, 0), sessionMinutes = stage.sessionMinutes, breakMinutes = 15, sessionsPerDay = stage.sessionsPerDay, includeWeekend = true),
        )
        GrowthStage.GRADUATE -> StageGuide(
            stage = stage,
            inputPrinciple = "깊이의 시기입니다. 양질의 입력은 원전 읽기, 재현, 쓰기, 그리고 지도교수·동료와의 토론입니다. 성과가 늦게 나오는 구조라 정신 건강과 재정 계획이 학업만큼 중요합니다.",
            parentTips = listOf("결과를 묻기보다 건강과 생활을 챙겨 주세요", "학위 기간·재정 계획을 함께 점검", "진로 전환(취업·유학·창업)도 열어 두기"),
            mentorTips = listOf("주간 글쓰기 목표(초록·서론 한 단락)를 로드맵에", "학회 발표·투고 일정을 역산해 마감 관리", "지도교수 면담 전 준비 항목을 과제로"),
            experiences = listOf("학회 발표·포스터", "타 기관 공동연구·방문", "티칭·튜터링 경험", "학술 글 외 대중 글쓰기", "산업체 인턴·자문"),
            praiseStyle = "끈기와 정직한 기록을. '실패 실험도 정리했네'",
            planOptions = PlanOptions(days = 7, startTime = LocalTime.of(9, 0), sessionMinutes = stage.sessionMinutes, breakMinutes = 20, sessionsPerDay = stage.sessionsPerDay, includeWeekend = false),
        )
    }

    /** 오늘 보여줄 팁·경험 하나. 매일 바뀌되 같은 날에는 같은 것을 보여 줍니다. */
    fun <T> pickForDay(items: List<T>, day: LocalDate): T? = if (items.isEmpty()) null else items[(day.toEpochDay() % items.size).toInt().let { if (it < 0) it + items.size else it }]

    /** 단계가 없을 때(학년 미입력) 쓰는 기본 계획 옵션. */
    fun defaultPlanOptions(stage: GrowthStage?): PlanOptions = stage?.let { forStage(it).planOptions } ?: PlanOptions()
}
