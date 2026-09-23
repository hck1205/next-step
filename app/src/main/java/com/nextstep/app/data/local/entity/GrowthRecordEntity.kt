package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 성장 기록 한 번(키·몸무게·시력). 검진이나 집에서 잰 값을 날짜별로 남깁니다. 값은 비워 둘 수 있습니다.
 * 시력은 0.1~2.0 소수 표기(시력표 기준).
 */
@Entity(tableName = "growth_records", indices = [Index("familyId")])
data class GrowthRecordEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    /** 측정일 (epoch day). */
    val date: Long,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val visionLeft: Double? = null,
    val visionRight: Double? = null,
    val note: String = "",
    val createdByRole: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val isEmpty: Boolean get() = heightCm == null && weightKg == null && visionLeft == null && visionRight == null
}
