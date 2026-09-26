package com.nextstep.app.domain.hub

/**
 * 기록 탭을 보는 사람의 자리. 같은 관심사라도 역할마다 먼저 보고 싶은 순서가 다릅니다:
 * 누구에게나 "목표·할 일"(무엇을 향해 무엇을 하고 있나)이 한눈에 바로 다음이고, 그다음 학생은 "배울 것"이, 학부모·멘토는 "공부"가 옵니다.
 * 어느 자리인지는 [com.nextstep.app.domain.access.Capabilities.hubAudience] 가 정합니다.
 */
enum class HubAudience(val order: List<Concern>) {
    PARENT(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.STUDY, Concern.LEARN, Concern.PROJECT, Concern.EXAMS, Concern.GROWTH, Concern.DISCOVER)),
    STUDENT(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.LEARN, Concern.PROJECT, Concern.STUDY, Concern.EXAMS, Concern.DISCOVER, Concern.GROWTH)),
    MENTOR(listOf(Concern.OVERVIEW, Concern.PLAN, Concern.STUDY, Concern.LEARN, Concern.PROJECT, Concern.EXAMS, Concern.DISCOVER, Concern.GROWTH)),
}
