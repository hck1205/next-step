package com.nextstep.app.domain.curriculum

import com.nextstep.app.domain.journey.JourneyPeriod.Companion.termKey

/**
 * 한국 교육과정(2022 개정, 2024~2027년 학년별 순차 적용) 기준 학기별 과목·단원 카탈로그.
 * "이 시기에 학교에서 이걸 배운다"의 기준선이며, 선행을 권하는 표가 아닙니다. 다음 학기 미리 보기는 화면이 다음 구간을 읽어 보여 줍니다.
 * 교과서 출판사마다 단원 순서가 조금 다르므로 뼈대 단원(essential)만 정확히 맞추고 나머지는 참고입니다.
 */
object CurriculumCatalog {
    private const val KOR = "국어"; private const val MATH = "수학"; private const val ENG = "영어"; private const val SCI = "과학"; private const val SOC = "사회"
    private const val HIST = "역사"; private const val KHIST = "한국사"; private const val INT = "통합교과"

    private val terms: Map<String, TermCurriculum> = listOf(
        term(termKey(1, 1), listOf("한글 소리 내어 읽기", "학교 생활 자립(등교 준비·정리)"), listOf("초등 적응이 우선, 학습은 하루 20분"),
            u(KOR, "한글 놀이·글자와 낱말", listOf("한글", "자음", "모음"), true), u(KOR, "문장으로 말하기·읽기", listOf("문장", "읽기")),
            u(MATH, "9까지의 수", listOf("수", "세기"), true), u(MATH, "여러 가지 모양", listOf("모양")), u(MATH, "덧셈과 뺄셈", listOf("덧셈", "뺄셈", "가르기", "모으기"), true), u(MATH, "비교하기", listOf("비교")), u(MATH, "50까지의 수", listOf("50")),
            u(INT, "학교·봄·가족", listOf("학교", "봄", "가족"))),
        term(termKey(1, 2), listOf("받아쓰기·짧은 글쓰기", "숙제 시간 정하기"), emptyList(),
            u(KOR, "문장 부호와 띄어쓰기", listOf("문장 부호", "띄어쓰기", "받아쓰기"), true), u(KOR, "겪은 일 글로 쓰기", listOf("일기", "글쓰기")),
            u(MATH, "100까지의 수", listOf("100"), true), u(MATH, "덧셈과 뺄셈 (받아올림 없이)", listOf("덧셈", "뺄셈"), true), u(MATH, "시계 보기와 규칙 찾기", listOf("시계", "규칙")), u(MATH, "여러 가지 모양 (평면)", listOf("모양")),
            u(INT, "이웃·가을·겨울", listOf("이웃", "가을", "겨울"))),
        term(termKey(2, 1), listOf("문장 단위 읽기", "알림장 스스로 확인"), emptyList(),
            u(KOR, "낱말의 뜻과 문장 만들기", listOf("낱말", "문장")), u(KOR, "마음을 나타내는 말", listOf("마음")),
            u(MATH, "세 자리 수", listOf("세 자리"), true), u(MATH, "여러 가지 도형", listOf("도형", "삼각형", "사각형")), u(MATH, "덧셈과 뺄셈 (받아올림·받아내림)", listOf("받아올림", "받아내림"), true), u(MATH, "길이 재기 (cm)", listOf("길이", "cm")), u(MATH, "분류하기", listOf("분류")), u(MATH, "곱셈의 뜻", listOf("곱셈", "묶어 세기"), true)),
        term(termKey(2, 2), listOf("동화책 혼자 읽기", "구구단 거꾸로도 말하기"), emptyList(),
            u(KOR, "인물의 마음 짐작하기", listOf("인물", "마음")), u(KOR, "글의 중심 내용", listOf("중심 내용")),
            u(MATH, "네 자리 수", listOf("네 자리")), u(MATH, "곱셈구구", listOf("구구단", "곱셈구구"), true), u(MATH, "길이 재기 (m)", listOf("길이", "m")), u(MATH, "시각과 시간", listOf("시각", "시간"), true), u(MATH, "표와 그래프", listOf("표", "그래프")), u(MATH, "규칙 찾기", listOf("규칙"))),
        term(termKey(3, 1), listOf("영어·과학·사회 시작 적응", "문단 요약"), listOf("3학년부터 영어·과학·사회가 시작돼요. 과목이 늘어난 첫 학기라 습관이 우선"),
            u(KOR, "중심 문장과 문단", listOf("문단", "중심 문장"), true), u(KOR, "국어사전 활용", listOf("사전")),
            u(MATH, "덧셈과 뺄셈 (세 자리)", listOf("덧셈", "뺄셈")), u(MATH, "평면도형", listOf("평면도형", "각", "직각")), u(MATH, "나눗셈", listOf("나눗셈"), true), u(MATH, "곱셈 (두 자리×한 자리)", listOf("곱셈")), u(MATH, "길이와 시간", listOf("길이", "시간")), u(MATH, "분수와 소수", listOf("분수", "소수"), true),
            u(ENG, "알파벳과 인사·자기소개", listOf("알파벳", "인사", "hello"), true), u(SCI, "물질의 성질", listOf("물질")), u(SCI, "동물의 한살이", listOf("한살이", "동물")), u(SCI, "자석의 이용", listOf("자석")), u(SCI, "지구의 모습", listOf("지구")),
            u(SOC, "우리 고장의 모습", listOf("고장")), u(SOC, "교통과 통신 수단의 변화", listOf("교통", "통신"))),
        term(termKey(3, 2), listOf("독서록 쓰기", "분수 개념 말로 설명"), emptyList(),
            u(KOR, "독서 감상문", listOf("독서", "감상문")), u(KOR, "글의 흐름 파악", listOf("흐름")),
            u(MATH, "곱셈 (세 자리×한 자리, 두 자리×두 자리)", listOf("곱셈")), u(MATH, "나눗셈 (나머지)", listOf("나눗셈", "나머지"), true), u(MATH, "원", listOf("원", "지름", "반지름")), u(MATH, "분수 (단위분수·크기 비교)", listOf("분수"), true), u(MATH, "들이와 무게", listOf("들이", "무게", "L", "kg")), u(MATH, "자료의 정리", listOf("자료", "표")),
            u(ENG, "색·숫자·물건 묻고 답하기", listOf("색", "숫자", "what")), u(SCI, "동물의 생활", listOf("동물")), u(SCI, "지표의 변화", listOf("지표", "흙")), u(SCI, "물질의 상태", listOf("고체", "액체", "기체"), true), u(SCI, "소리의 성질", listOf("소리")),
            u(SOC, "환경에 따라 다른 삶의 모습", listOf("환경")), u(SOC, "시대마다 다른 삶의 모습", listOf("시대")), u(SOC, "가족의 모습과 역할 변화", listOf("가족"))),
        term(termKey(4, 1), listOf("비문학 짧은 글 읽기", "주간 계획 스스로"), emptyList(),
            u(KOR, "생각과 느낌을 나누는 글", listOf("느낌")), u(KOR, "설명하는 글 읽기", listOf("설명문"), true),
            u(MATH, "큰 수", listOf("큰 수", "억", "조"), true), u(MATH, "각도", listOf("각도", "각도기"), true), u(MATH, "곱셈과 나눗셈 (세 자리÷두 자리)", listOf("곱셈", "나눗셈")), u(MATH, "평면도형의 이동", listOf("이동", "밀기", "뒤집기")), u(MATH, "막대그래프", listOf("막대그래프")), u(MATH, "규칙 찾기", listOf("규칙")),
            u(ENG, "일상 표현 (시간·날씨·좋아하는 것)", listOf("시간", "날씨", "like")), u(SCI, "지층과 화석", listOf("지층", "화석")), u(SCI, "식물의 한살이", listOf("식물", "한살이")), u(SCI, "물체의 무게", listOf("무게", "저울")), u(SCI, "혼합물의 분리", listOf("혼합물"), true),
            u(SOC, "지역의 위치와 특성", listOf("지도", "위치"), true), u(SOC, "우리 지역의 역사", listOf("지역", "역사")), u(SOC, "지역의 공공 기관과 주민 참여", listOf("공공 기관"))),
        term(termKey(4, 2), listOf("주장하는 글 쓰기", "분수 덧셈·뺄셈 완전 이해"), emptyList(),
            u(KOR, "의견이 드러나는 글", listOf("의견", "주장"), true), u(KOR, "이야기의 흐름과 인물", listOf("이야기")),
            u(MATH, "분수의 덧셈과 뺄셈", listOf("분수", "덧셈", "뺄셈"), true), u(MATH, "삼각형", listOf("삼각형", "이등변", "정삼각형")), u(MATH, "소수의 덧셈과 뺄셈", listOf("소수"), true), u(MATH, "사각형", listOf("사각형", "평행", "수직")), u(MATH, "꺾은선그래프", listOf("꺾은선")), u(MATH, "다각형", listOf("다각형")),
            u(ENG, "지시·요청 표현과 짧은 대화", listOf("대화")), u(SCI, "식물의 생활", listOf("식물")), u(SCI, "물의 상태 변화", listOf("물", "상태 변화"), true), u(SCI, "그림자와 거울", listOf("그림자", "거울")), u(SCI, "화산과 지진", listOf("화산", "지진")), u(SCI, "물의 여행", listOf("물의 순환")),
            u(SOC, "촌락과 도시의 생활", listOf("촌락", "도시")), u(SOC, "필요한 것의 생산과 교환", listOf("생산", "교환", "경제"), true), u(SOC, "사회 변화와 문화 다양성", listOf("문화", "다양성"))),
        term(termKey(5, 1), listOf("약수·배수·통분 완전 이해", "정보글 요약", "오답 노트 시작"), listOf("초5 수학은 중등의 문턱: 약분·통분이 흔들리면 중1 방정식이 어려워요"),
            u(KOR, "글의 구조 파악하기", listOf("구조", "요약"), true), u(KOR, "토의와 토론", listOf("토의", "토론")),
            u(MATH, "자연수의 혼합 계산", listOf("혼합 계산"), true), u(MATH, "약수와 배수", listOf("약수", "배수"), true), u(MATH, "규칙과 대응", listOf("대응")), u(MATH, "약분과 통분", listOf("약분", "통분"), true), u(MATH, "분수의 덧셈과 뺄셈 (분모 다른)", listOf("분수"), true), u(MATH, "다각형의 둘레와 넓이", listOf("둘레", "넓이")),
            u(ENG, "과거 표현·설명하기", listOf("과거", "was")), u(SCI, "온도와 열", listOf("온도", "열")), u(SCI, "태양계와 별", listOf("태양계", "별")), u(SCI, "용해와 용액", listOf("용해", "용액"), true), u(SCI, "다양한 생물과 우리 생활", listOf("생물", "균")),
            u(SOC, "국토와 우리 생활", listOf("국토", "지형", "기후"), true), u(SOC, "인권 존중과 정의로운 사회", listOf("인권", "법"))),
        term(termKey(5, 2), listOf("장편 완독", "시험 계획표 경험"), emptyList(),
            u(KOR, "매체 자료 읽기", listOf("매체")), u(KOR, "글쓴이의 관점", listOf("관점")),
            u(MATH, "수의 범위와 어림하기", listOf("어림", "이상", "이하")), u(MATH, "분수의 곱셈", listOf("분수", "곱셈"), true), u(MATH, "합동과 대칭", listOf("합동", "대칭")), u(MATH, "소수의 곱셈", listOf("소수", "곱셈"), true), u(MATH, "직육면체", listOf("직육면체")), u(MATH, "평균과 가능성", listOf("평균", "가능성")),
            u(ENG, "미래·계획 말하기", listOf("will", "계획")), u(SCI, "생물과 환경", listOf("생태계", "환경"), true), u(SCI, "날씨와 우리 생활", listOf("날씨", "습도")), u(SCI, "물체의 운동", listOf("운동", "속력"), true), u(SCI, "산과 염기", listOf("산", "염기")),
            u(SOC, "옛사람들의 삶과 문화 (선사~조선)", listOf("역사", "고조선", "삼국", "고려", "조선"), true), u(SOC, "사회의 새로운 변화와 오늘날의 우리", listOf("근대", "현대"))),
        term(termKey(6, 1), listOf("비와 비율 완전 이해", "논설문 쓰기"), listOf("비율은 중1 방정식·중2 함수의 기초"),
            u(KOR, "논설문 읽기와 쓰기", listOf("논설문", "주장"), true), u(KOR, "속담과 관용 표현", listOf("속담", "관용")),
            u(MATH, "분수의 나눗셈", listOf("분수", "나눗셈"), true), u(MATH, "각기둥과 각뿔", listOf("각기둥", "각뿔")), u(MATH, "소수의 나눗셈", listOf("소수", "나눗셈"), true), u(MATH, "비와 비율", listOf("비", "비율", "백분율"), true), u(MATH, "여러 가지 그래프", listOf("그래프", "띠그래프", "원그래프")), u(MATH, "직육면체의 부피와 겉넓이", listOf("부피", "겉넓이")),
            u(ENG, "경험 묻고 답하기·간단한 글 읽기", listOf("독해")), u(SCI, "지구와 달의 운동", listOf("지구", "달", "자전", "공전"), true), u(SCI, "여러 가지 기체", listOf("기체", "산소", "이산화탄소")), u(SCI, "식물의 구조와 기능", listOf("식물", "광합성")), u(SCI, "빛과 렌즈", listOf("빛", "렌즈")),
            u(SOC, "우리나라의 정치 발전", listOf("정치", "민주주의"), true), u(SOC, "우리나라의 경제 발전", listOf("경제"))),
        term(termKey(6, 2), listOf("초등 전 범위 오답 점검", "중학 생활 계획"), listOf("중학교 배정 원서(11~12월)와 함께 중1 예습 범위를 정하세요"),
            u(KOR, "글의 관점 비교", listOf("관점")), u(KOR, "중등 문학 용어 맛보기", listOf("문학", "시", "소설")),
            u(MATH, "분수의 나눗셈 (분수÷분수)", listOf("분수", "나눗셈"), true), u(MATH, "소수의 나눗셈", listOf("소수", "나눗셈")), u(MATH, "공간과 입체", listOf("입체", "쌓기나무")), u(MATH, "비례식과 비례배분", listOf("비례식", "비례배분"), true), u(MATH, "원의 넓이", listOf("원", "넓이", "원주율"), true), u(MATH, "원기둥·원뿔·구", listOf("원기둥", "원뿔", "구")),
            u(ENG, "긴 대화·짧은 글 쓰기", listOf("영작")), u(SCI, "전기의 이용", listOf("전기", "회로"), true), u(SCI, "계절의 변화", listOf("계절")), u(SCI, "연소와 소화", listOf("연소", "소화")), u(SCI, "우리 몸의 구조와 기능", listOf("몸", "소화", "호흡", "순환"), true), u(SCI, "에너지와 생활", listOf("에너지")),
            u(SOC, "세계 여러 나라의 자연과 문화", listOf("세계", "나라")), u(SOC, "통일 한국의 미래와 지구촌의 평화", listOf("통일", "지구촌"))),
        term(termKey(7, 1), listOf("개념 노트·오답 노트 습관", "자유학기 진로 탐색"), listOf("중학 첫 학기는 시험보다 노트 습관. 자유학기는 진로 탐색에 쓰세요"),
            u(KOR, "품사와 어휘", listOf("품사", "어휘"), true), u(KOR, "문학의 갈래 (시·소설)", listOf("시", "소설", "갈래"), true), u(KOR, "요약하며 읽기", listOf("요약")), u(KOR, "듣기·말하기 (토의)", listOf("토의")),
            u(MATH, "소인수분해", listOf("소인수분해", "소수", "최대공약수", "최소공배수"), true), u(MATH, "정수와 유리수", listOf("정수", "유리수", "음수"), true), u(MATH, "문자와 식", listOf("문자", "식", "동류항"), true), u(MATH, "일차방정식", listOf("일차방정식", "방정식"), true), u(MATH, "좌표평면과 그래프", listOf("좌표", "그래프", "정비례", "반비례")),
            u(ENG, "be동사·일반동사·시제 기초", listOf("be동사", "일반동사", "시제", "문법"), true), u(ENG, "듣기 평가 유형", listOf("듣기")), u(SCI, "생물의 구성과 다양성", listOf("세포", "생물", "다양성"), true), u(SCI, "열", listOf("열", "비열")), u(SCI, "물질의 상태 변화", listOf("상태 변화", "융해", "기화")), u(SCI, "힘의 작용", listOf("힘", "중력", "마찰력"), true),
            u(SOC, "내가 사는 세계", listOf("지도", "위치")), u(SOC, "자연환경과 인간 생활", listOf("기후", "지형")), u(HIST, "문명의 발생과 고대 국가", listOf("문명", "고조선", "삼국"), true)),
        term(termKey(7, 2), listOf("첫 정기고사 분석", "도형 증명 문장 쓰기"), emptyList(),
            u(KOR, "문장 성분과 짜임", listOf("문장 성분", "주어", "서술어"), true), u(KOR, "비유와 상징", listOf("비유", "상징")), u(KOR, "설명하는 글 쓰기", listOf("설명문")),
            u(MATH, "기본 도형 (점·선·면·각)", listOf("기본 도형", "각")), u(MATH, "작도와 합동", listOf("작도", "합동")), u(MATH, "평면도형의 성질 (다각형·원)", listOf("다각형", "내각", "부채꼴"), true), u(MATH, "입체도형의 성질", listOf("입체도형", "겉넓이", "부피")), u(MATH, "자료의 정리와 해석", listOf("통계", "도수분포", "히스토그램"), true),
            u(ENG, "미래·진행 시제와 조동사", listOf("시제", "조동사"), true), u(ENG, "독해 지문 구조", listOf("독해")), u(SCI, "기체의 성질", listOf("기체", "압력")), u(SCI, "지권의 변화", listOf("지권", "암석", "판")), u(SCI, "빛과 파동", listOf("빛", "파동", "소리")),
            u(SOC, "정치와 민주주의", listOf("민주주의", "선거")), u(SOC, "문화와 사회 변동", listOf("문화")), u(HIST, "남북국 시대와 고려", listOf("통일신라", "발해", "고려"), true)),
        term(termKey(8, 1), listOf("함수 그래프 직접 그리기", "수면 7시간 고정"), emptyList(),
            u(KOR, "음운의 체계", listOf("음운", "자음", "모음"), true), u(KOR, "소설의 시점과 갈등", listOf("시점", "갈등")), u(KOR, "주장하는 글 (논증)", listOf("논증", "근거")),
            u(MATH, "유리수와 순환소수", listOf("순환소수", "유리수"), true), u(MATH, "식의 계산 (지수법칙·다항식)", listOf("지수법칙", "다항식"), true), u(MATH, "일차부등식", listOf("부등식")), u(MATH, "연립일차방정식", listOf("연립방정식"), true), u(MATH, "일차함수와 그래프", listOf("일차함수", "기울기", "절편"), true),
            u(ENG, "to부정사·동명사·수동태", listOf("to부정사", "동명사", "수동태", "문법"), true), u(SCI, "물질의 구성 (원소·원자·이온)", listOf("원소", "원자", "이온"), true), u(SCI, "전기와 자기", listOf("전기", "전류", "자기"), true), u(SCI, "태양계", listOf("태양계", "행성")), u(SCI, "식물과 에너지 (광합성)", listOf("광합성", "호흡")),
            u(SOC, "인권과 헌법", listOf("인권", "헌법")), u(SOC, "경제 생활과 선택", listOf("경제", "시장")), u(HIST, "조선의 성립과 발전", listOf("조선", "세종"), true)),
        term(termKey(8, 2), listOf("고등학교 유형 조사", "닮음·피타고라스 완전 이해"), emptyList(),
            u(KOR, "한글의 창제 원리", listOf("한글", "훈민정음")), u(KOR, "고전 문학 입문", listOf("고전", "시조")), u(KOR, "매체와 비판적 읽기", listOf("매체")),
            u(MATH, "일차함수와 일차방정식의 관계", listOf("일차함수", "연립방정식")), u(MATH, "삼각형의 성질", listOf("삼각형", "이등변", "외심", "내심"), true), u(MATH, "사각형의 성질", listOf("평행사변형", "사각형")), u(MATH, "도형의 닮음", listOf("닮음", "닮음비"), true), u(MATH, "피타고라스 정리", listOf("피타고라스"), true), u(MATH, "확률", listOf("확률", "경우의 수"), true),
            u(ENG, "관계대명사·분사 입문", listOf("관계대명사", "분사"), true), u(ENG, "긴 지문 독해", listOf("독해")), u(SCI, "동물과 에너지 (소화·순환·호흡·배설)", listOf("소화", "순환", "호흡", "배설"), true), u(SCI, "물질의 특성", listOf("밀도", "용해도", "끓는점")), u(SCI, "수권과 해수의 순환", listOf("해수", "수권")), u(SCI, "열과 우리 생활", listOf("열", "비열")),
            u(SOC, "사회 변동과 사회 문제", listOf("사회 문제")), u(SOC, "국제 사회와 국제 정치", listOf("국제")), u(HIST, "조선 후기와 개항", listOf("조선 후기", "개항", "실학"), true)),
        term(termKey(9, 1), listOf("인수분해 속도", "고입 일정 역산"), listOf("특목·자사고는 중3 8~12월 원서. 지금 학교 유형을 정하세요"),
            u(KOR, "문법 요소 (높임·시간·피동)", listOf("높임", "피동", "사동"), true), u(KOR, "현대시·현대소설 심화", listOf("시", "소설")), u(KOR, "논설문 쓰기", listOf("논설문")),
            u(MATH, "제곱근과 실수", listOf("제곱근", "무리수", "실수"), true), u(MATH, "다항식의 곱셈과 인수분해", listOf("곱셈 공식", "인수분해"), true), u(MATH, "이차방정식", listOf("이차방정식", "근의 공식"), true), u(MATH, "이차함수", listOf("이차함수", "포물선", "꼭짓점"), true),
            u(ENG, "관계사·가정법·현재완료 완성", listOf("관계사", "가정법", "현재완료", "문법"), true), u(ENG, "고입 대비 독해 실전", listOf("독해", "어휘")), u(SCI, "화학 반응의 규칙과 에너지 변화", listOf("화학 반응", "질량 보존"), true), u(SCI, "기권과 날씨", listOf("기권", "날씨", "전선")), u(SCI, "운동과 에너지", listOf("속력", "운동 에너지", "일"), true), u(SCI, "자극과 반응", listOf("자극", "신경", "호르몬")),
            u(SOC, "인구·자원·환경 문제", listOf("인구", "자원", "환경")), u(HIST, "근대 국가 수립 운동과 국권 수호", listOf("근대", "국권", "독립운동"), true)),
        term(termKey(9, 2), listOf("중등 전 범위 오답 회독", "고1 공통수학 예습 범위 결정"), listOf("고1 선행은 필요한 만큼만: 공통수학1 다항식·방정식까지가 현실적"),
            u(KOR, "중등 문법 총정리", listOf("문법", "총정리"), true), u(KOR, "고전 시가·고전 소설", listOf("고전")), u(KOR, "비문학 독해 유형", listOf("비문학", "독해")),
            u(MATH, "삼각비", listOf("삼각비", "sin", "cos", "tan"), true), u(MATH, "원의 성질 (원주각·접선)", listOf("원", "원주각", "접선"), true), u(MATH, "통계 (대푯값·산포도·상관관계)", listOf("통계", "평균", "분산", "표준편차"), true),
            u(ENG, "고1 모의고사 지문 맛보기", listOf("모의고사", "독해"), true), u(ENG, "어휘 2000", listOf("어휘")), u(SCI, "생식과 유전", listOf("생식", "유전", "멘델"), true), u(SCI, "에너지 전환과 보존", listOf("에너지", "역학적 에너지"), true), u(SCI, "별과 우주", listOf("별", "우주", "은하")), u(SCI, "과학기술과 인류 문명", listOf("과학기술")),
            u(SOC, "글로벌 경제와 지역 변화", listOf("경제", "세계화")), u(HIST, "일제 강점기와 민족 운동, 대한민국의 발전", listOf("일제", "광복", "대한민국"), true)),
        term(termKey(10, 1), listOf("과목별 목표 등급·시간 배분 설계", "3월 모의고사 분석"), listOf("고교학점제 첫 해: 2학기에 고2 선택과목을 정합니다. 진로와 학과 권장 과목을 지금부터 알아보세요"),
            u(KOR, "공통국어1: 문학·독서·문법 기초", listOf("공통국어", "문학", "독서", "문법"), true),
            u(MATH, "공통수학1: 다항식", listOf("공통수학1", "다항식", "나머지정리", "인수분해"), true), u(MATH, "공통수학1: 방정식과 부등식", listOf("복소수", "이차방정식", "이차함수", "부등식"), true), u(MATH, "공통수학1: 경우의 수", listOf("경우의 수", "순열", "조합"), true), u(MATH, "공통수학1: 행렬", listOf("행렬")),
            u(ENG, "공통영어1: 구문 독해·어휘 3000", listOf("공통영어", "구문", "독해", "어휘"), true), u(SCI, "통합과학1: 물질과 규칙성·시스템과 상호작용", listOf("통합과학", "원소", "지구 시스템"), true), u(SOC, "통합사회1: 인간·사회·환경과 행복", listOf("통합사회", "행복", "자연환경")), u(KHIST, "한국사1: 전근대 한국사의 이해", listOf("한국사", "전근대"), true)),
        term(termKey(10, 2), listOf("선택과목 결정", "생기부 탐구 주제 정하기"), listOf("고2 선택과목(대수·미적분I·확률과 통계·과학 4과목 등)을 이번 학기에 결정합니다"),
            u(KOR, "공통국어2: 화법·작문·매체", listOf("공통국어", "화법", "작문", "매체"), true),
            u(MATH, "공통수학2: 도형의 방정식", listOf("공통수학2", "직선", "원의 방정식", "도형의 이동"), true), u(MATH, "공통수학2: 집합과 명제", listOf("집합", "명제", "절대부등식"), true), u(MATH, "공통수학2: 함수와 그래프", listOf("함수", "합성함수", "역함수", "유리함수", "무리함수"), true),
            u(ENG, "공통영어2: 유형별 독해 (빈칸·순서·삽입)", listOf("공통영어", "빈칸", "순서", "삽입"), true), u(SCI, "통합과학2: 변화와 다양성·환경과 에너지", listOf("통합과학", "진화", "에너지"), true), u(SOC, "통합사회2: 시장·정의·문화·지속가능성", listOf("통합사회", "시장", "정의")), u(KHIST, "한국사2: 근현대 한국사의 이해", listOf("한국사", "근현대", "독립운동"), true)),
        term(termKey(11, 1), listOf("6월 모평으로 위치 확인", "생기부 세특 활동 계획"), listOf("이공계는 미적분I, 인문·상경계도 대수와 확률과 통계가 중심. 선택과목 심화가 시작됩니다"),
            u(KOR, "문학: 갈래별 작품 감상", listOf("문학", "고전시가", "현대시"), true), u(KOR, "독서와 작문 / 화법과 언어", listOf("독서", "작문", "화법", "언어")),
            u(MATH, "대수: 지수함수와 로그함수", listOf("대수", "지수", "로그", "지수함수", "로그함수"), true), u(MATH, "대수: 삼각함수", listOf("삼각함수", "사인법칙", "코사인법칙"), true), u(MATH, "대수: 수열", listOf("수열", "등차", "등비", "수학적 귀납법"), true), u(MATH, "미적분I: 함수의 극한과 연속", listOf("미적분", "극한", "연속"), true), u(MATH, "미적분I: 미분", listOf("미분", "도함수", "접선"), true),
            u(ENG, "영어I: 어휘·구문·듣기", listOf("영어I", "어휘", "듣기"), true), u(SCI, "선택 과학 (물리학·화학·생명과학·지구과학) 중 2과목", listOf("물리학", "화학", "생명과학", "지구과학"), true), u(SOC, "선택 사회 (세계시민과 지리·세계사·경제·정치·법과 사회 등)", listOf("지리", "세계사", "경제", "정치"))),
        term(termKey(11, 2), listOf("EBS 연계 교재 시작", "수시·정시 시뮬레이션"), listOf("11월 모의고사 후 정시 가능권과 수시 6장 후보를 처음 그려 보세요"),
            u(KOR, "EBS 수능특강 문학 작품 정리", listOf("수능특강", "문학"), true), u(KOR, "비문학 유형별 접근 (과학·경제·철학)", listOf("비문학", "독서"), true),
            u(MATH, "미적분I: 적분", listOf("적분", "정적분", "넓이"), true), u(MATH, "확률과 통계: 경우의 수·확률", listOf("확률과 통계", "순열", "조합", "확률"), true), u(MATH, "확률과 통계: 통계", listOf("확률분포", "정규분포", "통계적 추정")),
            u(ENG, "영어II: 긴 지문·추론", listOf("영어II", "추론", "독해"), true), u(SCI, "선택 과학 심화·탐구 보고서", listOf("탐구", "보고서")), u(SOC, "선택 사회 심화·생기부 탐구", listOf("탐구"))),
        term(termKey(12, 1), listOf("6월 모평 후 수시·정시 확정", "실전 모의 주 1회"), listOf("수시 6장 후보 리스트를 6월 모평 결과로 확정. 학생부는 이번 학기가 마지막 기록"),
            u(KOR, "수능 국어 기출 5개년", listOf("기출", "수능"), true), u(KOR, "수능완성·실전 모의", listOf("수능완성", "실전")),
            u(MATH, "미적분II: 수열의 극한·미분법·적분법 (이공계)", listOf("미적분II", "수열의 극한", "미분법", "적분법"), true), u(MATH, "기하: 이차곡선·평면벡터·공간도형 (선택)", listOf("기하", "이차곡선", "벡터")), u(MATH, "수능 수학 기출 3개년", listOf("기출", "수능"), true),
            u(ENG, "수능 영어 기출·EBS 연계", listOf("기출", "수능특강", "수능완성"), true), u(SCI, "선택 과학 기출·실전", listOf("기출")), u(SOC, "선택 사회 기출·실전", listOf("기출"))),
        term(termKey(12, 2), listOf("9월 모평 분석 → 수능 → 정시", "수능 후 대학 생활 준비"), listOf("수능 원서(8월 말~9월 초), 수시(9월), 수능(11월), 정시(12월 말~1월 초)"),
            u(KOR, "실전 모의·오답 회독", listOf("실전", "오답"), true), u(MATH, "실전 모의·오답 회독", listOf("실전", "오답"), true), u(ENG, "실전 모의·듣기 만점", listOf("실전", "듣기"), true), u(SCI, "실전 모의", listOf("실전")), u(SOC, "실전 모의", listOf("실전"))),
    ).associateBy { it.periodKey }

    /** 구간 키(학기)의 커리큘럼. 학령 전·대학 구간은 null. */
    fun forPeriod(periodKey: String?): TermCurriculum? = periodKey?.let { terms[it] }

    val periodKeys: List<String> get() = terms.keys.toList()

    private fun term(key: String, competencies: List<String>, startNow: List<String>, vararg units: CurriculumUnit) =
        TermCurriculum(key, units.toList(), competencies, startNow)

    private fun u(subject: String, title: String, keywords: List<String> = emptyList(), essential: Boolean = false) =
        CurriculumUnit(subject, title, keywords, essential)
}
