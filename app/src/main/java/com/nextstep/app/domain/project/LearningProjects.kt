package com.nextstep.app.domain.project

import com.nextstep.app.domain.project.ProjectRows.r
import com.nextstep.app.domain.project.RoutineKind.LISTEN
import com.nextstep.app.domain.project.RoutineKind.PLAY
import com.nextstep.app.domain.project.RoutineKind.PRACTICE
import com.nextstep.app.domain.project.RoutineKind.READ
import com.nextstep.app.domain.project.RoutineKind.REVIEW
import com.nextstep.app.domain.project.RoutineKind.SPEAK
import com.nextstep.app.domain.project.RoutineKind.WRITE

/** 공부와 이어지는 오래 키우는 힘: 영어 읽기 · 한글 읽기 · 수 감각. 규칙과 모으는 곳은 [ProjectCatalog]. */
internal object LearningProjects {
    val all: List<ProjectPlan> = listOf(
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
    )
}
