package com.nextstep.app.data.repository

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
    /** 학생 학년(1~12). 성장 단계의 근거. */
    suspend fun setGradeYear(memberId: String, gradeYear: Int)
    suspend fun remove(memberId: String)
}
