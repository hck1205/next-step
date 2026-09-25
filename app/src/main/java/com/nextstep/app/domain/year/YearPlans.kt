package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.YearArea.ARTS
import com.nextstep.app.domain.year.YearArea.BODY
import com.nextstep.app.domain.year.YearArea.CAREER
import com.nextstep.app.domain.year.YearArea.ENGLISH
import com.nextstep.app.domain.year.YearArea.EXAM
import com.nextstep.app.domain.year.YearArea.KOREAN
import com.nextstep.app.domain.year.YearArea.LIFE
import com.nextstep.app.domain.year.YearArea.MATH
import com.nextstep.app.domain.year.YearArea.PLAY
import com.nextstep.app.domain.year.YearArea.READING
import com.nextstep.app.domain.year.YearArea.RECORD
import com.nextstep.app.domain.year.YearArea.SCIENCE
import com.nextstep.app.domain.year.YearArea.SOCIETY
import com.nextstep.app.domain.year.YearArea.TALK

/**
 * 해마다 학생이 할 일을 분류(탭)별로 잘게 나눈 목록. 키는 YearProfiles 와 같습니다(a0~a6, e1~e6, m1~m3, h1~h3, u, g).
 * 초중고 과목·단원은 2022 개정 교육과정, 유아는 표준보육·누리과정과 영유아 검진·접종 일정을 따릅니다.
 * 양은 상한에 가깝게 적었습니다: 더 시키라는 목록이 아니라 "이만큼이면 충분한" 목록입니다.
 */
object YearPlans {
    fun forYear(key: String): List<YearTask> = plans[key].orEmpty()

    /** 그 해에 할 일이 있는 분류(탭 순서). */
    fun areasOf(key: String): List<YearArea> = forYear(key).map { it.area }.distinct().sortedBy { it.ordinal }

    private val plans: Map<String, List<YearTask>> = mapOf(
        "a0" to listOf(
            a(TALK, "말 걸기·눈 맞추기", "아이 소리에 대답하며 하루 여러 번"), a(TALK, "그림책 보여 주기", "6개월부터 하루 5분"),
            a(PLAY, "엎드려 놀기", "깨어 있을 때 하루 여러 번"), a(PLAY, "딸랑이·까꿍 놀이", "소리와 얼굴로 주고받기"),
            a(BODY, "영유아 검진 1·2차", "4~6개월, 9~12개월"), a(BODY, "국가 예방접종", "2·4·6개월 일정대로"),
            a(LIFE, "수면 리듬 만들기", "밤낮 구분, 같은 시각에 재우기"),
        ),
        "a1" to listOf(
            a(TALK, "그림책 매일 읽어 주기", "하루 10분, 같은 책 반복도 좋아요"), a(TALK, "한 단어 더 붙여 되돌려 주기", "\"물\" → \"물 줄까?\""),
            a(PLAY, "매일 바깥 산책", "30분, 걷기 연습"), a(PLAY, "쌓기·끼우기 놀이", "컵·블록"),
            a(BODY, "영유아 검진 3차", "18~24개월"), a(BODY, "돌 전후 예방접종", "MMR·수두·일본뇌염"),
            a(LIFE, "컵으로 마시기", "젖병 줄이기"), a(LIFE, "화면 없이 지내기", "만 2세 전 영상은 거의 없이"),
        ),
        "a2" to listOf(
            a(TALK, "그림책 하루 3권", "아이가 고른 책으로"), a(TALK, "두 단어 문장 대화", "아이 말을 문장으로 늘려 주기"),
            a(PLAY, "역할 놀이", "소꿉·인형"), a(PLAY, "끼적이기·색칠", "큰 종이에 자유롭게"), a(PLAY, "몸 놀이", "달리기·점프·공"),
            a(BODY, "언어·발달 점검", "영유아 검진 4차"), a(BODY, "치과 첫 검진", "불소 도포"),
            a(LIFE, "혼자 숟가락 쓰기", "흘려도 괜찮아요"), a(LIFE, "배변 연습", "아이가 준비됐을 때"), a(LIFE, "화면 시간 규칙", "하루 한도를 정하기"),
        ),
        "a3" to listOf(
            a(TALK, "함께 읽기", "하루 15분"), a(TALK, "오늘 있었던 일 말하기", "저녁에 한 가지씩"),
            a(ENGLISH, "영어 노래·소리 놀이", "하루 10분, 놀이처럼"),
            a(PLAY, "블록·퍼즐", "스스로 끝까지"), a(PLAY, "그리기·만들기", "주 2~3번"),
            a(BODY, "놀이터 매일", "대근육 놀이 1시간"), a(BODY, "영유아 검진", "시력·청력 확인"),
            a(LIFE, "옷 입기 도전", "단추 없는 옷부터"), s2(LIFE, "유치원 지원", "11월 처음학교로"),
        ),
        "a4" to listOf(
            a(TALK, "함께 읽기", "하루 15분"), a(TALK, "끝말잇기·말놀이", "차 안·식탁에서"),
            a(ENGLISH, "영어 그림책·노래", "주 5회 10분"),
            a(MATH, "숫자 놀이", "10까지 세기, 놀이로"),
            a(PLAY, "만들기·그리기", "주 3번"),
            a(BODY, "자전거·킥보드", "주말마다"),
            a(LIFE, "장난감 정리", "놀고 나서 스스로"), a(LIFE, "수업은 주 2~3개까지", "학원·수업 주 5시간 안"),
        ),
        "a5" to listOf(
            a(TALK, "함께 읽기 20분", "잠들기 전"), a(TALK, "한글 놀이", "이름·간판 글자부터"),
            a(MATH, "수 세기·크기 비교", "생활 속에서"),
            a(ENGLISH, "영어 그림책", "주 3회 10분"),
            a(PLAY, "악기·그림 체험", "좋아하는 것 찾기"),
            a(BODY, "영유아 검진 마지막 회차", "시력·치과"),
            a(LIFE, "혼자 씻기", "양치·세수"), a(LIFE, "시험 보는 학원 피하기", "레벨테스트는 2026년 10월부터 금지"),
        ),
        "a6" to listOf(
            a(TALK, "매일 함께 읽기", "20분"), a(TALK, "한글 읽기", "받침 없는 글자부터"),
            a(MATH, "10 가르기·모으기", "구슬·손가락으로"),
            a(PLAY, "좋아하는 것 하나 정하기", "그림·악기·운동 중에서"),
            s2(BODY, "시력·치과 치료 마치기", "입학 전에"),
            a(LIFE, "20분 앉아 있기", "그림책·퍼즐로 연습"), a(LIFE, "혼자 화장실·옷 입기", "학교생활의 기본"),
            s2(LIFE, "취학통지서·예비소집", "12월 통지서, 1월 예비소집"), s2(LIFE, "돌봄교실 신청", "맞벌이라면 1~2월"),
        ),
        "e1" to listOf(
            s1(KOREAN, "자음·모음 익히기", "하루 10분 따라 쓰기"), s1(KOREAN, "받침 있는 글자 읽기", "교과서 소리 내어"), s2(KOREAN, "받아쓰기", "주 1회 급수표 10문제"), s2(KOREAN, "그림일기", "주 2회 세 문장"),
            s1(MATH, "9까지 수·가르기", "구슬로"), s1(MATH, "50까지 수", "수 모형"), s2(MATH, "덧셈·뺄셈", "수학 익힘 주 3회 10분"), s2(MATH, "시계 보기", "몇 시·몇 시 30분"),
            a(READING, "매일 소리 내어 읽기", "10분"), a(READING, "그림책 100권", "읽은 책 스티커"),
            a(ARTS, "통합교과 만들기", "학교 활동 이어서"),
            a(LIFE, "알림장 확인", "매일 가방 싸기 전"), a(LIFE, "준비물 스스로 챙기기", "전날 밤"), a(LIFE, "9시에 자기", "잠이 공부예요"),
            a(BODY, "줄넘기", "하루 10번부터"), a(BODY, "물놀이·수영 경험", "생존수영 전에"),
        ),
        "e2" to listOf(
            s1(KOREAN, "문장부호 쓰기", "마침표·물음표·느낌표"), a(KOREAN, "받아쓰기", "주 1회"), a(KOREAN, "일기 쓰기", "주 2회 세 문장"),
            s1(MATH, "세 자리 수", "수 모형"), s1(MATH, "받아올림 덧셈", "주 3회 10분"), s2(MATH, "구구단", "2단부터 9단까지"), s2(MATH, "길이 재기", "자로 cm"),
            a(ENGLISH, "영어 노래·그림책", "주 3회"),
            a(READING, "혼자 읽기 15분", "매일"), s2(READING, "글밥 있는 책 넘어가기", "한 쪽에 문장 다섯 줄"),
            a(LIFE, "숙제 스스로 하기", "학교 다녀와서 바로"), a(LIFE, "시간표 보고 가방 싸기", "전날 밤"),
            a(BODY, "줄넘기 50개", "매일 조금씩"), a(BODY, "두발자전거", "주말"),
        ),
        "e3" to listOf(
            s1(KOREAN, "문단 읽기", "중심 문장 찾기"), s1(KOREAN, "국어사전 찾기", "모르는 낱말 하루 하나"), a(KOREAN, "독서록", "주 1회"),
            s1(MATH, "세 자리 덧셈·뺄셈", "익힘책"), s1(MATH, "나눗셈 처음", "똑같이 나누기"), s2(MATH, "곱셈 두 자리", "주 3회 15분"), s2(MATH, "분수 처음", "피자·색종이로"),
            s1(ENGLISH, "알파벳·파닉스", "주 5회 10분"), a(ENGLISH, "교과서 단어 듣기", "매일 10분"),
            s1(SOCIETY, "우리 고장 알기", "동네 지도 그리기"), s2(SOCIETY, "지도 보기", "방위·기호"),
            a(SCIENCE, "관찰 기록", "주 1회 그림과 한 줄"), s1(SCIENCE, "물질·동물 단원", "실험 결과 정리"),
            a(READING, "한 달 4권", "역사·과학 그림책 섞기"),
            a(ARTS, "리코더", "주 2회 10분"),
            a(LIFE, "스스로 계획표", "주 1회 만들기"),
            a(BODY, "생존수영", "학교 수업 따라"),
        ),
        "e4" to listOf(
            s1(KOREAN, "요약하기", "문단마다 한 줄"), s2(KOREAN, "의견 쓰기", "까닭 두 가지"), a(KOREAN, "독서록", "주 1회"),
            s1(MATH, "큰 수", "억·조"), s1(MATH, "각도", "각도기"), s1(MATH, "곱셈·나눗셈", "세 자리 × 두 자리"), s2(MATH, "분수 덧셈·뺄셈", "분모 같은 분수"), s2(MATH, "소수", "소수 두 자리"),
            a(ENGLISH, "영어 문장 읽기", "주 5회 10분"), s2(ENGLISH, "짧은 글 쓰기", "세 문장 자기소개"),
            s1(SOCIETY, "지역 문제와 촌락", "우리 지역 조사"), s2(SOCIETY, "경제 기초", "용돈 기입장"),
            s1(SCIENCE, "식물·지층", "관찰 기록"), s2(SCIENCE, "물의 상태 변화", "실험 정리"),
            a(EXAM, "단원평가 복습", "틀린 문제 다시 풀기"),
            a(READING, "한 달 4권", "고전·과학 읽기"),
            a(LIFE, "스마트폰 규칙", "가족과 함께 정하기"),
            a(BODY, "운동 하나 꾸준히", "주 2회"),
        ),
        "e5" to listOf(
            s1(KOREAN, "토의·토론", "근거 들어 말하기"), s1(KOREAN, "글의 구조", "처음·가운데·끝"), s2(KOREAN, "논설문", "주장과 근거"),
            s1(MATH, "약수와 배수", "주 4회 20분"), s1(MATH, "약분과 통분", "최소공배수 활용"), s1(MATH, "분수 덧셈·뺄셈", "분모 다른 분수"), s1(MATH, "다각형의 넓이", "공식 이해"), s2(MATH, "분수의 곱셈", "익힘책"),
            a(ENGLISH, "영어 읽기", "주 5회 15분"), s2(ENGLISH, "문법 기초", "be동사·일반동사"),
            s1(SOCIETY, "국토와 우리 생활", "지도·기후"), s2(SOCIETY, "한국사", "고조선~조선, 주 2회 20분"),
            s1(SCIENCE, "온도와 열", "실험 보고서"), s1(SCIENCE, "태양계와 별", "별자리 관찰"), s2(SCIENCE, "생물과 환경", "생태계 조사"),
            a(RECORD, "수행평가 준비", "기준표 먼저 보기"),
            a(READING, "역사책·과학책", "한 달 3권"),
            a(ARTS, "실과", "요리·목공 체험"),
            a(LIFE, "스스로 계획·점검", "주 1회 되돌아보기"),
            s2(CAREER, "좋아하는 것 적기", "잘하는 것·재미있는 것"),
        ),
        "e6" to listOf(
            s1(KOREAN, "비유 표현", "시 쓰기"), s2(KOREAN, "논리적인 글", "주장·근거·반론"),
            s1(MATH, "분수·소수 나눗셈", "주 4회 20분"), s1(MATH, "비와 비율", "백분율"), s2(MATH, "원의 넓이", "원주율"), s2(MATH, "중1 정수 맛보기", "주 2회 20분, 선행은 가볍게"),
            a(ENGLISH, "문법 기초", "주 3회 20분"), a(ENGLISH, "영어 독해", "짧은 글 매일"),
            s1(SOCIETY, "정치·경제", "민주주의·시장"), s2(SOCIETY, "세계 여러 나라", "지구촌 조사"),
            s1(SCIENCE, "빛과 렌즈", "실험"), s2(SCIENCE, "전기의 이용", "회로 만들기"),
            a(RECORD, "수행평가", "기준표 먼저"),
            a(READING, "청소년 소설", "한 달 2권"),
            s2(CAREER, "진로 체험", "관심 분야 하나"),
            s2(LIFE, "중학교 배정 확인", "12월 발표"),
            a(BODY, "사춘기 몸 변화 알기", "가족과 이야기"),
        ),
        "m1" to listOf(
            s1(MATH, "소인수분해·정수", "주 4회 25분"), s1(MATH, "일차방정식", "문장제 연습"), s2(MATH, "좌표와 그래프", "정비례·반비례"), s2(MATH, "기본 도형·작도", "익힘"),
            a(KOREAN, "문학 감상", "시·소설 한 편씩"), a(KOREAN, "설명문 요약", "주 1회"),
            a(ENGLISH, "문법", "주 3회 25분"), a(ENGLISH, "단어 매일", "20개"),
            a(SOCIETY, "지리", "지도·세계"), a(SCIENCE, "힘·빛·물질", "실험 보고서"),
            a(RECORD, "수행평가 기준표 챙기기", "과목마다"),
            s2(EXAM, "첫 지필 준비", "2학기 기말 4주 전부터"),
            s1(CAREER, "자유학기 진로 탐색", "체험 활동 기록"), s1(CAREER, "동아리 정하기", "관심 있는 것"),
            a(LIFE, "스마트폰·수면", "밤 11시 전 잠자리"),
        ),
        "m2" to listOf(
            s1(EXAM, "1학기 중간고사", "4주 전 계획표"), s1(EXAM, "1학기 기말고사", "과목별 범위 정리"), s2(EXAM, "2학기 중간·기말", "4주 전부터"), a(EXAM, "오답노트", "시험 뒤 일주일 안"),
            s1(MATH, "연립방정식", "주 4회 25분"), s2(MATH, "일차함수", "그래프 그리기"), s2(MATH, "확률", "경우의 수"),
            a(ENGLISH, "내신 문법", "주 4회"), a(ENGLISH, "교과서 본문 익히기", "시험 2주 전"),
            a(KOREAN, "문법", "품사·문장 성분"), a(SOCIETY, "역사", "주 2회 25분 정리"), a(SCIENCE, "전기·화학 반응", "실험 정리"),
            a(RECORD, "수행평가", "과목마다 일정표"),
            s2(CAREER, "관심 분야 조사", "직업 하나 깊이"),
            a(LIFE, "수면 7시간", "시험 기간에도"),
        ),
        "m3" to listOf(
            a(EXAM, "내신 마무리", "고입에 반영돼요"), s2(EXAM, "2학기 기말", "마지막 지필"),
            s1(MATH, "제곱근·인수분해", "주 4회 30분"), s2(MATH, "이차방정식·이차함수", "기본 유형"),
            a(ENGLISH, "영어 독해", "주 4회 25분"), a(KOREAN, "비문학 독해", "하루 지문 1개"),
            a(SCIENCE, "운동과 에너지", "정리"), a(SOCIETY, "사회·역사 마무리", "단원 요약"),
            s1(CAREER, "고등학교 종류 알아보기", "일반고·특목고·특성화고"), s2(CAREER, "고교 원서 접수", "10~12월"),
            a(RECORD, "학생부 활동 정리", "봉사·동아리"),
            s2(LIFE, "고교 선행은 조절", "수면과 기초가 먼저"),
        ),
        "h1" to listOf(
            s1(EXAM, "1학기 중간·기말", "4주 전 계획"), s2(EXAM, "2학기 중간·기말", "오답노트"), a(EXAM, "학력평가 3·6·9·11월", "시험 뒤 복기"),
            s1(MATH, "공통수학1", "주 5회 40분"), s2(MATH, "공통수학2", "주 5회 40분"),
            a(KOREAN, "공통국어", "문학·비문학"), a(ENGLISH, "공통영어", "구문·독해"),
            a(SOCIETY, "통합사회·한국사", "단원 정리"), a(SCIENCE, "통합과학·과학탐구실험", "실험 보고서"),
            a(RECORD, "세특 기록", "수업 속 질문·탐구 남기기"), a(RECORD, "동아리 활동", "연간 계획"), a(RECORD, "봉사·자율 활동", "학기마다"),
            s2(CAREER, "2학년 선택과목 정하기", "희망 진로에 맞춰"),
            a(LIFE, "수면·운동", "7시간 · 주 2회"),
        ),
        "h2" to listOf(
            a(EXAM, "선택과목 내신", "4주 전 계획"), a(EXAM, "학력평가·모의고사", "시험 뒤 복기"),
            a(MATH, "수학 선택과목", "주 5회"), a(KOREAN, "국어 선택과목", "주 4회"), a(ENGLISH, "영어 선택과목", "주 4회"),
            a(SOCIETY, "사회 탐구 선택", "개념 정리"), a(SCIENCE, "과학 탐구 선택", "개념 정리"),
            a(RECORD, "탐구 보고서", "학기마다 한 편"), a(RECORD, "세특·동아리", "기록 확인"),
            s2(CAREER, "희망 대학·학과 후보", "5곳 적어 보기"), s2(CAREER, "전형 알아보기", "수시·정시 비율"),
            a(LIFE, "수면 7시간", "협상 불가"),
        ),
        "h3" to listOf(
            s1(EXAM, "3월 학력평가", "현재 위치 확인"), s1(EXAM, "6월 모의평가", "복기 일주일"), s2(EXAM, "9월 모의평가", "수시 지원 기준"), s2(EXAM, "수능", "11월"),
            a(KOREAN, "수능 국어 기출", "하루 지문 3개"), a(MATH, "수능 수학 기출", "하루 70분"), a(ENGLISH, "영어 1등급 유지", "주 4회"),
            a(SOCIETY, "탐구 2과목", "EBS 연계 마무리"),
            s1(RECORD, "학생부 마감 확인", "8월 말까지"),
            s2(CAREER, "수시 6곳 확정", "9월 원서"), s2(CAREER, "면접·서류 준비", "지원 대학별"), s2(CAREER, "정시 지원 계획", "12월"),
            a(LIFE, "수면·컨디션", "수능 시간표대로 생활"),
        ),
        "u" to listOf(
            a(READING, "전공 공부", "주 5회"), a(RECORD, "과제·프로젝트", "마감 일정표"), a(CAREER, "진로·경험", "인턴·대외활동 하나"), a(LIFE, "생활 리듬", "수면·운동"),
        ),
        "g" to listOf(
            a(READING, "논문 읽기", "주 5편"), a(RECORD, "연구·실험", "주간 기록"), a(KOREAN, "논문 쓰기", "주 3회"), a(CAREER, "학회·발표", "학기마다"),
        ),
    )

    private fun a(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how)
    private fun s1(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.FIRST, title, how)
    private fun s2(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.SECOND, title, how)
}
