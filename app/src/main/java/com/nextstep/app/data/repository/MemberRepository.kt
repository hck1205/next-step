package com.nextstep.app.data.repository

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.data.local.entity.MemberEntity
import kotlinx.coroutines.flow.Flow

/** 가족 구성원(학생·학부모·멘토) 관리. */
interface MemberRepository {
    val members: Flow<List<MemberEntity>>
    /** 이 기기 사용자의 구성원 정보. */
    val myMember: Flow<MemberEntity?>

    /** 멘토 담당 과목. 비우면 전 과목. */
    suspend fun setSubjects(memberId: String, subjectIds: List<String>)
    suspend fun updateProfile(memberId: String, name: String, title: String)
    /** 학부모가 멘토 역할을 겸할지. 멘토 본인은 항상 켜져 있습니다. */
    suspend fun setMentorEnabled(memberId: String, enabled: Boolean)
    /** 학생 학년(1~18). 생년월일이 없을 때 성장 단계의 근거. */
    suspend fun setGradeYear(memberId: String, gradeYear: Int)
    /** 학생 생년월일. null 이면 지웁니다. 여정 타임라인의 기준. */
    suspend fun setBirthDate(memberId: String, birthDate: java.time.LocalDate?)
    /** 학생 화면 단계를 직접 고릅니다. null 이면 학년에 맞춰 자동. */
    suspend fun setUiLevel(memberId: String, level: StudentUiLevel?)
    /** 학생이 이 화면 단계를 확인했다고 남깁니다("새 화면" 카드를 닫음). */
    suspend fun markUiLevelSeen(memberId: String, level: StudentUiLevel)
    /** 자기주도 단계를 직접 고릅니다. null 이면 화면 단계에 맞춰 자동. */
    suspend fun setSelfDirection(memberId: String, stage: SelfDirectionStage?)
    suspend fun remove(memberId: String)
}
