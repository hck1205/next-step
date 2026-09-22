package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 가족(학생 1명 단위)에 연결된 구성원. 학생 본인, 학부모, 멘토 모두 한 행씩 가집니다.
 * 멘토는 여러 명이 연결될 수 있고, 각자 담당 과목을 지정합니다.
 */
@Entity(tableName = "members", indices = [Index("familyId")])
data class MemberEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val role: String,
    val name: String,
    /** 멘토의 구분 (예: 수학 과외, 담임 선생님). */
    val title: String = "",
    /** 담당 과목 ID 목록. 쉼표로 구분. 비어 있으면 전 과목. */
    val subjectIds: String = "",
    /**
     * 멘토 기능(로드맵 큐레이팅, 과제 배정, 학급 진도 관리) 사용 여부.
     * 멘토는 항상 true, 학부모는 설정에서 켜서 "학부모 겸 멘토"가 될 수 있습니다.
     */
    val mentorEnabled: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val subjectIdList: List<String> get() = subjectIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun covers(subjectId: String?): Boolean = subjectIdList.isEmpty() || (subjectId != null && subjectId in subjectIdList)
}
