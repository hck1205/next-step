package com.nextstep.app.domain.access

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.hub.HubAudience
import com.nextstep.app.domain.year.YearDoer

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
    /** 성장 여정(이정표 완료·메모·직접 추가). 학부모가 주도하지만 학생·멘토도 함께 관리합니다. */
    val canEditJourney: Boolean get() = true
    /** 장기 목표·단계 관리와 단계를 할 일로 보내기. 어린 자녀는 부모가, 이후엔 학생·멘토가 함께 관리합니다. */
    val canManageGoals: Boolean get() = true
    /** 활동 기록(취미·동아리·현장학습·체험) 추가·수정·삭제. */
    val canRecordActivities: Boolean get() = true
    /** 성장 기록(키·몸무게·시력)과 소질 관찰 메모. 학생 본인도 기록할 수 있습니다. */
    val canRecordGrowth: Boolean get() = true
    /** 인사이트의 "할 일로 추가" 실행. */
    val canApplyInsightActions: Boolean get() = isStudent || actsAsMentor

    /** 가족 탭에서 "멘토 겸하기" 스위치를 보여 줄지. 학부모만. */
    val canToggleMentorMode: Boolean get() = isParent
    /** 다자녀: 새 자녀 공간을 만들 수 있는지(학부모). */
    val canAddChildren: Boolean get() = isParent
    /** 다른 자녀·학생을 연결 코드로 붙일 수 있는지(학부모·멘토). 학생은 자기 공간 하나. */
    val canLinkChildren: Boolean get() = !isStudent
    /** 학생 화면 단계(새싹~나무)를 직접 고를 수 있는지. 아이의 속도를 가장 잘 아는 학부모만. */
    val canChooseStudentScreen: Boolean get() = isParent
    /** 연결된 학부모·멘토를 목록에서 제거할 수 있는지. 학생 본인과 학부모만. */
    val canRemoveMembers: Boolean get() = isStudent || isParent

    /** 기록 탭의 자리(관심사 순서와 보이는 섹션). 학부모 겸 멘토는 학부모 자리에서 보고, 멘토 화면은 따로 엽니다. */
    val hubAudience: HubAudience get() = when {
        isStudent -> HubAudience.STUDENT
        role == Role.MENTOR -> HubAudience.MENTOR
        else -> HubAudience.PARENT
    }

    /** "올해" 화면에서 "내 할 일"로 모아 볼 몫. 학생은 스스로·같이, 학부모는 엄마·아빠가·같이(멘토 겸하면 멘토 몫까지), 멘토는 멘토 몫. */
    val yearDoers: Set<YearDoer> get() = when {
        isStudent -> setOf(YearDoer.CHILD, YearDoer.TOGETHER)
        role == Role.MENTOR -> setOf(YearDoer.MENTOR)
        actsAsMentor -> setOf(YearDoer.PARENT, YearDoer.TOGETHER, YearDoer.MENTOR)
        else -> setOf(YearDoer.PARENT, YearDoer.TOGETHER)
    }

    /** 과제/로드맵에 기록될 작성자 역할. 학부모 겸 멘토는 MENTOR 로 남깁니다. */
    val actingRoleName: String get() = if (actsAsMentor && !isStudent) Role.MENTOR.name else role.name

    companion object {
        /** 프로필 역할과 내 구성원 정보로 권한을 만듭니다. 멘토 역할은 항상 멘토로, 학부모는 스위치를 켠 경우만. */
        fun of(role: Role, me: MemberEntity?): Capabilities = Capabilities(role, mentorEnabled = role == Role.MENTOR || (me?.mentorEnabled ?: false))
    }
}
