package com.nextstep.app.domain.year

import com.nextstep.app.domain.year.HealthRows.DENTAL
import com.nextstep.app.domain.year.HealthRows.FLU
import com.nextstep.app.domain.year.HealthRows.PUBERTY_EARLY
import com.nextstep.app.domain.year.HealthRows.x
import com.nextstep.app.domain.year.YearArea.BODY
import com.nextstep.app.domain.year.YearArea.LIFE
import com.nextstep.app.domain.year.YearArea.MIND
import com.nextstep.app.domain.year.YearDoer.PARENT
import com.nextstep.app.domain.year.YearDoer.TOGETHER
import com.nextstep.app.domain.year.YearTerm.ALL_YEAR
import com.nextstep.app.domain.year.YearTerm.SECOND

/** 학령 전(a0~a6) 건강: 영유아 검진·구강검진·접종 월령. 출처와 모으는 곳은 [HealthPlans]. */
internal object PreschoolHealth {
    val byYear: Map<String, List<YearTask>> = mapOf(
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
    )
}
