package com.nextstep.app.domain.journey

import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.DueRule.AgeMonths
import com.nextstep.app.domain.journey.DueRule.BirthYearOffset
import com.nextstep.app.domain.journey.DueRule.SchoolMonth
import com.nextstep.app.domain.journey.MilestoneCategory.ADMIN
import com.nextstep.app.domain.journey.MilestoneCategory.CAREER
import com.nextstep.app.domain.journey.MilestoneCategory.FINANCE
import com.nextstep.app.domain.journey.MilestoneCategory.HEALTH
import com.nextstep.app.domain.journey.MilestoneCategory.LANGUAGE
import com.nextstep.app.domain.journey.MilestoneCategory.LEARNING
import com.nextstep.app.domain.journey.MilestoneCategory.SOCIAL

/**
 * 신생아부터 대학원까지의 기본 여정 카탈로그. 한국 기준 행정·건강·학습·언어·진로 이정표를 시기별로 담습니다.
 * 날짜와 제도는 바뀔 수 있으므로 각 항목의 why 에 근거를 적고, 사용자가 건너뛰거나 날짜를 바꿀 수 있게 합니다.
 * id 는 저장된 상태와 연결되므로 절대 바꾸지 않습니다.
 */
object MilestoneCatalog {
    val templates: List<MilestoneTemplate> = listOf(
        // ---------------------------------------------------------------- 영아 (0~11개월)
        t("birth-registration", "출생신고", "주민센터 또는 온라인(정부24)으로 출생신고. 이후 양육수당·아동수당 신청까지 이어집니다.", "출생 후 1개월 내 신고 의무. 늦으면 과태료가 있고 수당 지급도 늦어집니다.", ADMIN, GrowthStage.NEWBORN, AgeMonths(1), leadMonths = 1, priority = 1),
        t("child-benefit", "아동수당·부모급여 신청", "출생신고와 함께 아동수당, 부모급여(영아수당), 첫만남이용권을 신청합니다.", "신청일 기준으로 지급되어 늦을수록 못 받는 달이 생깁니다.", FINANCE, GrowthStage.NEWBORN, AgeMonths(1), leadMonths = 1, priority = 1),
        t("daycare-waitlist", "어린이집 입소 대기 신청", "임신육아종합포털(아이사랑)에서 희망 어린이집에 입소 대기를 걸어 둡니다. 여러 곳 동시 가능.", "인기 어린이집은 대기가 1년 이상입니다. 태어나자마자 신청해야 원하는 시기에 자리를 잡습니다.", ADMIN, GrowthStage.NEWBORN, AgeMonths(2), leadMonths = 2, priority = 1),
        t("vaccine-2m", "2개월 예방접종 시작", "BCG(4주 이내), B형간염, DTaP·소아마비·Hib·폐렴구균·로타 등 2·4·6개월 접종 일정을 시작합니다.", "국가예방접종 일정은 시기가 정해져 있고 밀리면 이후 접종이 함께 밀립니다.", HEALTH, GrowthStage.NEWBORN, AgeMonths(2), leadMonths = 1, priority = 1),
        t("infant-checkup-1", "영유아 건강검진 1차 (4~6개월)", "국민건강보험 영유아 건강검진 1차. 성장·발달·청각 등을 확인합니다.", "생후 4~6개월이 검진 기간이며 이후 8회까지 시기별로 이어집니다. 기간을 넘기면 무료 검진을 못 받습니다.", HEALTH, GrowthStage.NEWBORN, AgeMonths(5), leadMonths = 1, priority = 1),
        t("talk-daily", "매일 말 걸기·그림책 읽어 주기 습관", "하루 여러 번 아이 눈을 맞추고 말을 걸고, 6개월부터는 그림책을 보여 줍니다.", "생후 1년의 언어 노출량이 이후 어휘력과 읽기 능력의 토대라는 연구가 많습니다. 학습이 아니라 대화입니다.", LANGUAGE, GrowthStage.NEWBORN, AgeMonths(6), leadMonths = 6, priority = 2),
        t("infant-checkup-2", "영유아 건강검진 2차 (9~12개월)", "2차 검진과 구강검진 시작 시기를 확인합니다.", "발달 지연은 일찍 발견할수록 개입 효과가 큽니다.", HEALTH, GrowthStage.NEWBORN, AgeMonths(10), leadMonths = 1, priority = 2),
        t("mmr-12m", "12개월 예방접종 (MMR·수두·일본뇌염 등)", "돌 전후 접종 묶음. 어린이집 입소 시 접종 증명이 필요합니다.", "돌 접종은 어린이집 입소 요건이기도 합니다.", HEALTH, GrowthStage.NEWBORN, AgeMonths(12), leadMonths = 1, priority = 1),

        // ---------------------------------------------------------------- 유아 (1~2세)
        t("daycare-enroll", "어린이집 입소 결정", "대기 순번이 오면 입소 여부를 결정합니다. 맞벌이 여부, 적응 기간을 고려하세요.", "입소 제안은 며칠 안에 답해야 하고, 거절하면 대기가 뒤로 밀리는 곳이 많습니다.", ADMIN, GrowthStage.TODDLER, AgeMonths(15), leadMonths = 3, priority = 1),
        t("language-checkup", "언어·발달 점검 (18~24개월)", "영유아검진 3~4차에서 언어 발달 문항을 꼼꼼히. 두 단어 조합이 늦으면 상담을 받습니다.", "만 2세 전후는 언어 폭발기라 지연을 가장 잘 알아챌 수 있는 시기입니다.", HEALTH, GrowthStage.TODDLER, AgeMonths(20), leadMonths = 2, priority = 1),
        t("picture-books", "그림책 하루 3권 루틴", "같은 책 반복도 좋습니다. 아이가 고르게 하세요.", "이 시기 어휘 노출량이 초등 읽기 이해력과 직결됩니다.", LANGUAGE, GrowthStage.TODDLER, AgeMonths(24), leadMonths = 10, priority = 2),
        t("toddler-play", "몸 놀이·또래 놀이 시간 확보", "놀이터·문화센터·놀이 교실 등 매주 정기적인 신체·또래 활동을 둡니다.", "대근육 발달과 사회성의 기초가 이 시기에 만들어집니다. 화면 시간은 최소로.", SOCIAL, GrowthStage.TODDLER, AgeMonths(30), leadMonths = 12, priority = 3),
        t("screen-rule", "화면 시간 규칙 정하기", "만 2세 전에는 영상 노출을 거의 하지 않고, 이후에도 하루 한도를 정합니다.", "소아과 지침은 만 2세 미만 영상 노출을 권하지 않습니다. 습관은 처음 정할 때가 가장 쉽습니다.", HEALTH, GrowthStage.TODDLER, AgeMonths(24), leadMonths = 6, priority = 2),

        // ---------------------------------------------------------------- 유치원기 (3~6세)
        t("english-exposure", "영어 소리 노출 시작 (노래·영상·놀이)", "하루 10~20분 영어 노래, 짧은 영상, 영어 그림책. 학습지가 아니라 소리에 익숙해지는 것이 목표.", "만 3~7세는 소리를 구별·모방하는 능력이 가장 좋은 언어 민감기입니다. 이 시기의 편안한 노출이 이후 발음·듣기의 차이를 만듭니다.", LANGUAGE, GrowthStage.PRESCHOOL, AgeMonths(42), leadMonths = 6, priority = 1),
        t("kindergarten-apply", "유치원 지원 (처음학교로, 11월)", "만 3세가 되는 해 11월 '처음학교로'에서 국공립·사립 유치원에 지원합니다. 어린이집 계속 다닐지 함께 결정.", "유치원 접수는 매년 11월 한 번이며 추첨입니다. 놓치면 결원만 노려야 합니다.", ADMIN, GrowthStage.PRESCHOOL, BirthYearOffset(3, 11), leadMonths = 2, priority = 1),
        t("preschool-checkup", "영유아 건강검진 마지막 회차 (54~66개월)", "취학 전 마지막 검진. 시력·청력·치과를 함께 확인합니다.", "초등 입학 전에 시력 교정, 치아 치료를 끝내야 적응이 쉽습니다.", HEALTH, GrowthStage.PRESCHOOL, AgeMonths(60), leadMonths = 3, priority = 2),
        t("hangul-play", "한글 놀이 시작 (아이가 글자에 관심 보일 때)", "간판·이름·좋아하는 단어부터. 쓰기보다 읽기 먼저.", "대부분 만 5~6세에 자연스럽게 관심이 생깁니다. 억지로 앞당기면 흥미를 잃습니다.", LEARNING, GrowthStage.PRESCHOOL, AgeMonths(66), leadMonths = 12, priority = 2),
        t("school-entry-notice", "취학통지서 확인·예비소집 (1월)", "12월 취학통지서를 받고 1월 초등학교 예비소집에 참석합니다. 입학 유예·조기 입학은 이때 신청.", "예비소집 불참 시 학교가 확인 절차를 밟습니다. 돌봄교실·방과후 신청도 이 무렵 시작합니다.", ADMIN, GrowthStage.PRESCHOOL, BirthYearOffset(7, 1, 10), leadMonths = 2, priority = 1),
        t("after-school-care", "초등 돌봄교실·늘봄 신청", "맞벌이라면 입학 전 돌봄교실(늘봄학교) 신청 시기를 확인합니다.", "정원이 있어 신청 시기를 놓치면 대기입니다.", ADMIN, GrowthStage.PRESCHOOL, BirthYearOffset(7, 2), leadMonths = 2, priority = 2),
        t("school-readiness", "초등 입학 준비 (생활 습관)", "일찍 자고 일어나기, 혼자 화장실·옷 입기, 20분 앉아 있기, 자기 물건 챙기기.", "학습보다 생활 자립이 1학년 적응을 좌우합니다.", SOCIAL, GrowthStage.PRESCHOOL, BirthYearOffset(7, 3), leadMonths = 4, priority = 2),

        // ---------------------------------------------------------------- 초등 저학년
        t("reading-habit", "매일 읽기 습관 (초1~2)", "하루 20분, 아이가 고른 책. 읽어 주기와 혼자 읽기를 섞습니다.", "초등 저학년의 읽기 유창성이 이후 모든 과목 이해의 기반입니다.", LEARNING, GrowthStage.EARLY_ELEMENTARY, SchoolMonth(1, 9), leadMonths = 6, priority = 1),
        t("english-listening", "영어 듣기·읽기 노출 유지", "민감기 마지막 구간. 챕터북 음원, 애니메이션, 영어 그림책을 매일 조금씩.", "만 7세 무렵까지의 노출이 듣기·발음에 남습니다. 이후에는 '학습'의 비중이 커집니다.", LANGUAGE, GrowthStage.EARLY_ELEMENTARY, SchoolMonth(2, 3), leadMonths = 12, priority = 2),
        t("math-basics", "수 감각·연산 기초 다지기", "덧셈·뺄셈, 구구단(초2), 시계·달력 읽기를 생활에서 반복.", "초3부터 나눗셈·분수가 시작되어 기초 연산이 흔들리면 수학이 급격히 어려워집니다.", LEARNING, GrowthStage.EARLY_ELEMENTARY, SchoolMonth(2, 12), leadMonths = 12, priority = 2),
        t("hobby-one", "예체능 하나 꾸준히 (수영·악기·운동)", "만들기·악기·운동 중 하나를 1년 이상 지속해 봅니다.", "성취와 꾸준함의 경험이 학습 태도로 이어집니다. 생존 수영은 초등 필수 과정이기도 합니다.", SOCIAL, GrowthStage.EARLY_ELEMENTARY, SchoolMonth(3, 3), leadMonths = 18, priority = 3),
        t("vision-check-8", "시력·치과 정기 검진", "초등 저학년은 근시가 시작되는 시기. 학교 검진 외에 1년에 한 번 확인.", "근시는 조기 관리로 진행을 늦출 수 있습니다.", HEALTH, GrowthStage.EARLY_ELEMENTARY, SchoolMonth(2, 5), leadMonths = 1, priority = 3),

        // ---------------------------------------------------------------- 초등 고학년
        t("self-planning", "주간 계획 스스로 세우기", "매주 일요일 아이가 계획표를 쓰고 부모는 확인만 합니다.", "고학년의 자기 관리 습관이 중학교 학습량 증가를 버티게 합니다.", LEARNING, GrowthStage.UPPER_ELEMENTARY, SchoolMonth(4, 4), leadMonths = 3, priority = 2),
        t("english-grammar-start", "영어 읽기·문법 학습으로 전환", "듣기 노출에서 읽기·기초 문법·어휘 학습으로 비중을 옮깁니다.", "중학교 영어는 문법·독해 중심이라 초5~6에 읽기 체력을 만들어 두면 부담이 줄어듭니다.", LANGUAGE, GrowthStage.UPPER_ELEMENTARY, SchoolMonth(5, 3), leadMonths = 6, priority = 2),
        t("math-fractions", "분수·소수·비율 완전 이해", "초5~6 분수·소수·비와 비율은 중등 수학의 문턱. 개념을 말로 설명할 수 있게.", "중1 수학의 절반이 이 개념 위에 세워집니다.", LEARNING, GrowthStage.UPPER_ELEMENTARY, SchoolMonth(6, 10), leadMonths = 12, priority = 1),
        t("middle-school-assign", "중학교 배정 (희망 학교 조사, 11~12월)", "초6 2학기 중학교 배정 원서·희망 조사에 응답합니다. 학군·통학을 미리 확인.", "배정 후에는 전학 외에 바꾸기 어렵습니다.", ADMIN, GrowthStage.UPPER_ELEMENTARY, SchoolMonth(6, 11), leadMonths = 2, priority = 1),
        t("long-project", "긴 프로젝트 하나 완성 (독서·탐구·발표)", "한 주제로 몇 주간 조사하고 결과물을 만들어 발표합니다.", "긴 호흡의 몰입 경험이 중·고등 수행평가와 탐구 활동의 예행연습입니다.", SOCIAL, GrowthStage.UPPER_ELEMENTARY, SchoolMonth(5, 12), leadMonths = 6, priority = 3),

        // ---------------------------------------------------------------- 중등
        t("free-semester", "자유학기 활동 계획 (중1)", "자유학기(학년)의 진로·예술·동아리 활동을 관심사에 맞춰 고릅니다.", "성적 부담이 없는 유일한 학기라 진로 탐색에 쓰기 가장 좋습니다.", CAREER, GrowthStage.MIDDLE, SchoolMonth(7, 3), leadMonths = 1, priority = 3),
        t("concept-notebook", "오답·개념 노트 습관", "틀린 이유를 스스로 적는 노트를 과목별로 시작합니다.", "중등은 개념의 뼈대가 세워지는 시기. 오답 정리가 성적 차이를 만듭니다.", LEARNING, GrowthStage.MIDDLE, SchoolMonth(7, 5), leadMonths = 2, priority = 2),
        t("high-school-search", "고등학교 유형 탐색 (일반·특목·자사·특성화)", "중2 여름부터 학교 유형별 입시·교육과정·통학을 비교합니다.", "특목·자사고는 중3 8~12월에 원서를 내므로 중2에 방향을 정해야 합니다.", CAREER, GrowthStage.MIDDLE, SchoolMonth(8, 7), leadMonths = 3, priority = 2),
        t("high-school-apply", "고입 원서 (특목·자사고 8~12월, 일반고 12월)", "지원 학교의 전형 일정에 맞춰 원서·자기소개서·면접을 준비합니다.", "학교 유형마다 접수 기간이 다르고 한 번뿐입니다.", ADMIN, GrowthStage.MIDDLE, SchoolMonth(9, 10), leadMonths = 4, priority = 1),
        t("english-reading-level", "영어 원서·긴 지문 읽기 체력", "학년별 권장 어휘 수준의 원서나 긴 지문을 주 3회 읽습니다.", "고등 영어 지문 길이와 어휘는 중등과 격차가 커서 미리 읽기 체력이 필요합니다.", LANGUAGE, GrowthStage.MIDDLE, SchoolMonth(9, 3), leadMonths = 12, priority = 2),
        t("sleep-exercise", "수면·운동 시간 지키기", "하루 7시간 이상 수면, 주 3회 운동을 일정에 고정합니다.", "사춘기의 수면 부족은 집중력과 정서에 직접 영향을 줍니다.", HEALTH, GrowthStage.MIDDLE, SchoolMonth(8, 3), leadMonths = 12, priority = 3),

        // ---------------------------------------------------------------- 고등
        t("subject-selection", "선택과목 결정 (고1 2학기)", "고2~3 선택과목을 진로·대입 전형과 맞춰 결정합니다.", "선택과목은 대학 학과별 권장 과목과 연결되어 되돌리기 어렵습니다.", CAREER, GrowthStage.HIGH, SchoolMonth(10, 9), leadMonths = 3, priority = 1),
        t("study-strategy", "과목별 목표 등급·시간 배분 설계", "성적 추이와 시간 분포 데이터를 보고 과목별 전략을 세웁니다.", "고등은 전략의 시기. 모든 과목을 똑같이 하면 어느 것도 오르지 않습니다.", LEARNING, GrowthStage.HIGH, SchoolMonth(10, 4), leadMonths = 1, priority = 2),
        t("mock-exam-plan", "모의고사 일정·분석 루틴", "3·6·9월 모의고사 후 오답 분석과 취약 단원 계획을 반복합니다.", "모의고사 분석이 수능 전략의 근거입니다.", LEARNING, GrowthStage.HIGH, SchoolMonth(11, 3), leadMonths = 1, priority = 2),
        t("admission-type", "수시·정시 방향 결정 (고3 6월 전)", "내신·모평·비교과를 보고 수시(학생부·논술)와 정시 비중을 정합니다.", "6월 모평 이후 원서 전략이 갈리며 준비 방식이 완전히 다릅니다.", CAREER, GrowthStage.HIGH, SchoolMonth(12, 6), leadMonths = 3, priority = 1),
        t("csat-register", "수능 원서 접수 (8월 말~9월 초)", "재학 중인 학교에서 수능 응시 원서를 접수합니다.", "접수 기간이 짧고 놓치면 그해 수능을 볼 수 없습니다.", ADMIN, GrowthStage.HIGH, SchoolMonth(12, 8, 25), leadMonths = 1, priority = 1),
        t("early-admission", "수시 원서 접수 (9월)", "최대 6개 대학에 수시 원서를 냅니다. 자기소개서·면접·논술 일정 확인.", "9월 초 며칠간만 접수합니다.", ADMIN, GrowthStage.HIGH, SchoolMonth(12, 9, 5), leadMonths = 2, priority = 1),
        t("regular-admission", "정시 원서 접수 (12월 말~1월 초)", "수능 성적으로 가·나·다군 지원. 배치표와 대학별 환산 점수를 비교합니다.", "군별 1곳씩만 지원 가능해 전략이 중요합니다.", ADMIN, GrowthStage.HIGH, SchoolMonth(12, 12, 28), leadMonths = 1, priority = 1),
        t("scholarship-search", "장학금·국가장학금 알아보기", "국가장학금(한국장학재단) 1차 신청은 11~12월. 대학별 장학금도 확인.", "입학 전 신청해야 1학기부터 받습니다.", FINANCE, GrowthStage.HIGH, SchoolMonth(12, 11, 20), leadMonths = 1, priority = 2),

        // ---------------------------------------------------------------- 대학
        t("major-explore", "전공 외 과목·동아리로 탐색 (1학년)", "관심 분야 교양·타과 수업, 동아리 하나를 해 봅니다.", "1~2학년의 탐색이 전과·복수전공·진로 결정의 근거가 됩니다.", CAREER, GrowthStage.UNIVERSITY, SchoolMonth(13, 9), leadMonths = 3, priority = 3),
        t("exchange-program", "교환학생·어학연수 지원 (2학년)", "학교 국제교류처 모집 일정(보통 학기 초)을 확인해 지원합니다.", "3학년 이후에는 졸업 요건·취업 준비와 겹쳐 가기 어렵습니다.", SOCIAL, GrowthStage.UNIVERSITY, SchoolMonth(14, 3), leadMonths = 4, priority = 2),
        t("internship", "인턴·현장실습 (3학년 여름)", "방학 인턴십, 학교 현장실습, 공모전·프로젝트 중 하나를 완료합니다.", "졸업 전 실무 경험 한 번이 취업과 진학 모두에 결정적입니다.", CAREER, GrowthStage.UNIVERSITY, SchoolMonth(15, 6), leadMonths = 4, priority = 1),
        t("grad-or-job", "취업·대학원 진로 결정 (3학년 말)", "대학원이면 연구실 컨택·추천서, 취업이면 직무·기업 목표를 정합니다.", "4학년은 실행의 해라 방향은 3학년에 정해야 합니다.", CAREER, GrowthStage.UNIVERSITY, SchoolMonth(15, 12), leadMonths = 3, priority = 1),
        t("national-scholarship", "국가장학금 매 학기 신청", "매 학기 전 한국장학재단 신청 기간(5~6월, 11~12월)을 확인합니다.", "신청 기간을 놓치면 그 학기는 받을 수 없습니다.", FINANCE, GrowthStage.UNIVERSITY, SchoolMonth(13, 11, 20), leadMonths = 1, priority = 2),

        // ---------------------------------------------------------------- 대학원
        t("thesis-plan", "논문 주제·지도교수 확정", "입학 첫 학기 안에 주제 방향과 지도교수를 정하고 연구 계획을 씁니다.", "주제가 늦게 정해지면 학위 기간이 그만큼 늘어납니다.", LEARNING, GrowthStage.GRADUATE, SchoolMonth(17, 8), leadMonths = 3, priority = 1),
        t("conference-submit", "학회 발표·논문 투고 첫 경험", "국내 학회 포스터나 워크숍부터 시작합니다.", "발표 경험은 졸업 요건과 이후 진로(연구·산업) 모두에 필요합니다.", CAREER, GrowthStage.GRADUATE, SchoolMonth(18, 5), leadMonths = 4, priority = 2),
        t("grad-funding", "연구비·조교·장학 재원 확보", "BK21, 연구실 인건비, 조교, 외부 장학금 등 재원을 학기마다 점검합니다.", "재정 불안이 학위 중단의 흔한 이유입니다.", FINANCE, GrowthStage.GRADUATE, SchoolMonth(17, 3), leadMonths = 2, priority = 2),
        t("grad-health", "정신 건강·생활 리듬 점검", "주간 운동, 상담 자원(학교 상담센터) 확인, 수면 루틴을 정합니다.", "장기 프로젝트의 번아웃은 예방이 유일한 대책입니다.", HEALTH, GrowthStage.GRADUATE, SchoolMonth(17, 9), leadMonths = 6, priority = 3),
    )

    val byId: Map<String, MilestoneTemplate> = templates.associateBy { it.id }

    fun forStage(stage: GrowthStage): List<MilestoneTemplate> = templates.filter { it.stage == stage }

    private fun t(
        id: String, title: String, description: String, why: String, category: MilestoneCategory, stage: GrowthStage,
        due: DueRule, leadMonths: Int, priority: Int = 2,
    ) = MilestoneTemplate(id, title, description, why, category, stage, due, leadMonths, priority)
}
