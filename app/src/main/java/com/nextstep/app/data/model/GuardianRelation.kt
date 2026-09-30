package com.nextstep.app.data.model

/**
 * 학부모(보호자) 구성원의 관계. 한 자녀에 엄마·아빠·조부모·보호자가 함께 연결될 수 있습니다.
 * 저장은 MemberEntity.title 에 [label] 로 합니다(멘토는 같은 칸에 "수학 과외" 같은 구분을 둡니다).
 */
enum class GuardianRelation(val label: String) {
    MOM("엄마"),
    DAD("아빠"),
    GRANDMA("할머니"),
    GRANDPA("할아버지"),
    GUARDIAN("보호자");

    companion object {
        fun fromLabel(label: String?): GuardianRelation? = entries.firstOrNull { it.label == label }
    }
}
