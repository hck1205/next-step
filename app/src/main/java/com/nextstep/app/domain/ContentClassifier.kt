package com.nextstep.app.domain

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel

/** 자동 분류 결과. 사용자가 등록 화면에서 수정할 수 있습니다. */
data class ContentClassification(
    val subjectKey: String,
    val gradeLevel: GradeLevel,
    val contentType: ContentType,
    val keywords: List<String>,
    /** 분류 근거. UI 에서 "왜 이렇게 분류했는지" 보여주고 나중에 모델 학습 데이터로 씁니다. */
    val reasons: List<String>,
)

/**
 * 유튜브 링크 등록 시 제목·채널·설명에서 과목/학년/유형/키워드를 뽑는 규칙 기반 분류기.
 * 서버 없이 기기에서 동작하도록 사전(dictionary) 매칭으로 만들었고, 나중에 LLM 분류로 교체할 때
 * 같은 [ContentClassification] 을 내도록 인터페이스를 유지합니다.
 */
object ContentClassifier {

    /** 과목 키 → 제목에서 찾을 힌트 단어들. 단원명이 곧 과목 힌트가 되도록 넓게 잡았습니다. */
    val subjectHints: Map<String, List<String>> = linkedMapOf(
        "수학" to listOf("수학", "수1", "수2", "수I", "수II", "미적", "확통", "기하", "방정식", "함수", "도형", "인수분해", "다항식", "집합", "명제", "지수", "로그", "삼각", "수열", "극한", "미분", "적분", "확률", "통계", "정수", "유리수", "무리수", "제곱근", "피타고라스", "닮음", "원주각", "연립", "부등식", "좌표", "비례", "분수", "소수", "약수", "배수", "각도", "넓이", "부피"),
        "영어" to listOf("영어", "english", "영문법", "문법", "독해", "리스닝", "listening", "reading", "grammar", "vocab", "단어", "어휘", "영작", "회화", "관계대명사", "분사", "가정법", "수동태", "to부정사", "동명사", "시제", "조동사", "비교급", "toeic", "토익", "텝스", "수능영어", "모의고사 영어"),
        "국어" to listOf("국어", "문학", "비문학", "독서", "화법", "작문", "문법", "고전", "현대시", "현대소설", "고전시가", "수필", "시조", "향가", "가사", "음운", "형태소", "품사", "문장 성분", "띄어쓰기", "맞춤법", "한자", "논술", "글쓰기", "어휘력"),
        "과학" to listOf("과학", "물리", "화학", "생물", "생명과학", "지구과학", "지학", "운동", "힘", "에너지", "전기", "자기", "빛", "파동", "원소", "주기율표", "화학 반응", "산과 염기", "세포", "유전", "광합성", "호흡", "생태계", "지층", "화산", "지진", "판구조", "태양계", "별", "기권", "수권", "날씨", "물질", "혼합물", "용해도", "열", "밀도"),
        "사회" to listOf("사회", "지리", "일반사회", "정치", "경제", "법", "문화", "인권", "헌법", "민주주의", "시장", "수요", "공급", "무역", "환율", "기후", "지형", "인구", "도시", "자원", "국제", "사회문화", "생활과 윤리", "윤리"),
        "역사" to listOf("역사", "한국사", "세계사", "동아시아사", "고조선", "삼국", "고구려", "백제", "신라", "고려", "조선", "일제", "독립", "근대", "현대사", "임진왜란", "병자호란", "세종", "이순신", "프랑스 혁명", "산업혁명", "냉전", "로마", "그리스"),
        "코딩" to listOf("코딩", "프로그래밍", "파이썬", "python", "스크래치", "scratch", "엔트리", "알고리즘", "자바", "java", "c언어", "javascript", "html", "정보", "컴퓨터"),
        "한문" to listOf("한문", "한자", "사자성어", "고사성어"),
        "제2외국어" to listOf("일본어", "중국어", "스페인어", "프랑스어", "독일어", "jlpt", "hsk"),
        "예체능" to listOf("음악", "미술", "체육", "악보", "코드", "피아노", "드로잉", "수채화", "스트레칭", "줄넘기"),
    )

    private val gradeHints: List<Pair<GradeLevel, List<String>>> = listOf(
        GradeLevel.HIGH to listOf("고등", "고1", "고2", "고3", "수능", "모의고사", "모평", "학평", "내신 고", "수1", "수2", "수I", "수II", "미적", "확통", "기하", "생명과학", "지구과학", "물리학", "화학1", "화학2", "사회문화", "생활과 윤리", "한국지리", "세계지리", "동아시아사", "정치와 법", "경제", "n수", "재수", "정시", "수시"),
        GradeLevel.MIDDLE to listOf("중등", "중1", "중2", "중3", "중학", "중학교", "중학생", "고입", "특목고", "자사고", "중간고사", "기말고사"),
        GradeLevel.ELEMENTARY to listOf("초등", "초1", "초2", "초3", "초4", "초5", "초6", "초등학교", "초등학생", "어린이", "키즈", "kids", "구구단", "받아쓰기", "학년 수학"),
    )

    private val typeHints: List<Pair<ContentType, List<String>>> = listOf(
        ContentType.EXAM_PREP to listOf("시험 대비", "시험대비", "내신", "중간고사", "기말고사", "기출", "모의고사", "수능", "족보", "예상 문제", "벼락치기", "총정리", "1등급"),
        ContentType.PROBLEM to listOf("문제 풀이", "문제풀이", "문풀", "풀이", "유형", "해설", "기출문제", "예제", "연습문제", "실전"),
        ContentType.SUMMARY to listOf("요약", "정리", "핵심", "한번에", "한 번에", "10분", "5분", "총정리", "개념정리", "요점", "암기", "정리노트", "마인드맵", "치트키"),
        ContentType.STUDY_METHOD to listOf("공부법", "공부 방법", "학습법", "노트 정리", "플래너", "시간 관리", "암기법", "집중력", "루틴", "습관", "성적 올리는", "공부 잘하는", "메타인지", "오답노트"),
        ContentType.MOTIVATION to listOf("동기부여", "멘탈", "슬럼프", "번아웃", "응원", "합격 수기", "공부 자극", "열정", "포기하지", "불안", "스트레스", "마음가짐"),
        ContentType.DOCUMENTARY to listOf("다큐", "다큐멘터리", "교양", "역사 이야기", "과학 이야기", "지식", "상식", "ted", "강연", "이야기"),
        ContentType.CONCEPT to listOf("개념", "강의", "강좌", "설명", "이해", "원리", "기초", "입문", "완전정복", "1강", "2강", "3강", "단원", "쉽게", "제대로"),
    )

    private val stopWords = setOf("영상", "강의", "공부", "학습", "유튜브", "youtube", "채널", "구독", "좋아요", "shorts", "full", "part", "편", "회", "the", "and", "for", "with")

    fun classify(title: String, channel: String = "", description: String = "", familySubjectNames: List<String> = emptyList()): ContentClassification {
        val text = listOf(title, channel, description).joinToString(" ")
        val lower = text.lowercase()
        val reasons = mutableListOf<String>()

        // 1. 과목: 가족이 등록한 과목명이 제목에 있으면 최우선, 아니면 힌트 사전 점수
        val familyHit = familySubjectNames.firstOrNull { it.isNotBlank() && lower.contains(it.lowercase()) }
        val subjectScores = subjectHints.mapValues { (_, hints) -> hints.count { lower.contains(it.lowercase()) } }
        val bestSubject = subjectScores.maxByOrNull { it.value }
        val subjectKey = when {
            familyHit != null -> { reasons += "제목에 과목명 '$familyHit'"; familyHit }
            bestSubject != null && bestSubject.value > 0 -> {
                val matched = subjectHints[bestSubject.key]!!.filter { lower.contains(it.lowercase()) }.take(3)
                reasons += "${bestSubject.key} 힌트: ${matched.joinToString()}"
                bestSubject.key
            }
            else -> ""
        }

        // 2. 학년
        val grade = gradeHints.firstOrNull { (_, hints) -> hints.any { lower.contains(it.lowercase()) } }
        val gradeLevel = grade?.first ?: GradeLevel.ALL
        grade?.let { (g, hints) -> reasons += "${g.label}: ${hints.first { lower.contains(it.lowercase()) }}" }

        // 3. 유형: 앞쪽(우선순위 높은) 힌트가 먼저 매칭됨. 아무것도 없고 과목이 있으면 개념 강의로 추정.
        val type = typeHints.firstOrNull { (_, hints) -> hints.any { lower.contains(it.lowercase()) } }
        val contentType = type?.first ?: if (subjectKey.isNotEmpty()) ContentType.CONCEPT else ContentType.OTHER
        type?.let { (t, hints) -> reasons += "${t.label}: ${hints.first { lower.contains(it.lowercase()) }}" }

        // 4. 키워드: 과목 힌트 중 매칭된 단원성 단어 + 제목의 명사성 토큰
        val hintKeywords = subjectHints.values.flatten().filter { it.length >= 2 && lower.contains(it.lowercase()) && it != subjectKey }
        val tokens = title.split(Regex("[\\s\\[\\]()|/,.:!?\"'#~\\-_]+"))
            .map { it.trim() }
            .filter { it.length in 2..12 && it.lowercase() !in stopWords && !it.all { c -> c.isDigit() } }
        val keywords = (hintKeywords + tokens).distinct().take(8)

        return ContentClassification(subjectKey, gradeLevel, contentType, keywords, reasons)
    }
}
