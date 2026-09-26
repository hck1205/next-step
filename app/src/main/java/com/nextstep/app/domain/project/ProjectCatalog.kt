package com.nextstep.app.domain.project

import com.nextstep.app.domain.project.RoutineKind.LISTEN
import com.nextstep.app.domain.project.RoutineKind.PLAY
import com.nextstep.app.domain.project.RoutineKind.PRACTICE
import com.nextstep.app.domain.project.RoutineKind.READ
import com.nextstep.app.domain.project.RoutineKind.REVIEW
import com.nextstep.app.domain.project.RoutineKind.SPEAK
import com.nextstep.app.domain.project.RoutineKind.WRITE

/**
 * 기본 교육 프로젝트. 한 프로젝트는 "언제까지 무엇을 할 수 있게"를 단계로 나누고, 단계마다
 * 하루 루틴(무엇을 · 몇 분 · 주 며칠) · 통과 기준 · 추천 교재를 적습니다. 루틴의 양은 단계마다 조금씩 늘어나고
 * 학령 전에는 성장 단계별 권장 시간(YearProfiles.dailyMinutes)을 넘지 않게 잡았습니다.
 * 개월 수는 만 나이 기준이며, 초등 학년의 시작은 초1 = 78개월로 어림합니다(3월 입학).
 * id·key 는 저장된 프로젝트와 연결되므로 바꾸지 않습니다.
 */
object ProjectCatalog {
    val plans: List<ProjectPlan> = listOf(
        ProjectPlan(
            id = "english-reader", category = ProjectCategory.ENGLISH,
            title = "영어 챕터북 혼자 읽기",
            goal = "초6 겨울까지 영어 챕터북을 혼자 읽고, 좋아하는 주제로 3분 동안 영어로 말하기",
            span = "만 3세 → 초6",
            why = "외국어 실력은 들은 시간과 읽은 양의 합입니다. 만 3~6세는 소리에 익숙해지는 때라 뜻 설명 없이 노래·그림책·영상으로 듣기만 쌓고, " +
                "글자는 소리를 충분히 들은 뒤에 파닉스로 붙입니다. 초등부터는 읽기가 중심이고, 쓰기·문법은 읽은 문장에서 규칙을 찾는 방식으로 늦게 붙입니다. " +
                "하루 평균 7분에서 33분으로 천천히 늘려 가면 초6까지 1,000시간 넘게 쌓입니다.",
            phases = listOf(
                ProjectPhase(
                    "p1", "소리와 친해지기", "만 3세", 36, 52,
                    listOf(r("영어 노래·율동 따라 하기", PLAY, 10, 5)),
                    checkpoint = "좋아하는 영어 노래 5곡을 따라 부른다",
                    materials = "마더구스·동요 영상, 율동 노래", tip = "뜻을 설명하거나 단어를 묻지 않아요. 놀이로만.",
                ),
                ProjectPhase(
                    "p2", "그림책 소리 듣기", "만 4세", 48, 52,
                    listOf(r("영어 그림책 음원 들으며 넘기기", LISTEN, 10, 5), r("영어 노래", PLAY, 5, 3)),
                    checkpoint = "영어 그림책 10권을 음원과 함께 끝까지 듣고, 좋아하는 책을 스스로 고른다",
                    materials = "음원 있는 영어 그림책(한 쪽에 한두 문장)", tip = "같은 책을 여러 번 반복하는 것이 새 책보다 좋아요.",
                ),
                ProjectPhase(
                    "p3", "좋아하는 영상 반복 듣기", "만 5세", 60, 52,
                    listOf(r("같은 영어 애니메이션 한 편", LISTEN, 15, 5), r("그림책 음원", LISTEN, 10, 3)),
                    checkpoint = "주인공의 대사 한두 마디를 상황에 맞게 따라 한다",
                    materials = "한 편 10~15분짜리 영어 시리즈 하나", tip = "자막 없이, 한 시리즈를 오래. 화면은 하루 1시간 안.",
                ),
                ProjectPhase(
                    "p4", "소리와 글자 잇기", "만 6세", 72, 26,
                    listOf(r("알파벳 소리(음가) 놀이", PLAY, 10, 5), r("그림책 따라 말하기", SPEAK, 10, 5), r("좋아하는 영상", LISTEN, 15, 2)),
                    checkpoint = "알파벳 26개의 소리를 말하고, 자기 이름을 영어로 쓴다",
                    materials = "자석 글자·알파벳 카드", tip = "글자 이름(에이·비)보다 소리(애·브)부터.",
                ),
                ProjectPhase(
                    "p5", "파닉스", "초1", 78, 52,
                    listOf(r("파닉스 교재 한 쪽", PRACTICE, 10, 5), r("리더스 1단계 음원 따라 읽기", READ, 10, 5), r("흘려듣기", LISTEN, 15, 3)),
                    checkpoint = "cat·dog 같은 세 글자 단어 20개를 소리 내어 읽는다",
                    materials = "파닉스 교재 1권, 리더스 1단계 20권", tip = "학교 적응이 먼저라 하루 20분 안에서만.",
                ),
                ProjectPhase(
                    "p6", "리더스 따라 읽기", "초2", 90, 52,
                    listOf(r("리더스 2~3단계 따라 읽기", READ, 15, 5), r("사이트워드", PRACTICE, 5, 5), r("영어 영상", LISTEN, 15, 4)),
                    checkpoint = "리더스 3단계 한 권을 음원 없이 읽고, 자주 나오는 단어 100개를 바로 읽는다",
                    materials = "리더스 2~3단계 30권", tip = "음원 듣기 → 따라 읽기 → 혼자 읽기 순서로 같은 책을 세 번.",
                ),
                ProjectPhase(
                    "p7", "초기 챕터북", "초3", 102, 52,
                    listOf(r("챕터북 음원 들으며 읽기", READ, 20, 5), r("영어 한 문장 말하기", SPEAK, 5, 5), r("흘려듣기", LISTEN, 15, 4)),
                    checkpoint = "그림 적은 초기 챕터북(60~80쪽) 한 권을 혼자 끝까지 읽는다",
                    materials = "초기 챕터북 시리즈 하나", tip = "학교 영어가 시작돼요. 모르는 단어는 넘어가며 읽기.",
                ),
                ProjectPhase(
                    "p8", "시리즈 완독", "초4", 114, 52,
                    listOf(
                        r("챕터북 혼자 읽기", READ, 25, 5), r("모르는 단어 카드", REVIEW, 5, 5),
                        r("책 이야기 영어로 말하기", SPEAK, 5, 3), r("흘려듣기", LISTEN, 15, 2),
                    ),
                    checkpoint = "같은 시리즈 10권을 다 읽고, 줄거리를 영어 세 문장으로 말한다",
                    materials = "주인공이 같은 챕터북 시리즈 10권", tip = "재미있는 시리즈 하나가 가장 좋은 교재예요.",
                ),
                ProjectPhase(
                    "p9", "어휘와 짧은 글쓰기", "초5", 126, 52,
                    listOf(
                        r("챕터북·쉬운 논픽션 읽기", READ, 25, 5), r("어휘 정리", REVIEW, 10, 5),
                        r("영어 다섯 문장 쓰기", WRITE, 15, 2), r("말하기 녹음", SPEAK, 5, 2),
                    ),
                    checkpoint = "주 1회 다섯 문장 글을 석 달 동안 이어 쓰고, 읽은 책의 단어 800개를 안다",
                    materials = "논픽션 리더스, 단어장 공책", tip = "틀린 문법을 고치기보다 쓴 내용을 칭찬해요.",
                ),
                ProjectPhase(
                    "p10", "문법 정리와 3분 말하기", "초6", 138, 52,
                    listOf(
                        r("챕터북 읽기", READ, 30, 5), r("읽은 문장에서 문법 규칙 찾기", PRACTICE, 15, 3),
                        r("3분 말하기 준비·녹음", SPEAK, 10, 2), r("영어 글쓰기", WRITE, 15, 1),
                    ),
                    checkpoint = "챕터북 한 권을 혼자 읽고, 좋아하는 주제로 3분 동안 영어로 말한 녹음을 남긴다",
                    materials = "얇은 기초 문법책 1권, 녹음 앱", tip = "중1 교과서 문법 용어에 미리 익숙해지는 정도면 충분해요.",
                ),
            ),
        ),
        ProjectPlan(
            id = "korean-reader", category = ProjectCategory.READING,
            title = "혼자 읽고 요약하기",
            goal = "초4까지 글밥 있는 책을 혼자 읽고, 한 장을 한 문단(다섯 문장)으로 요약하기",
            span = "만 2세 → 초4",
            why = "문해력은 모든 과목의 바탕입니다. 어릴 때 어른이 읽어 주며 나눈 대화의 양이 어휘가 되고, " +
                "초1~2의 소리 내어 읽기가 유창성이 되며, 초3~4에 넓게 읽고 요약하는 힘이 교과 읽기로 이어집니다.",
            phases = listOf(
                ProjectPhase(
                    "p1", "매일 읽어 주기", "만 2세", 24, 52,
                    listOf(r("그림책 읽어 주기", READ, 10, 7)),
                    checkpoint = "좋아하는 책이 생기고, 읽어 달라며 스스로 책을 가져온다",
                    materials = "보드북·그림책, 손 닿는 낮은 책장", tip = "같은 책 반복은 좋은 신호예요.",
                ),
                ProjectPhase(
                    "p2", "질문하며 읽기", "만 3~4세", 36, 104,
                    listOf(r("그림책 읽어 주기", READ, 15, 5), r("\"왜 그랬을까?\" 이야기 나누기", SPEAK, 5, 3)),
                    checkpoint = "읽은 이야기를 처음과 끝으로 다시 말한다",
                    materials = "이야기가 있는 그림책", tip = "정답을 묻기보다 아이 생각을 물어요.",
                ),
                ProjectPhase(
                    "p3", "글자와 친해지기", "만 5~6세", 60, 78,
                    listOf(r("그림책 읽어 주기", READ, 15, 5), r("한 줄씩 번갈아 읽기", READ, 5, 5)),
                    checkpoint = "받침 없는 글자로 된 그림책 한 권을 소리 내어 읽는다",
                    materials = "글자 적은 그림책", tip = "한글은 초1 국어에서 처음부터 배워요. 흥미만 붙이기.",
                ),
                ProjectPhase(
                    "p4", "소리 내어 읽기", "초1", 78, 52,
                    listOf(r("소리 내어 읽기", READ, 10, 5), r("어른이 읽어 주기", READ, 15, 5)),
                    checkpoint = "교과서 한 쪽을 막히지 않고 소리 내어 읽는다",
                    materials = "교과서, 읽기 쉬운 동화", tip = "읽어 주기를 그만두지 않아요. 듣는 이해가 읽는 이해보다 앞서요.",
                ),
                ProjectPhase(
                    "p5", "혼자 읽기", "초2", 90, 52,
                    listOf(r("혼자 읽기", READ, 20, 5), r("한 줄 느낌 쓰기", WRITE, 5, 3), r("어른이 읽어 주기", READ, 15, 2)),
                    checkpoint = "한 쪽에 다섯 줄 이상인 책 한 권을 혼자 끝까지 읽는다",
                    materials = "저학년 문고", tip = "재미있으면 만화책도 좋아요. 끝까지 읽는 경험이 먼저.",
                ),
                ProjectPhase(
                    "p6", "넓게 읽기", "초3", 102, 52,
                    listOf(r("혼자 읽기(역사·과학 섞기)", READ, 25, 6), r("독서록 세 줄", WRITE, 10, 1), r("가족과 책 이야기", SPEAK, 10, 1)),
                    checkpoint = "한 달에 4권, 이야기 말고 다른 분야 한 권 이상",
                    materials = "지식 그림책·어린이 역사책", tip = "분야를 넓히되 좋아하는 분야는 그대로 두어요.",
                ),
                ProjectPhase(
                    "p7", "요약하기", "초4", 114, 52,
                    listOf(r("혼자 읽기", READ, 30, 5), r("문단 요약", WRITE, 10, 2), r("가족과 책 이야기", SPEAK, 10, 1)),
                    checkpoint = "읽은 책의 한 장을 한 문단(다섯 문장)으로 요약한다",
                    materials = "중학년 문고·어린이 신문", tip = "요약은 \"누가·무엇을·왜\" 세 가지부터.",
                ),
            ),
        ),
        ProjectPlan(
            id = "number-sense", category = ProjectCategory.MATH,
            title = "수 감각과 암산",
            goal = "초2 겨울까지 두 자리 덧셈·뺄셈을 암산하고, 곱셈의 뜻을 그림으로 설명하기",
            span = "만 4세 → 초2",
            why = "초등 수학의 첫 고비는 받아올림과 곱셈의 뜻입니다. 학습지로 속도를 올리기보다 구체물로 10을 가르고 모으는 경험이 " +
                "암산의 바탕이 됩니다. 하루 7분 놀이에서 시작해 초2에 20분 남짓까지만 늘립니다.",
            phases = listOf(
                ProjectPhase(
                    "p1", "수 세기 놀이", "만 4세", 48, 52,
                    listOf(r("생활 속에서 세고 비교하기", PLAY, 10, 5)),
                    checkpoint = "20까지 세고, 두 무리 가운데 어느 쪽이 많은지 말한다",
                    materials = "계단·과일·장난감", tip = "학습지 대신 생활에서.",
                ),
                ProjectPhase(
                    "p2", "가르기와 모으기", "만 5세", 60, 52,
                    listOf(r("구슬로 10 가르기·모으기", PLAY, 10, 5), r("주사위 보드게임", PLAY, 15, 2)),
                    checkpoint = "10을 두 수로 가르는 방법을 모두 말한다",
                    materials = "구슬 10개, 주사위 게임", tip = "손가락 쓰기는 자연스러운 단계예요.",
                ),
                ProjectPhase(
                    "p3", "한 자리 더하기", "만 6세", 72, 26,
                    listOf(r("더하기 놀이", PLAY, 10, 5), r("달력·시계 보기", PLAY, 5, 5), r("보드게임", PLAY, 15, 2)),
                    checkpoint = "한 자리 덧셈을 손가락 없이 5초 안에 말한다",
                    materials = "수 카드, 달력", tip = "틀려도 괜찮아요. 어떻게 셌는지 물어요.",
                ),
                ProjectPhase(
                    "p4", "받아올림 이해", "초1", 78, 52,
                    listOf(r("수학 익힘", PRACTICE, 15, 5), r("10 만들기 게임", PLAY, 5, 5), r("보드게임", PLAY, 15, 2)),
                    checkpoint = "받아올림 있는 한 자리 덧셈 20문제를 90% 이상 맞힌다",
                    materials = "수학 익힘책, 10칸 틀", tip = "받아올림은 \"10을 만들고 남은 것\"으로 설명해요.",
                ),
                ProjectPhase(
                    "p5", "두 자리 암산과 곱셈의 뜻", "초2", 90, 52,
                    listOf(
                        r("두 자리 암산 5문제", PRACTICE, 5, 5), r("수학 익힘", PRACTICE, 15, 5),
                        r("곱셈을 묶음 그림으로", PLAY, 10, 3), r("문장제 한두 개", PRACTICE, 10, 3),
                    ),
                    checkpoint = "두 자리 덧셈·뺄셈 암산 10문제를 풀고, \"3씩 4묶음\"을 그림으로 설명한다",
                    materials = "익힘책, 모눈종이", tip = "곱셈구구 외우기보다 뜻을 먼저.",
                ),
            ),
        ),
        ProjectPlan(
            id = "piano", category = ProjectCategory.MUSIC,
            title = "피아노 한 곡 완성",
            goal = "초6까지 좋아하는 곡 한 곡을 악보 보고 처음부터 끝까지 연주해 녹음하기(체르니 30 수준)",
            span = "만 5세 → 초6",
            why = "악기는 매일 짧게가 주 1회 길게보다 낫습니다. 한국 피아노 교육의 흔한 순서(바이엘 → 체르니 100 → 소나티네 → 체르니 30)를 따르되, " +
                "단계마다 좋아하는 곡을 한 곡씩 끼워 오래 이어 가게 합니다.",
            phases = listOf(
                ProjectPhase(
                    "p1", "건반과 친해지기", "만 5~6세", 60, 78,
                    listOf(r("건반 놀이·노래", PLAY, 10, 5)),
                    checkpoint = "도레미 자리를 알고 한 손으로 동요 한 곡을 친다",
                    materials = "건반(디지털 피아노도 좋음)", tip = "앉는 자세와 손 모양만 챙겨요.",
                ),
                ProjectPhase(
                    "p2", "바이엘", "초1", 78, 52,
                    listOf(r("바이엘 연습", PRACTICE, 15, 5)),
                    checkpoint = "두 손으로 쉬운 곡 3곡을 친다",
                    materials = "바이엘 교재", tip = "매일 15분이 주말 1시간보다 나아요.",
                ),
                ProjectPhase(
                    "p3", "체르니 100", "초2~3", 90, 104,
                    listOf(r("체르니 100 연습", PRACTICE, 20, 5), r("좋아하는 곡", PLAY, 10, 2)),
                    checkpoint = "체르니 100의 절반을 마치고, 가족 앞에서 작은 연주회를 한다",
                    materials = "체르니 100, 좋아하는 곡 악보", tip = "그만두고 싶은 때는 시간을 줄여서라도 이어 가요.",
                ),
                ProjectPhase(
                    "p4", "소나티네", "초4", 114, 52,
                    listOf(r("소나티네 연습", PRACTICE, 25, 5), r("연주 음악 듣기", LISTEN, 10, 2)),
                    checkpoint = "소나티네 한 악장을 처음부터 끝까지 친다",
                    materials = "소나티네 앨범", tip = "잘 치는 연주를 듣는 것도 연습이에요.",
                ),
                ProjectPhase(
                    "p5", "체르니 30과 곡 완성", "초5~6", 126, 104,
                    listOf(r("체르니 30 연습", PRACTICE, 30, 5), r("완성할 곡 연습", PRACTICE, 15, 2)),
                    checkpoint = "좋아하는 곡 한 곡을 처음부터 끝까지 연주해 녹음한다",
                    materials = "체르니 30, 완성할 곡 악보", tip = "녹음해 들어 보면 스스로 고칠 곳이 보여요.",
                ),
            ),
        ),
        ProjectPlan(
            id = "fitness", category = ProjectCategory.SPORT,
            title = "하루 60분 몸 쓰기",
            goal = "초6까지 줄넘기 2단 뛰기 10번, 25m 자유형, 하루 60분 몸 쓰는 습관 만들기",
            span = "초1 → 초6",
            why = "WHO는 5~17세에게 하루 평균 60분의 신체활동을 권합니다. 한국 초등학생은 학년이 오를수록 활동이 줄어서, " +
                "저학년에 바깥 놀이 습관을 먼저 만들고 종목 하나를 꾸준히 이어 갑니다. 초3부터 학교 생존수영이 시작돼요.",
            phases = listOf(
                ProjectPhase(
                    "p1", "매일 바깥 놀이", "초1", 78, 52,
                    listOf(r("놀이터·산책", PLAY, 40, 5), r("줄넘기", PRACTICE, 5, 5)),
                    checkpoint = "줄넘기 모아 뛰기 20번",
                    materials = "줄넘기(키에 맞게)", tip = "학원보다 놀이터가 먼저예요.",
                ),
                ProjectPhase(
                    "p2", "종목 하나 시작", "초2", 90, 52,
                    listOf(r("바깥 놀이", PLAY, 40, 4), r("줄넘기", PRACTICE, 10, 5), r("수영·태권도 등 종목 하나", PRACTICE, 50, 2)),
                    checkpoint = "줄넘기 앞으로 50번, 물에 뜨기",
                    materials = "종목 하나(아이가 고른 것)", tip = "잘하는 것보다 즐거운 것을 골라요.",
                ),
                ProjectPhase(
                    "p3", "체력 기르기", "초3~4", 102, 104,
                    listOf(r("줄넘기", PRACTICE, 10, 5), r("종목 연습", PRACTICE, 60, 2), r("바깥 놀이", PLAY, 40, 4)),
                    checkpoint = "25m 발차기로 가기, 줄넘기 엇갈려 뛰기",
                    materials = "학교 생존수영, 동네 체육 프로그램", tip = "주말 가족 활동 한 번이 큰 도움이 돼요.",
                ),
                ProjectPhase(
                    "p4", "도전 목표", "초5~6", 126, 104,
                    listOf(r("줄넘기 2단 뛰기 연습", PRACTICE, 10, 5), r("종목 연습", PRACTICE, 60, 3), r("바깥 활동", PLAY, 30, 4)),
                    checkpoint = "2단 뛰기 10번, 25m 자유형, 하루 60분 몸 쓰기를 한 달 이어 간다",
                    materials = "종목 하나", tip = "사춘기 앞두고 운동 습관이 잠과 기분을 지켜요.",
                ),
            ),
        ),
        ProjectPlan(
            id = "coding", category = ProjectCategory.CODING,
            title = "게임 하나 만들기에서 파이썬까지",
            goal = "중1까지 스크래치 게임 하나를 완성하고, 파이썬 기초 문제 30개 풀기",
            span = "초3 → 중1",
            why = "2022 개정 교육과정은 초등 정보 교육을 34시간 이상, 중학교를 68시간 이상으로 늘렸습니다. " +
                "블록 코딩으로 순서·반복·조건을 먼저 몸에 익히고, 작품 하나를 끝까지 만든 뒤 텍스트 코딩으로 넘어갑니다. 주 2~3회면 충분해요.",
            phases = listOf(
                ProjectPhase(
                    "p1", "순서 놀이와 블록 코딩", "초3", 102, 52,
                    listOf(r("순서·반복 보드게임", PLAY, 15, 2), r("엔트리·스크래치 따라 하기", PRACTICE, 20, 2)),
                    checkpoint = "캐릭터가 반복해서 움직이는 짧은 애니메이션을 만든다",
                    materials = "엔트리 또는 스크래치(무료)", tip = "화면 앞 시간은 한 번 20분 안.",
                ),
                ProjectPhase(
                    "p2", "스크래치 작품", "초4", 114, 52,
                    listOf(r("내 작품 만들기", PRACTICE, 30, 2), r("다른 작품 따라 만들기", PRACTICE, 20, 1)),
                    checkpoint = "조건(만약 ~라면)이 들어간 작품 두 개를 공유한다",
                    materials = "스크래치 튜토리얼", tip = "막히면 다른 작품의 코드를 열어 봐요.",
                ),
                ProjectPhase(
                    "p3", "게임 하나 완성", "초5", 126, 52,
                    listOf(r("게임 프로젝트", PRACTICE, 30, 3)),
                    checkpoint = "점수와 끝이 있는 게임 하나를 완성해 가족에게 해 보게 한다",
                    materials = "스크래치", tip = "작게 만들고 조금씩 늘려요.",
                ),
                ProjectPhase(
                    "p4", "파이썬 첫걸음", "초6", 138, 52,
                    listOf(r("파이썬 기초 강의·따라 치기", PRACTICE, 20, 3), r("작은 문제 풀기", PRACTICE, 15, 2)),
                    checkpoint = "변수·반복·조건을 써서 구구단 출력 프로그램을 만든다",
                    materials = "무료 파이썬 입문 강의, 온라인 실행기", tip = "오타를 찾는 것도 실력이에요.",
                ),
                ProjectPhase(
                    "p5", "문제 해결", "중1", 150, 52,
                    listOf(r("파이썬 문제 풀기", PRACTICE, 20, 3), r("작은 프로그램 만들기", PRACTICE, 30, 1), r("정보 수업 복습", REVIEW, 10, 1)),
                    checkpoint = "파이썬 기초 문제 30개를 풀고, 생활 속 작은 프로그램 하나를 만든다",
                    materials = "입문 문제집·온라인 저지 입문 문제", tip = "하루 한 문제면 충분해요.",
                ),
            ),
        ),
        ProjectPlan(
            id = "self-plan", category = ProjectCategory.HABIT,
            title = "스스로 계획하고 지키기",
            goal = "중1까지 주간 계획을 스스로 세우고 80% 이상 지키기",
            span = "초3 → 중1",
            why = "중학교에 가면 시험 범위와 수행평가를 스스로 챙겨야 합니다. 초3부터 \"오늘 할 일 확인\" → \"주간 계획 같이\" → \"혼자\" 순서로 " +
                "어른의 손을 조금씩 빼고, 타이머 기록으로 계획과 실제를 비교하는 눈을 기릅니다.",
            phases = listOf(
                ProjectPhase(
                    "p1", "오늘 할 일 보기", "초3", 102, 52,
                    listOf(r("오늘 할 일 확인", REVIEW, 5, 5), r("끝낸 것 체크", REVIEW, 5, 5)),
                    checkpoint = "알림 없이 한 달 동안 오늘 할 일을 스스로 확인한다",
                    materials = "이 앱의 오늘 화면", tip = "잔소리 대신 \"오늘 뭐 있어?\" 한마디.",
                ),
                ProjectPhase(
                    "p2", "주간 계획 같이 세우기", "초4", 114, 52,
                    listOf(r("일요일 주간 계획", WRITE, 15, 1), r("매일 확인", REVIEW, 5, 5), r("금요일 되돌아보기", REVIEW, 10, 1)),
                    checkpoint = "주간 계획의 60% 이상을 지킨 주가 네 번",
                    materials = "주간 계획표", tip = "계획은 작게, 지킨 것을 크게 칭찬.",
                ),
                ProjectPhase(
                    "p3", "혼자 세우는 계획", "초5", 126, 52,
                    listOf(r("주간 계획 혼자 세우기", WRITE, 20, 1), r("매일 확인", REVIEW, 5, 6), r("되돌아보기", REVIEW, 10, 1)),
                    checkpoint = "어른 도움 없이 세운 계획의 70% 이상을 지킨 주가 네 번",
                    materials = "주간 계획표", tip = "어른은 질문만: \"이번 주에 제일 중요한 건?\"",
                ),
                ProjectPhase(
                    "p4", "시간 재고 조정하기", "초6", 138, 52,
                    listOf(r("주간 계획", WRITE, 20, 1), r("타이머로 공부 시간 재기", REVIEW, 5, 5), r("계획과 실제 비교", REVIEW, 15, 1)),
                    checkpoint = "계획한 시간과 실제 시간의 차이를 보고 다음 주 계획을 스스로 고친다",
                    materials = "이 앱의 타이머·습관 화면", tip = "차이가 나는 것이 정상이에요. 고치는 힘이 목표.",
                ),
                ProjectPhase(
                    "p5", "시험 계획 스스로", "중1", 150, 52,
                    listOf(r("주간 계획", WRITE, 20, 1), r("시험 4주 전 계획", WRITE, 15, 1), r("매일 점검", REVIEW, 5, 6), r("되돌아보기", REVIEW, 15, 1)),
                    checkpoint = "주간 계획을 스스로 세우고 80% 이상 지킨 달이 한 번",
                    materials = "시험 범위표, 이 앱의 시험·목표", tip = "첫 지필은 방법을 배우는 시험이에요.",
                ),
            ),
        ),
    )

    val byId: Map<String, ProjectPlan> = plans.associateBy { it.id }

    fun byCategory(category: ProjectCategory): List<ProjectPlan> = plans.filter { it.category == category }

    private fun r(name: String, kind: RoutineKind, minutes: Int, daysPerWeek: Int) = RoutineItem(name, kind, minutes, daysPerWeek)
}
