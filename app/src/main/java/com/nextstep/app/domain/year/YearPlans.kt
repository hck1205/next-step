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
 * 학령 전은 누가 하는지([YearDoer])를 꼭 적습니다: 만 0세는 전부 부모가, 만 1~2세는 부모가·같이, 만 3세부터 "스스로"가 생깁니다.
 * 학교부터는 2022 개정 교육과정의 학기별 단원, 성취평가(중)·내신 5등급(고)·창체 3영역, 2028 수능 개편을 따르고,
 * 수면은 권장 시간(초 9~12시간, 중·고 8~10시간) 아래로 적지 않습니다.
 * 영유아 검진 회차·월령은 국민건강보험 영유아 건강검진(1~8차, 구강 1~3차) 일정을 따릅니다.
 */
object YearPlans {
    // plans 보다 먼저 초기화되어야 해서 맨 위에 둡니다.
    private val P = YearDoer.PARENT
    private val T = YearDoer.TOGETHER

    /** 그 해 할 일 전부: 학업·생활([plans]) + 건강([HealthPlans]) + 부모의 지원·서류·상담([ParentPlans]) + 멘토 코칭([MentorPlans]). */
    fun forYear(key: String): List<YearTask> =
        plans[key].orEmpty() + HealthPlans.forYear(key) + ParentPlans.forYear(key) + MentorPlans.forYear(key)

    /** 그 해에 할 일이 있는 분류(탭 순서). */
    fun areasOf(key: String): List<YearArea> = forYear(key).map { it.area }.distinct().sortedBy { it.ordinal }

    private val plans: Map<String, List<YearTask>> = mapOf(
        // 만 0세(0~12개월): 공부는 없어요. 전부 엄마·아빠가 해 주는 일이고 월령을 적어 둡니다.
        "a0" to listOf(
            pa(TALK, "눈 맞추고 말 걸기", "기저귀 갈 때·수유할 때 지금 하는 일을 말로"), pa(TALK, "옹알이에 대답하기", "4개월 무렵부터 아이 소리를 따라 해 주기"),
            pa(TALK, "초점책·헝겊책 보여 주기", "0~3개월 흑백 초점책, 6개월부터 헝겊책(물고 빨아도 괜찮아요)"), pa(TALK, "노래·자장가 불러 주기", "같은 노래를 하루 여러 번"),
            ta(PLAY, "엎드려 놀기(터미타임)", "깨어 있을 때 하루 몇 번, 목·어깨 힘 기르기"), ta(PLAY, "손 뻗어 잡기", "4~6개월, 장난감을 손 닿는 곳에"), ta(PLAY, "까꿍 놀이", "6~9개월, 사라졌다 나타나는 놀이"),
            pa(LIFE, "이유식 시작", "만 6개월 무렵, 한 숟가락부터"), pa(LIFE, "바로 눕혀 재우기", "딱딱한 매트, 푹신한 이불·베개 없이"), pa(LIFE, "영상 보여 주지 않기", "만 2세 전에는 화면 0분이 권장"),
        ),
        // 만 1세(12~24개월): 걷고 첫 단어가 나와요. 부모가 읽어 주고 말을 늘려 줍니다.
        "a1" to listOf(
            pa(TALK, "그림책 읽어 주기", "하루 10분, 그림을 가리키며 이름 말해 주기"), pa(TALK, "아이 말에 한 단어 붙여 주기", "\"물\" → \"물 줄까?\""), ta(TALK, "몸짓에 말로 대답하기", "가리키기·손 흔들기·고개 젓기"),
            ta(PLAY, "매일 바깥 걷기", "30분, 걷기가 서툴러도 괜찮아요"), ta(PLAY, "쌓기·넣었다 빼기", "컵·블록·통"), ta(PLAY, "끼적이기", "18개월 무렵, 굵은 크레용으로"),
            ta(LIFE, "컵으로 마시기·숟가락 잡기", "흘려도 스스로 해 보게"), pa(LIFE, "젖병·밤중 수유 줄이기", "돌 전후부터 천천히"), pa(LIFE, "영상 없이 지내기", "만 2세 전 화면 0분"),
        ),
        // 만 2세(24~36개월): 말이 트여요. 영어보다 한국어 대화가 먼저인 해.
        "a2" to listOf(
            pa(TALK, "그림책 2~3권 읽어 주기", "아이가 고른 책, 같은 책 반복도 좋아요"), ta(TALK, "두 단어를 세 단어로 늘려 주기", "\"차 가\" → \"빨간 차가 가네\""), ta(TALK, "노래·손유희", "율동 따라 하기"),
            ta(PLAY, "역할 놀이", "소꿉·인형·병원 놀이"), ta(PLAY, "뛰기·점프·공 차기", "놀이터 하루 1시간"), ta(PLAY, "퍼즐·모양 맞추기", "3~4조각부터"), ca(PLAY, "끼적이기·동그라미 그리기", "큰 종이에 자유롭게"),
            ca(LIFE, "혼자 숟가락으로 먹기", "흘려도 괜찮아요"), ta(LIFE, "배변 연습", "아이가 신호를 보일 때 시작"), pa(LIFE, "화면은 하루 1시간 안", "만 2세부터, 부모와 함께 보기"),
        ),
        // 만 3세: 누리과정(놀이 중심) 시작. 읽기는 여전히 읽어 주는 것.
        "a3" to listOf(
            ta(TALK, "함께 읽기 15분", "읽고 나서 \"왜 그랬을까?\" 물어보기"), ca(TALK, "오늘 있었던 일 말하기", "저녁에 한 가지씩"), ta(TALK, "말놀이·수수께끼", "차 안·식탁에서"),
            ta(ENGLISH, "영어 노래·그림책", "하루 10분 안, 놀이로만"),
            ca(PLAY, "블록·퍼즐 끝까지", "스스로 완성해 보기"), ca(PLAY, "가위·풀로 만들기", "안전 가위, 선 따라 자르기"), ta(PLAY, "숫자 세며 놀기", "계단·과일 세기, 10까지"),
            ta(BODY, "바깥놀이 1시간", "놀이터·산책"), 
            ca(LIFE, "스스로 옷 입기", "단추 없는 옷부터"), ca(LIFE, "장난감 정리", "놀고 나서 제자리"), YearTask(LIFE, YearTerm.FIRST, "유치원·어린이집 적응", "3~4월, 짧게 시작해 늘리기", YearDoer.TOGETHER),
        ),
        // 만 4세: 호기심의 해. 글자는 읽기보다 알아보기, 수는 세기와 비교.
        "a4" to listOf(
            ta(TALK, "함께 읽기 15분", "읽은 뒤 이야기 바꿔 말하기"), ca(TALK, "끝말잇기·말놀이", "차 안·식탁에서"), ca(TALK, "이름 글자 알아보기", "자기·가족 이름부터, 쓰기는 아직"),
            ta(MATH, "수 세기·많고 적음", "20까지, 간식 나누기로"), ta(MATH, "모양 찾기", "동그라미·세모·네모를 집 안에서"),
            ta(ENGLISH, "영어 노래·그림책", "주 5회 10분"),
            ca(PLAY, "그리기·만들기", "주 3번"), ta(PLAY, "보드게임", "순서 지키기·지는 연습"),
            ta(BODY, "자전거·킥보드", "주말마다, 헬멧 쓰고"),
            ca(LIFE, "혼자 양치·세수", "마무리는 어른이"), pa(LIFE, "학원·수업은 주 2~3개까지", "시험 보는 곳은 피하기"),
        ),
        // 만 5세: 글자에 관심이 생기는 해. 한글은 놀이로, 받침 없는 글자부터.
        "a5" to listOf(
            ta(TALK, "함께 읽기 20분", "잠들기 전"), ca(TALK, "한글 놀이", "간판·이름 글자, 받침 없는 글자부터"), ca(TALK, "그림일기(그림 + 한 단어)", "주 1~2회"),
            ta(MATH, "가르기·모으기", "10까지, 구슬·손가락으로"), ta(MATH, "시계·달력 보기 놀이", "오늘 요일·몇 시"),
            ta(ENGLISH, "영어 그림책", "주 3회 10분"),
            ca(PLAY, "좋아하는 활동 찾기", "그림·악기·운동 중에서 체험"),
            ca(LIFE, "혼자 씻기·옷 개기", "매일 조금씩"), pa(LIFE, "레벨테스트 보는 학원 피하기", "유아 레벨테스트는 2026년 10월부터 금지"),
        ),
        // 만 6세(입학 전 해): 한글은 초1 국어가 처음부터 가르쳐요. 입학 준비는 생활 습관.
        "a6" to listOf(
            ta(TALK, "매일 함께 읽기 20분", "읽어 주다가 한 줄씩 번갈아"), ca(TALK, "한글 읽기", "받침 없는 글자부터, 흥미만 붙이기"),
            ta(MATH, "10 가르기·모으기", "구슬·손가락으로"), ta(MATH, "20까지 세고 읽기", "달력·엘리베이터 숫자"),
            ca(PLAY, "좋아하는 것 하나 정하기", "그림·악기·운동 중에서"),
            ca(LIFE, "20분 앉아 있기", "그림책·퍼즐로 연습"), ca(LIFE, "혼자 화장실·옷 입기", "학교생활의 기본"),
        ),
        // 초1: 국어 시간을 늘려 한글을 처음부터 가르쳐요(2022 개정). 적응·읽기·잠이 먼저.
        "e1" to listOf(
            s1(LIFE, "학교 적응", "3월 입학 초기 적응 활동, 화장실·급식 익히기", T),
            s1(KOREAN, "한글 익히기", "자음·모음과 받침 없는 글자, 하루 10분 따라 읽기"), s2(KOREAN, "받침 있는 글자 읽고 쓰기", "교과서 소리 내어 읽기"),
            s2(KOREAN, "받아쓰기", "학교마다 달라요, 주 1회 10문제 정도", T),
            s1(MATH, "9까지 수·모으기와 가르기", "구슬·손가락으로"), s1(MATH, "50까지의 수", "수 모형·달력으로"),
            s2(MATH, "100까지의 수와 덧셈·뺄셈", "수학 익힘 주 3회 10분"), s2(MATH, "시계 보기", "몇 시·몇 시 30분"),
            a(READING, "매일 소리 내어 읽기", "10분, 읽어 주기와 번갈아", T),
            a(ARTS, "통합교과 활동 이어 하기", "학교에서 만든 것을 집에서 이야기"),
            a(LIFE, "알림장 확인·가방 싸기", "전날 밤, 처음엔 같이", T), a(LIFE, "9시 전에 자기", "초등학생 권장 수면 9~12시간", T),
            a(BODY, "줄넘기", "하루 10번부터"), a(BODY, "바깥 놀이 하루 1시간", "학원보다 놀이터", T),
        ),
        // 초2: 곱셈의 뜻과 곱셈구구, 혼자 읽기로 넘어가는 해.
        "e2" to listOf(
            s1(KOREAN, "문장부호·띄어쓰기", "마침표·물음표·느낌표"), a(KOREAN, "일기 쓰기", "주 2회 세 문장"), a(KOREAN, "받아쓰기", "학교마다 달라요", T),
            s1(MATH, "세 자리 수", "수 모형"), s1(MATH, "받아올림 덧셈·받아내림 뺄셈", "주 3회 10분"), s1(MATH, "길이 재기(cm)", "자로 재 보기"), s1(MATH, "곱셈의 뜻", "몇씩 몇 묶음"),
            s2(MATH, "곱셈구구", "2단부터 9단까지 하루 한 단"), s2(MATH, "네 자리 수·시각과 시간", "달력·시계로"),
            a(READING, "혼자 읽기 15분", "매일"), s2(READING, "글밥 있는 책으로 넘어가기", "한 쪽에 다섯 줄 이상"),
            a(ENGLISH, "영어 노래·그림책", "학교 영어는 3학년부터, 지금은 주 2~3회 노출만", T),
            a(LIFE, "숙제 스스로 하기", "학교 다녀와서 바로"), a(LIFE, "시간표 보고 가방 싸기", "전날 밤"),
            a(BODY, "줄넘기·두발자전거", "주말마다"),
        ),
        // 초3: 사회·과학·영어가 교과로 시작돼요.
        "e3" to listOf(
            s1(KOREAN, "문단과 중심 문장", "문단마다 한 줄"), a(KOREAN, "국어사전 찾기", "모르는 낱말 하루 하나"), a(KOREAN, "독서록", "주 1회 세 줄"),
            s1(MATH, "세 자리 덧셈·뺄셈", "익힘책"), s1(MATH, "나눗셈의 뜻", "똑같이 나누기"), s1(MATH, "분수와 소수 처음", "색종이·피자로"),
            s2(MATH, "곱셈(두 자리)·나눗셈", "주 3회 15분"), s2(MATH, "원·들이와 무게", "컵·저울로"),
            s1(ENGLISH, "알파벳·파닉스", "학교 영어 시작, 주 5회 10분"), a(ENGLISH, "교과서 듣기", "매일 10분"),
            a(SOCIETY, "우리 고장 알기", "동네 지도 그리기, 옛날과 오늘 비교"),
            a(SCIENCE, "관찰 기록", "주 1회 그림과 한 줄"),
            a(READING, "한 달 4권", "역사·과학 책 섞기"),
            a(ARTS, "리코더", "주 2회 10분"),
            a(BODY, "생존수영", "학교 수업 따라"),
            a(LIFE, "스스로 계획표", "주 1회 같이 점검", T),
        ),
        // 초4: 큰 수·각도·분수 덧셈. 단원평가 방식은 학교마다 달라요.
        "e4" to listOf(
            s1(KOREAN, "요약하기", "문단마다 한 줄"), s2(KOREAN, "의견 쓰기", "까닭 두 가지"), a(KOREAN, "독서록", "주 1회"),
            s1(MATH, "큰 수", "만·억·조"), s1(MATH, "각도", "각도기"), s1(MATH, "곱셈·나눗셈", "세 자리 × 두 자리, ÷ 두 자리"),
            s2(MATH, "분수 덧셈·뺄셈", "분모가 같은 분수"), s2(MATH, "소수 덧셈·뺄셈", "소수 두 자리"), s2(MATH, "삼각형·사각형", "이름과 성질"),
            a(ENGLISH, "영어 문장 읽기", "주 5회 10분"), s2(ENGLISH, "짧은 글 쓰기", "세 문장 자기소개"),
            s1(SOCIETY, "우리 지역과 공공기관", "지역 지도·주민 참여"), s2(SOCIETY, "촌락과 도시·경제 기초", "용돈 기입장"),
            a(SCIENCE, "실험 결과 정리", "관찰한 것 → 까닭 한 줄"),
            a(EXAM, "단원평가 복습", "틀린 문제 다시 풀기"),
            a(READING, "한 달 4권", "고전·과학 읽기"),
            a(LIFE, "스마트폰 규칙", "가족과 함께 정하기", T),
            a(BODY, "운동 하나 꾸준히", "주 2회"),
        ),
        // 초5: 약수·배수·분수가 한꺼번에, 한국사가 시작돼요.
        "e5" to listOf(
            s1(KOREAN, "토의·토론", "근거 들어 말하기"), s1(KOREAN, "글의 구조", "처음·가운데·끝"), s2(KOREAN, "논설문", "주장과 근거"),
            s1(MATH, "약수와 배수", "주 4회 20분"), s1(MATH, "약분과 통분", "최소공배수 활용"), s1(MATH, "분수 덧셈·뺄셈", "분모가 다른 분수"), s1(MATH, "다각형의 넓이", "공식이 나온 까닭 이해"),
            s2(MATH, "분수·소수의 곱셈", "익힘책"), s2(MATH, "합동과 대칭·평균", "종이 접기·기록 평균 내기"),
            a(ENGLISH, "영어 읽기", "주 5회 15분"), s2(ENGLISH, "문법 기초", "be동사·일반동사"),
            s1(SOCIETY, "국토와 우리 생활", "지도·기후"), s2(SOCIETY, "한국사", "옛사람들의 삶, 주 2회 20분"),
            s1(SCIENCE, "온도와 열", "실험 보고서"), s1(SCIENCE, "태양계와 별", "별자리 관찰"), s2(SCIENCE, "생물과 환경", "생태계 조사"),
            a(RECORD, "수행평가 준비", "기준표 먼저 보기"),
            a(READING, "역사책·과학책", "한 달 3권"),
            a(ARTS, "실과", "요리·목공 체험"),
            a(LIFE, "10시 전에 자기", "권장 수면 9~12시간", T),
            s2(CAREER, "좋아하는 것 적기", "잘하는 것·재미있는 것"),
        ),
        // 초6: 비와 비율·원의 넓이. 중학 준비는 선행보다 초등 분수·비율 다지기.
        "e6" to listOf(
            s1(KOREAN, "비유 표현", "시 쓰기"), s2(KOREAN, "논리적인 글", "주장·근거·반론"),
            s1(MATH, "분수·소수의 나눗셈", "주 4회 20분"), s1(MATH, "비와 비율", "백분율"), s1(MATH, "여러 가지 그래프·부피", "띠·원그래프, 직육면체 부피"),
            s2(MATH, "비례식과 비례배분", "요리 양 나누기"), s2(MATH, "원의 넓이", "원주율"), s2(MATH, "중1 수학 가볍게 맛보기", "겨울방학에 주 2회 20분"),
            a(ENGLISH, "문법 기초", "주 3회 20분"), a(ENGLISH, "영어 독해", "짧은 글 매일"),
            s1(SOCIETY, "민주주의와 경제", "선거·시장"), s2(SOCIETY, "세계 여러 나라", "지구촌 조사"),
            s1(SCIENCE, "빛과 렌즈", "실험"), s2(SCIENCE, "전기의 이용", "회로 만들기"),
            a(RECORD, "수행평가", "기준표 먼저"),
            a(READING, "청소년 소설", "한 달 2권"),
            s2(CAREER, "진로 체험", "관심 분야 하나"),
            a(BODY, "사춘기 몸 변화 알기", "보건 수업과 함께, 가족과 이야기", T),
            a(LIFE, "밤 10시 이후 화면 끄기", "잠이 키와 집중력을 지켜요", T),
        ),
        // 중1: 자유학기(학교마다 1학기 또는 2학기)에는 지필 시험이 없어요.
        "m1" to listOf(
            s1(CAREER, "자유학기 진로 탐색", "학교마다 1학기 또는 2학기, 체험 활동 기록"), s1(CAREER, "동아리 정하기", "관심 있는 것"),
            s1(MATH, "소인수분해·정수와 유리수", "주 4회 25분"), s1(MATH, "문자와 식·일차방정식", "문장제 연습"),
            s2(MATH, "좌표평면과 그래프", "정비례·반비례"), s2(MATH, "기본 도형·작도와 합동", "익힘"), s2(MATH, "평면도형·입체도형", "겉넓이·부피"),
            a(KOREAN, "문학 감상", "시·소설 한 편씩"), a(KOREAN, "설명문 요약", "주 1회"),
            a(ENGLISH, "문법", "주 3회 25분"), a(ENGLISH, "단어 매일", "20개"),
            a(SOCIETY, "지리", "지도·세계"), a(SCIENCE, "힘·빛·물질", "실험 보고서"),
            a(RECORD, "수행평가 기준표 챙기기", "과목마다"),
            s2(EXAM, "첫 지필 준비", "자유학기가 끝난 학기, 4주 전부터"),
            a(LIFE, "밤 11시 전 잠자리", "청소년 권장 수면 8~10시간"),
        ),
        // 중2: 첫 지필이 본격적으로. 성취평가(A~E)라 친구와 경쟁하는 시험이 아니에요.
        "m2" to listOf(
            s1(EXAM, "1학기 중간·기말", "4주 전 계획표"), s2(EXAM, "2학기 중간·기말", "과목별 범위 정리"), a(EXAM, "오답노트", "시험 뒤 일주일 안에"),
            s1(MATH, "유리수와 순환소수·식의 계산", "주 4회 25분"), s1(MATH, "연립방정식·부등식", "문장제"), s2(MATH, "일차함수", "그래프 그리기"),
            s2(MATH, "도형의 성질·닮음·피타고라스", "증명 따라 쓰기"), s2(MATH, "경우의 수와 확률", "표로 세기"),
            a(ENGLISH, "내신 문법", "주 4회"), a(ENGLISH, "교과서 본문 익히기", "시험 2주 전"),
            a(KOREAN, "문법", "품사·문장 성분"), a(SOCIETY, "역사", "주 2회 25분 정리"), a(SCIENCE, "전기·화학 반응", "실험 정리"),
            a(RECORD, "수행평가", "과목마다 일정표"),
            s2(CAREER, "관심 분야 조사", "직업 하나 깊이"),
            a(LIFE, "수면 8시간", "시험 기간에도, 권장 8~10시간"),
        ),
        // 중3: 고입의 해. 중3 수학(인수분해·이차함수)이 고1 수학의 절반이에요.
        "m3" to listOf(
            a(EXAM, "내신 마무리", "특목·자사·특성화고는 중학 내신을 봐요"), s2(EXAM, "2학기 기말", "마지막 지필"),
            s1(MATH, "제곱근과 실수·인수분해", "주 4회 30분"), s1(MATH, "이차방정식", "기본 유형"), s2(MATH, "이차함수", "그래프 그리기"), s2(MATH, "삼각비·원의 성질", "그림 그려 풀기"),
            a(ENGLISH, "영어 독해", "주 4회 25분"), a(KOREAN, "비문학 독해", "하루 지문 1개"),
            a(SCIENCE, "운동과 에너지", "정리"), a(SOCIETY, "사회·역사 마무리", "단원 요약"),
            s1(CAREER, "고등학교 종류 알아보기", "일반고·특목고·자사고·특성화고", T), 
            s2(CAREER, "고교 원서 접수", "특목·자사고 12월 전후, 일반고 12월", T), s2(CAREER, "진로연계교육", "2학기 말, 고교 선택과목 미리 보기"),
            a(RECORD, "학생부 활동 정리", "동아리·진로 활동"),
            s2(LIFE, "고교 선행은 조절", "수면 8시간과 중학 개념이 먼저"),
        ),
        // 고1: 공통과목과 내신 5등급제(1등급 = 상위 10%). 창체는 자율·자치 · 동아리 · 진로 3영역.
        "h1" to listOf(
            s1(EXAM, "1학기 중간·기말", "내신 5등급제, 4주 전 계획"), s2(EXAM, "2학기 중간·기말", "오답노트"), a(EXAM, "학력평가 3·6·9·11월", "시험 뒤 복기"),
            s1(MATH, "공통수학1", "주 5회 40분"), s2(MATH, "공통수학2", "주 5회 40분"),
            a(KOREAN, "공통국어1·2", "문학·비문학"), a(ENGLISH, "공통영어1·2", "구문·독해"),
            a(SOCIETY, "통합사회·한국사", "단원 정리"), a(SCIENCE, "통합과학·과학탐구실험", "실험 보고서"),
            a(RECORD, "수업 속 질문·탐구", "세특은 선생님이 수업 모습을 보고 써요"), a(RECORD, "동아리 활동", "연간 계획"), a(RECORD, "자율·자치·진로 활동", "학기마다 한 가지 깊이"),
            a(LIFE, "수면 8시간·운동", "권장 8~10시간, 운동 주 2회"),
        ),
        // 고2: 선택과목의 해. 2028 수능부터 국어·수학·탐구에 선택과목이 없어요.
        "h2" to listOf(
            a(EXAM, "선택과목 내신", "4주 전 계획"), a(EXAM, "학력평가 3·6·9·11월", "시험 뒤 복기"),
            a(MATH, "대수·미적분Ⅰ·확률과 통계", "고른 과목 주 5회"), a(KOREAN, "문학·독서와 작문·화법과 언어", "고른 과목 주 4회"), a(ENGLISH, "영어Ⅰ·Ⅱ·독해와 작문", "주 4회"),
            a(SOCIETY, "사회 선택과목", "진로에 맞춰 개념 정리"), a(SCIENCE, "과학 선택과목", "진로에 맞춰 개념 정리"),
            a(RECORD, "탐구 보고서", "학기마다 한 편"), a(RECORD, "세특·동아리", "기록 확인"),
            s2(CAREER, "희망 대학·학과 후보", "5곳 적어 보기", T), s2(CAREER, "전형 알아보기", "수시·정시 비율", T),
            a(LIFE, "수면 8시간", "줄이지 않아요"),
        ),
        // 고3: 모평·수능·원서의 해.
        "h3" to listOf(
            s1(EXAM, "3월 학력평가", "현재 위치 확인"), s1(EXAM, "6월 모의평가", "복기 일주일"), s2(EXAM, "9월 모의평가", "수시 지원 기준"), s2(EXAM, "수능", "11월"),
            a(KOREAN, "수능 국어 기출", "하루 지문 3개"), a(MATH, "수능 수학 기출", "하루 70분"), a(ENGLISH, "영어 1등급 유지", "주 4회"),
            a(SOCIETY, "탐구 영역", "2027 수능까지 선택 2과목, 2028부터 통합사회·통합과학"),
            s1(RECORD, "학생부 마감 확인", "8월 말까지"),
            s2(CAREER, "수시 6곳 확정", "9월 원서", T), s2(CAREER, "면접·서류 준비", "지원 대학별"), s2(CAREER, "정시 지원 계획", "12월 말~1월 초 원서", T),
            a(LIFE, "수면·컨디션", "수능 시간표대로 생활, 8시간 목표"),
        ),
        "u" to listOf(
            a(READING, "전공 공부", "주 5회"), a(RECORD, "과제·프로젝트", "마감 일정표"), a(CAREER, "진로·경험", "인턴·대외활동 하나"), a(LIFE, "생활 리듬", "수면·운동"),
        ),
        "g" to listOf(
            a(READING, "논문 읽기", "주 5편"), a(RECORD, "연구·실험", "주간 기록"), a(KOREAN, "논문 쓰기", "주 3회"), a(CAREER, "학회·발표", "학기마다"),
        ),
    )

    private fun a(area: YearArea, title: String, how: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.ALL_YEAR, title, how, who)
    private fun pa(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how, YearDoer.PARENT)
    private fun ta(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how, YearDoer.TOGETHER)
    private fun ca(area: YearArea, title: String, how: String) = YearTask(area, YearTerm.ALL_YEAR, title, how, YearDoer.CHILD)
    private fun s1(area: YearArea, title: String, how: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.FIRST, title, how, who)
    private fun s2(area: YearArea, title: String, how: String, who: YearDoer = YearDoer.CHILD) = YearTask(area, YearTerm.SECOND, title, how, who)
}
