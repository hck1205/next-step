package com.nextstep.app.data.model

/** 예체능·비교과 소질 영역. 교과 재능(TalentEngine)과 달리 활동·관찰 기록에서 찾습니다. */
enum class AptitudeDomain(val label: String, val keywords: List<String>) {
    MUSIC("음악", listOf("피아노", "바이올린", "첼로", "플루트", "기타", "드럼", "음악", "합창", "노래", "성악", "작곡", "우쿨렐레", "오케스트라", "밴드")),
    ART("미술", listOf("미술", "그림", "드로잉", "수채화", "만들기", "공예", "조소", "디자인", "일러스트", "만화", "점토", "전시")),
    SPORT("운동", listOf("축구", "농구", "야구", "수영", "태권도", "검도", "유도", "달리기", "육상", "체조", "줄넘기", "자전거", "스케이트", "스키", "테니스", "배드민턴", "탁구", "운동", "체육", "클라이밍", "골프", "승마")),
    DANCE("무용", listOf("발레", "무용", "댄스", "춤", "한국무용", "현대무용", "치어")),
    PERFORMANCE("연기·표현", listOf("연극", "뮤지컬", "연기", "발표", "스피치", "아나운서", "낭독", "인형극", "공연")),
    LANGUAGE("언어·글", listOf("글쓰기", "독서", "토론", "논술", "영어", "외국어", "일기", "시", "동화", "책", "도서관")),
    LOGIC("수리·탐구", listOf("코딩", "프로그래밍", "로봇", "수학", "과학", "실험", "천문", "체스", "바둑", "퍼즐", "발명", "메이커")),
    NATURE("자연·생명", listOf("자연", "곤충", "식물", "텃밭", "동물", "숲", "갯벌", "낚시", "캠핑", "관찰", "농장")),
    SOCIAL("사회성·리더십", listOf("봉사", "리더", "회장", "반장", "팀장", "동아리장", "기획", "운영", "친구", "멘토링"));

    /** 매칭용 소문자 키워드. 호출마다 소문자로 바꾸지 않도록 미리 계산합니다. */
    val lowerKeywords: List<String> = keywords.map { it.lowercase() }

    companion object {
        fun from(value: String?): AptitudeDomain = entries.firstOrNull { it.name == value } ?: LOGIC

        /** 활동 제목에서 영역을 추측합니다. 첫 번째로 맞는 키워드의 영역. 없으면 null. */
        fun guessFrom(text: String): AptitudeDomain? {
            val lower = text.lowercase()
            return entries.firstOrNull { d -> d.lowerKeywords.any { lower.contains(it) } }
        }
    }
}
