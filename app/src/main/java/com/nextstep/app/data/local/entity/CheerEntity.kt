package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 해낸 일에 붙인 응원 하나(학부모 → 학생). 할 일 하나에 한 사람이 하나만 붙이고, 다시 누르면 바꾸거나 거둡니다.
 * 아이가 "고마워요"를 누르면 [seenAt] 이 남아 오늘 화면의 "받은 응원"에서 내려갑니다. 가족의 일이라 멘토에게는 넘기지 않습니다.
 */
@Entity(tableName = "cheers", indices = [Index("familyId"), Index("taskId")])
data class CheerEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val taskId: String,
    /** 응원할 때의 할 일 제목(할 일이 지워져도 받은 응원 문장은 남도록). */
    val taskTitle: String,
    /** CheerKind 이름. */
    val kind: String,
    val fromId: String = "",
    val fromRole: String = "",
    /** 보낸 사람의 부름(엄마·아빠·할머니 …). */
    val fromName: String = "",
    val seenAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
