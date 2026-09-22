package com.nextstep.app.domain

import com.nextstep.app.data.model.Role

/**
 * 역할별로 볼 수 있는 화면과 쓸 수 있는 기능.
 *
 * - 학생: 자기 학습 기록(타이머, 단원 상태, 할 일 완료), 커리큘럼 스케줄링
 * - 학부모: 모니터링·격려·분석·재능 발견. 편집은 일정과 성적 입력 정도로 제한
 * - 멘토: 큐레이팅(로드맵), 학습 지도(단원·학급 진도·과제 배정·피드백)
 * - 학부모 겸 멘토: 학부모 기능 + 멘토 기능
 */
data class Capabilities(val role: Role, val mentorEnabled: Boolean) {
    val actsAsMentor: Boolean get() = role == Role.MENTOR || mentorEnabled
    val isStudent: Boolean get() = role == Role.STUDENT
    val isParent: Boolean get() = role == Role.PARENT

    /** 과목 추가·편집·삭제. */
    val canEditSubjects: Boolean get() = isStudent || actsAsMentor
    /** 단원 등록·삭제, 학급 진도 설정. */
    val canEditTopics: Boolean get() = isStudent || actsAsMentor
    /** 단원의 내 상태(예습/복습)와 이해도 기록은 학생 본인만. */
    val canMarkTopicStatus: Boolean get() = isStudent
    /** 일정(시간표·학원·시험)은 모두 등록 가능. */
    val canEditEvents: Boolean get() = true
    /** 성적 입력은 모두 가능 (학부모가 성적표를 대신 입력하는 경우가 많음). */
    val canEditGrades: Boolean get() = true
    /** 할 일 생성: 학생은 자기 할 일, 멘토는 과제 배정. 학부모(멘토 아님)는 할 일 대신 격려·메모. */
    val canCreateTasks: Boolean get() = isStudent || actsAsMentor
    val canCompleteTasks: Boolean get() = isStudent
    val canEditRoadmap: Boolean get() = actsAsMentor
    val canUpdateRoadmapProgress: Boolean get() = isStudent
    val canUseTimer: Boolean get() = isStudent
    val canGeneratePlan: Boolean get() = isStudent
    /** 인사이트의 "할 일로 추가" 실행. */
    val canApplyInsightActions: Boolean get() = isStudent || actsAsMentor

    /** 과제/로드맵에 기록될 작성자 역할. 학부모 겸 멘토는 MENTOR 로 남깁니다. */
    val actingRoleName: String get() = if (actsAsMentor && !isStudent) Role.MENTOR.name else role.name
}
