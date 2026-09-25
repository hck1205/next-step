package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.YearArea.BODY
import com.nextstep.app.domain.year.YearArea.LIFE
import com.nextstep.app.domain.year.YearArea.MIND
import com.nextstep.app.domain.year.YearDoer.CHILD
import com.nextstep.app.domain.year.YearDoer.PARENT
import com.nextstep.app.domain.year.YearDoer.TOGETHER
import com.nextstep.app.domain.year.YearTerm.ALL_YEAR
import com.nextstep.app.domain.year.YearTerm.FIRST
import com.nextstep.app.domain.year.YearTerm.SECOND

/**
 * 해마다 건강 체크리스트(몸·마음). 월령·학년은 아래 공식 일정을 따릅니다. 바뀌면 여기만 고칩니다.
 * - 질병관리청 2026 국가예방접종 지침·표준 예방접종 일정(인플루엔자 6개월~13세 무료, HPV 12세 여아 + 2026년 5월부터 12세 남아).
 * - 국민건강보험 영유아 건강검진 1~8차(14~35일 · 4~6 · 9~12 · 18~24 · 30~36 · 42~48 · 54~60 · 66~71개월), 구강검진 1~3차(18~29 · 42~53 · 54~65개월).
 * - 학교건강검사규칙: 건강검진은 초1·초4·중1·고1(검진기관), 나머지 학년은 학교의 신체발달·건강조사.
 * - 학생정서·행동특성검사와 인터넷·스마트폰 이용습관 진단: 초1·초4·중1·고1(초등은 부모가 설문).
 * - 권장 수면(미국수면의학회): 1~2세 11~14시간, 3~5세 10~13시간, 6~12세 9~12시간, 13~18세 8~10시간. 신체활동(WHO): 5~17세 하루 60분.
 * 진단이 아니라 "이 나이에 챙길 것" 목록이며, 이상이 보이면 소아청소년과 상담이 먼저입니다.
 */
internal object HealthPlans {
    fun forYear(key: String): List<YearTask> = plans[key].orEmpty()

    private val FLU = x(BODY, SECOND, PARENT, "인플루엔자 접종", "매년 10~11월, 생후 6개월~만 13세 무료")
    private val DENTAL = x(BODY, ALL_YEAR, PARENT, "치과 정기검진", "6개월마다, 불소 도포 함께")
    private val PUBERTY_EARLY = x(BODY, ALL_YEAR, PARENT, "성조숙 신호 살피기", "여아 8세 전 가슴 멍울, 남아 9세 전 고환 커짐이면 소아내분비 상담")
    private val MYOPIA = x(BODY, ALL_YEAR, TOGETHER, "근시 늦추기", "하루 2시간 바깥 활동, 가까이 보기 40분마다 쉬기")
    private val MOVE = x(BODY, ALL_YEAR, CHILD, "하루 60분 몸 쓰기", "숨이 찰 정도로, 주 3회는 격하게(WHO 권고)")
    private val SCHOOL_CHECK = x(BODY, FIRST, PARENT, "학생 건강검진", "검진기관에서, 학교 안내문 확인")
    private val SCHOOL_SURVEY = x(BODY, FIRST, CHILD, "신체발달·건강조사", "학교에서 키·몸무게·시력 측정")
    private val DEPRESSION = x(MIND, ALL_YEAR, PARENT, "우울 신호 알기", "2주 넘는 무기력·짜증·잠 변화면 Wee클래스나 청소년상담 1388")

    private val plans: Map<String, List<YearTask>> = mapOf(
        "a0" to listOf(
            x(BODY, ALL_YEAR, PARENT, "B형간염 1·2·3차", "출생 직후 · 1개월 · 6개월"),
            x(BODY, ALL_YEAR, PARENT, "BCG(결핵)", "생후 4주 안"),
            x(BODY, ALL_YEAR, PARENT, "DTaP·IPV·Hib·폐렴구균 1~3차", "2 · 4 · 6개월(IPV 3차는 6~18개월)"),
            x(BODY, ALL_YEAR, PARENT, "로타바이러스", "2 · 4(· 6)개월, 먹는 백신"),
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 1차", "생후 14~35일"),
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 2차", "4~6개월"),
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 3차", "9~12개월"),
            x(BODY, ALL_YEAR, PARENT, "신생아 선별검사 결과", "청각·선천성 대사이상, 재검이면 3개월 안에"),
            x(BODY, ALL_YEAR, PARENT, "발달 이정표", "4개월 목 가누기 · 6개월 뒤집기 · 9개월 앉기 · 12개월 잡고 서기"),
            x(BODY, ALL_YEAR, PARENT, "열날 때 기준", "3개월 미만 38℃ 이상이면 바로 병원"),
            x(BODY, ALL_YEAR, PARENT, "모유 수유아 비타민 D", "하루 400IU"),
            x(BODY, ALL_YEAR, PARENT, "첫 이 닦기", "첫 이가 나면 거즈·실리콘 칫솔, 쌀알만큼 불소치약"),
            x(BODY, SECOND, PARENT, "인플루엔자 첫 접종", "생후 6개월부터, 첫해는 4주 간격 2회"),
            x(LIFE, ALL_YEAR, PARENT, "카시트는 뒤보기로", "만 6세 미만 카시트 의무, 뒤보기는 가능한 오래"),
            x(MIND, ALL_YEAR, PARENT, "산후우울 점검", "2주 넘게 가라앉으면 정신건강복지센터 상담"),
        ),
        "a1" to listOf(
            x(BODY, ALL_YEAR, PARENT, "MMR 1차·수두", "12~15개월"),
            x(BODY, ALL_YEAR, PARENT, "A형간염 1·2차", "12~23개월, 6개월 간격"),
            x(BODY, ALL_YEAR, PARENT, "일본뇌염 기초접종", "12~23개월(불활성 2회 또는 생백신 1회)"),
            x(BODY, ALL_YEAR, PARENT, "Hib·폐렴구균 추가", "12~15개월"),
            x(BODY, ALL_YEAR, PARENT, "DTaP 4차", "15~18개월"),
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 4차", "18~24개월, 발달선별검사(K-DST)"),
            x(BODY, ALL_YEAR, PARENT, "구강검진 1차", "18~29개월"),
            x(BODY, ALL_YEAR, PARENT, "말 늦음 신호", "18개월 단어 10개 안팎, 24개월 두 단어 — 늦으면 검진 때 상담"),
            x(BODY, ALL_YEAR, PARENT, "우유는 하루 400~500ml", "많으면 철결핍 빈혈"),
            x(BODY, ALL_YEAR, PARENT, "잠 11~14시간", "낮잠 포함"),
            FLU,
            x(LIFE, ALL_YEAR, PARENT, "집 안 안전", "단추 전지·동전은 손 닿지 않게, 창문 잠금"),
            x(MIND, ALL_YEAR, PARENT, "떼쓰기는 발달 과정", "감정에 이름 붙여 주고 기다려 주기"),
        ),
        "a2" to listOf(
            x(BODY, ALL_YEAR, PARENT, "일본뇌염 추가접종", "24~35개월"),
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 5차", "30~36개월, 말이 늦으면 이때 상담"),
            DENTAL,
            x(BODY, ALL_YEAR, PARENT, "잠 11~14시간", "낮잠 1회"),
            x(BODY, ALL_YEAR, PARENT, "눈 이상 신호", "찡그리거나 한쪽 눈이 몰리면 안과"),
            FLU,
            x(LIFE, ALL_YEAR, TOGETHER, "간식은 정해진 시간에", "단 음료 대신 물"),
            x(MIND, ALL_YEAR, PARENT, "어린이집 적응 살피기", "잠·식사가 크게 흔들리면 선생님과 상담"),
        ),
        "a3" to listOf(
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 6차", "42~48개월, 시력 검사 꼭"),
            x(BODY, ALL_YEAR, PARENT, "구강검진 2차", "42~53개월"),
            x(BODY, ALL_YEAR, PARENT, "약시 찾기", "만 3~4세 시력 검사가 적기, 늦으면 치료가 어려워요"),
            DENTAL,
            x(BODY, ALL_YEAR, PARENT, "잠 10~13시간", "낮잠 포함"),
            FLU,
            x(LIFE, ALL_YEAR, TOGETHER, "손 씻기·기침 예절", "밖에서 돌아오면 30초"),
            x(MIND, ALL_YEAR, TOGETHER, "감정 말로 하기", "화남·속상함·무서움 그림 카드"),
        ),
        "a4" to listOf(
            x(BODY, ALL_YEAR, PARENT, "만 4~6세 추가접종 시작", "DTaP 5차 · IPV 4차 · MMR 2차"),
            x(BODY, ALL_YEAR, PARENT, "성장 기록", "6개월마다 키·몸무게, 성장곡선 3백분위 아래면 상담"),
            DENTAL,
            x(BODY, ALL_YEAR, PARENT, "잠 10~13시간", "같은 시각에 재우기"),
            FLU,
            x(LIFE, ALL_YEAR, TOGETHER, "헬멧·구명조끼", "자전거·킥보드·물놀이 때 꼭"),
            x(MIND, ALL_YEAR, TOGETHER, "친구와 다툼 다루기", "차례 지키기·미안해 말하기 연습"),
        ),
        "a5" to listOf(
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 7차", "54~60개월"),
            x(BODY, ALL_YEAR, PARENT, "구강검진 3차", "54~65개월"),
            x(BODY, ALL_YEAR, PARENT, "추가접종 이어서", "DTaP 5차 · IPV 4차 · MMR 2차를 입학 전까지"),
            DENTAL,
            FLU,
            x(MIND, ALL_YEAR, PARENT, "등원 거부 살피기", "2주 넘게 이어지면 선생님과 상담"),
        ),
        "a6" to listOf(
            x(BODY, ALL_YEAR, PARENT, "영유아 검진 8차", "66~71개월"),
            x(BODY, SECOND, PARENT, "입학 전 접종 확인", "DTaP 5차 · IPV 4차 · MMR 2차 · 일본뇌염 추가(6세), 예방접종도우미에서"),
            x(BODY, SECOND, PARENT, "시력·치과 치료 마치기", "입학 전에"),
            x(BODY, ALL_YEAR, PARENT, "첫 영구 어금니 홈메우기", "6세 구치가 나면, 건강보험 적용"),
            FLU,
            PUBERTY_EARLY,
            x(MIND, SECOND, TOGETHER, "입학 불안 덜기", "학교 가 보기·등굣길 걸어 보기"),
        ),
        "e1" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, PARENT, "학생정서·행동특성검사", "초1, 부모가 설문 작성"),
            x(MIND, FIRST, PARENT, "스마트폰 이용습관 진단", "초1, 부모가 설문"),
            FLU, DENTAL, MYOPIA, PUBERTY_EARLY,
            x(LIFE, FIRST, TOGETHER, "통학로 같이 걷기", "횡단보도·위험한 곳 표시"),
            x(MIND, ALL_YEAR, PARENT, "학교 스트레스 신호", "배 아픔·등교 거부가 2주 넘으면 담임과 상담"),
        ),
        "e2" to listOf(
            SCHOOL_SURVEY, FLU, DENTAL, MYOPIA, PUBERTY_EARLY,
            x(BODY, ALL_YEAR, PARENT, "두 번째 영구 어금니 전 점검", "홈메우기한 곳 떨어지지 않았는지"),
            x(MIND, ALL_YEAR, TOGETHER, "친구 관계 이야기", "저녁에 오늘 누구와 놀았는지"),
        ),
        "e3" to listOf(
            SCHOOL_SURVEY, FLU, DENTAL, MYOPIA, PUBERTY_EARLY,
            x(MIND, ALL_YEAR, PARENT, "학교폭력 신호 알기", "물건이 자주 없어지거나 등교를 피하면 담임·117"),
        ),
        "e4" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, PARENT, "학생정서·행동특성검사", "초4, 부모가 설문 작성"),
            x(MIND, FIRST, CHILD, "스마트폰 이용습관 진단", "초4, 학생이 직접"),
            x(BODY, SECOND, PARENT, "건강검진 결과 확인", "체질량지수(BMI)·시력·혈압"),
            x(BODY, ALL_YEAR, PARENT, "사춘기 시작 알기", "여아는 평균 10세 전후 가슴 발달, 초경은 평균 12세 전후"),
            FLU, DENTAL, MYOPIA, MOVE,
            x(MIND, ALL_YEAR, TOGETHER, "비교 대신 과정 칭찬", "형제·친구와 비교하지 않기"),
        ),
        "e5" to listOf(
            SCHOOL_SURVEY, FLU, DENTAL, MYOPIA, MOVE,
            x(BODY, ALL_YEAR, TOGETHER, "사춘기 준비", "생리대·속옷·면도기 미리, 몸의 변화 이야기"),
            x(BODY, ALL_YEAR, PARENT, "교정 상담", "영구치 교정은 대개 11~13세, 치과에서 시기 확인"),
            x(MIND, ALL_YEAR, TOGETHER, "감정 기복 이해하기", "호르몬 변화, 혼내기보다 들어 주기"),
        ),
        "e6" to listOf(
            SCHOOL_SURVEY,
            x(BODY, ALL_YEAR, PARENT, "만 11~12세 접종", "Tdap 6차 · 일본뇌염 추가(12세) · HPV 2회(12세 여아, 2026년부터 남아도)"),
            x(BODY, SECOND, PARENT, "척추측만 확인", "앞으로 숙였을 때 등 높이가 다르면 정형외과"),
            FLU, DENTAL, MYOPIA, MOVE,
            x(MIND, ALL_YEAR, TOGETHER, "SNS·단체방 갈등", "힘든 대화는 캡처해 어른에게"),
        ),
        "m1" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, CHILD, "학생정서·행동특성검사", "중1, 학생이 직접"),
            x(MIND, FIRST, CHILD, "스마트폰 이용습관 진단", "중1, 학생이 직접"),
            x(BODY, FIRST, PARENT, "입학 접종 확인", "Tdap · HPV · 일본뇌염, 빠진 것 보충"),
            x(BODY, ALL_YEAR, PARENT, "HPV 2차", "1차 6개월 뒤"),
            FLU, DENTAL, MOVE,
            x(BODY, ALL_YEAR, CHILD, "여드름·위생", "하루 두 번 세안, 짜지 않기"),
            DEPRESSION,
        ),
        "m2" to listOf(
            SCHOOL_SURVEY, DENTAL, MOVE,
            x(BODY, ALL_YEAR, CHILD, "에너지음료 피하기", "청소년 카페인은 체중 1kg당 2.5mg 안"),
            x(BODY, ALL_YEAR, CHILD, "바른 자세·목 건강", "책상 높이 맞추기, 50분마다 스트레칭"),
            x(MIND, ALL_YEAR, TOGETHER, "시험 스트레스 다루기", "시험 뒤 결과보다 과정 이야기"),
            DEPRESSION,
        ),
        "m3" to listOf(
            SCHOOL_SURVEY, DENTAL, MOVE,
            x(MIND, ALL_YEAR, TOGETHER, "진로 불안 나누기", "정하지 못해도 괜찮다고 말해 주기"),
            x(MIND, ALL_YEAR, CHILD, "스마트폰 사용 점검", "하루 사용 시간 스스로 확인"),
            DEPRESSION,
        ),
        "h1" to listOf(
            SCHOOL_CHECK,
            x(MIND, FIRST, CHILD, "학생정서·행동특성검사", "고1, 학생이 직접"),
            x(MIND, FIRST, CHILD, "스마트폰 이용습관 진단", "고1, 학생이 직접"),
            DENTAL, MOVE,
            x(MIND, ALL_YEAR, PARENT, "위기 신호 알기", "죽고 싶다는 말은 그냥 넘기지 않기 — 109(자살예방)·1388"),
        ),
        "h2" to listOf(
            SCHOOL_SURVEY, DENTAL, MOVE,
            x(BODY, ALL_YEAR, CHILD, "에너지음료 피하기", "청소년 카페인은 체중 1kg당 2.5mg 안"),
            x(MIND, ALL_YEAR, TOGETHER, "번아웃 살피기", "주 1회는 공부 없는 반나절"),
            DEPRESSION,
        ),
        "h3" to listOf(
            SCHOOL_SURVEY,
            x(BODY, SECOND, PARENT, "독감 접종", "수능 전 10월 초(만 13세 넘어 유료)"),
            x(BODY, SECOND, CHILD, "수능 생활 리듬", "한 달 전부터 6시 반 기상, 수능 시간표대로"),
            x(MIND, SECOND, CHILD, "시험 불안 호흡법", "4초 들이쉬고 6초 내쉬기, 시험 전 연습"),
            x(MIND, SECOND, TOGETHER, "수능 뒤 생활 되찾기", "잠·운동·만남부터"),
        ),
        "u" to listOf(
            x(BODY, FIRST, CHILD, "A형간염 항체 확인", "없으면 접종(20~30대 권장)"),
            x(MIND, ALL_YEAR, CHILD, "학교 상담센터", "무료 상담 한 번 받아 보기"),
        ),
        "g" to listOf(
            x(MIND, ALL_YEAR, CHILD, "연구실 번아웃 점검", "주 1일은 쉬기, 학교 상담센터"),
        ),
    )

    private fun x(area: YearArea, term: YearTerm, who: YearDoer, title: String, how: String) = YearTask(area, term, title, how, who)
}
