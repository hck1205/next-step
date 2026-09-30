package com.nextstep.app.domain.project

import com.nextstep.app.domain.project.ProjectRows.r
import com.nextstep.app.domain.project.RoutineKind.LISTEN
import com.nextstep.app.domain.project.RoutineKind.PLAY
import com.nextstep.app.domain.project.RoutineKind.PRACTICE
import com.nextstep.app.domain.project.RoutineKind.REVIEW
import com.nextstep.app.domain.project.RoutineKind.WRITE

/** 공부 밖에서 오래 키우는 힘: 악기 · 운동 · 코딩 · 자기주도. 규칙과 모으는 곳은 [ProjectCatalog]. */
internal object ActivityProjects {
    val all: List<ProjectPlan> = listOf(
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
}
