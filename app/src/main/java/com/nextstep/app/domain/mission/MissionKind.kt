package com.nextstep.app.domain.mission

import com.nextstep.app.domain.growth.GrowthStage

/**
 * 날짜가 정해진 목표의 종류. 한국 학교에서 성적과 입시를 좌우하는 일정들입니다.
 * [needsSubject] 이면 과목 이름이 목표 제목에 붙습니다(예: "수학 수행평가").
 * 저장은 GoalEntity.trackId 에 [trackId] 로 남겨 종류를 되찾습니다.
 */
enum class MissionKind(val label: String, val needsSubject: Boolean, val isExam: Boolean, private val stages: Set<GrowthStage>) {
    UNIT_TEST("단원평가", true, true, setOf(GrowthStage.EARLY_ELEMENTARY, GrowthStage.UPPER_ELEMENTARY)),
    EXAM("중간·기말고사", false, true, setOf(GrowthStage.MIDDLE, GrowthStage.HIGH)),
    PERFORMANCE("수행평가", true, false, setOf(GrowthStage.UPPER_ELEMENTARY, GrowthStage.MIDDLE, GrowthStage.HIGH)),
    CLUB("동아리 활동", false, false, setOf(GrowthStage.MIDDLE, GrowthStage.HIGH)),
    CSAT("수능", false, true, setOf(GrowthStage.HIGH)),
    EARLY_ADMISSION("수시 원서", false, false, setOf(GrowthStage.HIGH));

    val trackId: String get() = PREFIX + name

    fun fits(stage: GrowthStage?): Boolean = stage != null && stage in stages

    companion object {
        private const val PREFIX = "mission:"

        fun ofTrackId(trackId: String?): MissionKind? =
            trackId?.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)?.let { name -> entries.firstOrNull { it.name == name } }

        /** 이 성장 단계에서 만들 수 있는 종류. 단계를 모르면 없음. */
        fun forStage(stage: GrowthStage?): List<MissionKind> = entries.filter { it.fits(stage) }
    }
}
