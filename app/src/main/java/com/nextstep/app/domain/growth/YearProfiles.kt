package com.nextstep.app.domain.growth

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.growth.StudentHomeSection.JOURNEY
import com.nextstep.app.domain.growth.StudentHomeSection.MISSION
import com.nextstep.app.domain.growth.StudentHomeSection.PLANNER
import com.nextstep.app.domain.growth.StudentHomeSection.PREVIEW
import com.nextstep.app.domain.growth.StudentHomeSection.REVIEW
import com.nextstep.app.domain.growth.StudentHomeSection.SUBJECTS
import com.nextstep.app.domain.growth.StudentHomeSection.TASKS
import com.nextstep.app.domain.growth.StudentHomeSection.YEAR
import com.nextstep.app.domain.growth.StudyKindType.CAREER
import com.nextstep.app.domain.growth.StudyKindType.HABIT
import com.nextstep.app.domain.growth.StudyKindType.PLAY
import com.nextstep.app.domain.growth.StudyKindType.PRACTICE
import com.nextstep.app.domain.growth.StudyKindType.PROJECT
import com.nextstep.app.domain.growth.StudyKindType.READ
import com.nextstep.app.domain.growth.StudyKindType.TEST_PREP
import com.nextstep.app.domain.growth.StudyKindType.WRITE
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 만 0세부터 고3까지 한 해에 하나씩, 그리고 대학·대학원 프로필. 과목은 2022 개정 교육과정(유아는 누리과정) 기준입니다.
 * 하루 권장 학습(분)은 학교 밖 스스로 하는 공부의 상한에 가깝게 잡았습니다: 이 앱은 더 하라고 밀지 않습니다.
 * 키는 저장된 상태와 연결되지 않지만 테스트와 프로토타입이 같은 값을 쓰므로 바꾸지 않습니다.
 */
object YearProfiles {
    private const val MAX_PRESCHOOL_AGE = 6
    private const val MONTHS_PER_YEAR = 12
    private const val LAST_SCHOOL_GRADE = 12
    private const val LAST_UNIVERSITY_GRADE = 16

    /** 누리과정 5영역(만 3~5세). */
    private val NURI = listOf("신체운동·건강", "의사소통", "사회관계", "예술경험", "자연탐구")
    private val INFANT = listOf("기본생활", "신체운동", "의사소통", "사회관계", "예술경험", "자연탐구")
    private val LOWER = listOf("국어", "수학", "바른 생활", "슬기로운 생활", "즐거운 생활")
    private val MIDDLE_ELEM = listOf("국어", "수학", "사회", "과학", "영어", "도덕", "음악", "미술", "체육")
    private val UPPER_ELEM = MIDDLE_ELEM + "실과"
    private val MIDDLE = listOf("국어", "수학", "영어", "사회", "과학", "도덕", "기술·가정", "정보", "체육", "음악", "미술")

    val all: List<YearProfile> = listOf(
        // ---------------------------------------------------------------- 학령 전 (씨앗): 앉아서 하는 공부 대신 놀이
        // 만 0~2세의 "공부"는 전부 부모가 해 주는 일입니다(아이가 스스로 읽거나 푸는 것이 아님).
        preschool(0, "안아 주고 말 걸어 주는 해", INFANT, 0, k("말 걸고 옹알이에 대답하기", PLAY, 7, 10), k("엎드려 놀기(터미타임)", PLAY, 7, 15), k("노래·자장가 불러 주기", PLAY, 7, 10)),
        preschool(1, "걷고 첫 단어가 나오는 해", INFANT, 0, k("그림책 읽어 주기", READ, 7, 10), k("바깥 걷기", PLAY, 7, 30), k("노래·손유희", PLAY, 5, 10)),
        preschool(2, "말이 트이는 해", INFANT, 0, k("그림책 2~3권 읽어 주기", READ, 7, 15), k("역할 놀이", PLAY, 5, 20), k("끼적이기", PLAY, 3, 10)),
        preschool(3, "놀이로 배우기 시작하는 해", NURI, 10, k("함께 읽기", READ, 7, 15), k("영어 노래·소리", PLAY, 5, 10), k("블록·퍼즐", PLAY, 5, 15)),
        preschool(4, "호기심이 커지는 해", NURI, 10, k("함께 읽기", READ, 7, 15), k("영어 노래", PLAY, 5, 10), k("숫자 놀이", PLAY, 3, 10), k("그리기·만들기", PLAY, 3, 15)),
        preschool(5, "글자에 관심이 생기는 해", NURI, 15, k("함께 읽기", READ, 7, 20), k("한글 놀이", PLAY, 3, 10), k("수 세기 놀이", PLAY, 3, 10), k("영어 그림책", READ, 3, 10)),
        preschool(6, "학교 갈 준비를 하는 해", NURI, 20, k("매일 함께 읽기", READ, 7, 20), k("한글 읽기", READ, 5, 10), k("20분 앉아 있기", HABIT, 5, 20), k("준비물 스스로 챙기기", HABIT, 7, 5)),
        // ---------------------------------------------------------------- 초등
        school(1, "한글과 학교생활에 익숙해지는 해", LOWER, 20, 10, 2, 5, 1.3f, 2, listOf(TASKS, YEAR),
            k("소리 내어 읽기", READ, 7, 10), k("받아쓰기 연습", WRITE, 2, 10), k("수학 익힘", PRACTICE, 3, 10), k("알림장·준비물 챙기기", HABIT, 5, 5)),
        school(2, "구구단과 혼자 읽기의 해", LOWER, 30, 15, 2, 5, 1.25f, 2, listOf(YEAR),
            k("혼자 읽기", READ, 7, 15), k("받아쓰기", WRITE, 2, 10), k("구구단", PRACTICE, 5, 10), k("그림일기", WRITE, 2, 15)),
        school(3, "사회·과학·영어가 새로 시작되는 해", MIDDLE_ELEM, 40, 20, 2, 5, 1.2f, 3, listOf(YEAR),
            k("영어 듣기·단어", PRACTICE, 5, 10), k("분수 처음 배우기", PRACTICE, 3, 15), k("과학 관찰 기록", PROJECT, 1, 20), k("독서록", WRITE, 1, 20)),
        school(4, "단원평가로 배운 것을 확인하는 해", MIDDLE_ELEM, 50, 25, 2, 5, 1.15f, 3, listOf(REVIEW),
            k("단원평가 복습", TEST_PREP, 2, 20), k("큰 수·각도 연습", PRACTICE, 3, 15), k("영어 문장 읽기", READ, 5, 10), k("독서록", WRITE, 1, 20)),
        school(5, "수학이 어려워지는 고비, 한국사가 시작되는 해", UPPER_ELEM, 60, 30, 2, 5, 1.1f, 3, listOf(REVIEW, YEAR),
            k("약수·배수·분수 연산", PRACTICE, 4, 20), k("영어 읽기", READ, 5, 15), k("한국사 읽기", READ, 2, 20), k("수행평가 준비", PROJECT, 1, 30)),
        school(6, "중학교를 준비하는 해", UPPER_ELEM, 70, 35, 2, 5, 1.05f, 3, listOf(PREVIEW),
            k("비와 비율", PRACTICE, 4, 20), k("영어 문법 기초", PRACTICE, 3, 20), k("중학 수학 맛보기", PRACTICE, 2, 20), k("진로 찾기", CAREER, 1, 30)),
        // ---------------------------------------------------------------- 중학
        school(7, "자유학기: 시험 대신 수행평가와 진로 탐색", MIDDLE, 80, 40, 2, 6, 1.0f, 3, listOf(YEAR),
            k("정수·유리수", PRACTICE, 4, 25), k("영어 문법", PRACTICE, 3, 25), k("수행평가 준비", PROJECT, 2, 30), k("진로 탐색 활동", CAREER, 1, 40)),
        school(8, "첫 지필평가(중간·기말)가 시작되는 해", MIDDLE + "역사", 100, 50, 2, 6, 1.0f, 3, listOf(MISSION),
            k("중간·기말 대비", TEST_PREP, 3, 40), k("일차함수", PRACTICE, 4, 25), k("영어 내신", PRACTICE, 4, 25), k("역사 정리", READ, 2, 25), k("수행평가", PROJECT, 1, 40)),
        school(9, "고등학교를 고르는 해", MIDDLE + "역사", 120, 40, 3, 6, 1.0f, 3, listOf(MISSION, JOURNEY),
            k("기말·수행 대비", TEST_PREP, 3, 40), k("이차방정식·함수", PRACTICE, 4, 30), k("영어 독해", READ, 4, 25), k("고교 선택 알아보기", CAREER, 1, 40)),
        // ---------------------------------------------------------------- 고등
        school(10, "고교학점제 첫해, 내신과 학생부가 시작되는 해", listOf("공통국어", "공통수학", "공통영어", "통합사회", "통합과학", "한국사", "과학탐구실험"), 150, 50, 3, 6, 1.0f, 3, listOf(MISSION, YEAR),
            k("내신 대비", TEST_PREP, 3, 50), k("공통수학", PRACTICE, 5, 40), k("모의고사 복기", TEST_PREP, 1, 50), k("동아리·세특 기록", PROJECT, 1, 30)),
        school(11, "선택과목과 진로가 정해지는 해", listOf("국어 선택", "수학 선택", "영어 선택", "사회·과학 탐구 선택", "진로 선택"), 180, 60, 3, 6, 1.0f, 3, listOf(MISSION, SUBJECTS),
            k("선택과목 내신", TEST_PREP, 3, 60), k("모의고사 복기", TEST_PREP, 1, 60), k("탐구 보고서", PROJECT, 1, 60), k("진로 정하기", CAREER, 1, 30)),
        school(12, "수능과 수시, 마지막 해", listOf("국어", "수학", "영어", "한국사", "탐구 2과목", "진로 선택"), 210, 70, 3, 6, 1.0f, 3, listOf(MISSION, PLANNER),
            k("수능 기출", TEST_PREP, 6, 70), k("6·9월 모평 복기", TEST_PREP, 1, 60), k("수시 원서·서류", CAREER, 1, 60), k("수면·컨디션 지키기", HABIT, 7, 10)),
        // ---------------------------------------------------------------- 대학·대학원
        YearProfile("u", "대학", StudentUiLevel.TREE, "전공과 경험을 넓히는 시기", listOf("전공", "교양"),
            listOf(k("전공 공부", READ, 5, 60), k("과제·프로젝트", PROJECT, 2, 90), k("진로·경험", CAREER, 1, 60)), 180, 90, 2, 5, 1.0f, 3, emptyList()),
        YearProfile("g", "대학원", StudentUiLevel.TREE, "한 문제를 깊이 파는 시기", listOf("연구", "세미나"),
            listOf(k("논문 읽기", READ, 5, 60), k("연구·실험", PROJECT, 5, 120), k("글쓰기", WRITE, 3, 60)), 240, 120, 2, 5, 1.0f, 3, emptyList()),
    )

    val byKey: Map<String, YearProfile> = all.associateBy { it.key }

    /** 만 나이(학령 전, 0~6). */
    fun forAge(age: Int): YearProfile = byKey.getValue("a${age.coerceIn(0, MAX_PRESCHOOL_AGE)}")

    /** 학년(1=초1 … 12=고3, 13~16=대학, 17~18=대학원). 0 이하는 null. */
    fun forGrade(gradeYear: Int): YearProfile? = when {
        gradeYear <= 0 -> null
        gradeYear <= LAST_SCHOOL_GRADE -> byKey.getValue(schoolKey(gradeYear))
        gradeYear <= LAST_UNIVERSITY_GRADE -> byKey.getValue("u")
        else -> byKey.getValue("g")
    }

    /** 학생 구성원의 올해: 생년월일(학제 3월 기준) → 학년 순. 둘 다 없으면 null. */
    fun of(student: MemberEntity, today: LocalDate = DateUtils.today()): YearProfile? {
        student.birthDate?.let { epoch ->
            val birth = DateUtils.fromEpochDay(epoch)
            GrowthStage.schoolGradeYear(birth, today)?.let { return forGrade(it) }
            val months = ChronoUnit.MONTHS.between(birth, today).toInt()
            return if (today.year - birth.year <= GrowthStage.ELEMENTARY_ENTRY_YEARS_AFTER_BIRTH) forAge(months.coerceAtLeast(0) / MONTHS_PER_YEAR) else byKey.getValue("g")
        }
        return forGrade(student.gradeYear)
    }

    private fun schoolKey(grade: Int): String = when {
        grade <= 6 -> "e$grade"
        grade <= 9 -> "m${grade - 6}"
        else -> "h${grade - 9}"
    }

    private fun k(name: String, type: StudyKindType, timesPerWeek: Int, minutes: Int) = StudyKind(name, type, timesPerWeek, minutes)

    private fun preschool(age: Int, theme: String, subjects: List<String>, dailyMinutes: Int, vararg kinds: StudyKind) = YearProfile(
        key = "a$age", label = "만 ${age}세", level = StudentUiLevel.SEED, theme = theme, subjects = subjects, kinds = kinds.toList(),
        dailyMinutes = dailyMinutes, sessionMinutes = dailyMinutes, sessionsPerDay = if (dailyMinutes == 0) 0 else 1,
        studyDaysPerWeek = if (dailyMinutes == 0) 0 else 5, textScale = 1.4f, taskRows = 2, lead = listOf(TASKS),
    )

    private fun school(
        grade: Int, theme: String, subjects: List<String>, dailyMinutes: Int, sessionMinutes: Int, sessionsPerDay: Int, days: Int,
        textScale: Float, taskRows: Int, lead: List<StudentHomeSection>, vararg kinds: StudyKind,
    ) = YearProfile(
        key = schoolKey(grade), label = GrowthStage.fromGradeYear(grade)!!.gradeLabel(grade), level = StudentUiLevel.forGrade(grade), theme = theme,
        subjects = subjects, kinds = kinds.toList(), dailyMinutes = dailyMinutes, sessionMinutes = sessionMinutes, sessionsPerDay = sessionsPerDay,
        studyDaysPerWeek = days, textScale = textScale, taskRows = taskRows, lead = lead,
    )
}
