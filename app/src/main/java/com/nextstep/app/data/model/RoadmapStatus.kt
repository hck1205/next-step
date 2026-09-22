package com.nextstep.app.data.model

/** 멘토가 큐레이팅한 로드맵 항목의 진행 상태. */
enum class RoadmapStatus(val label: String) {
    PLANNED("예정"),
    IN_PROGRESS("진행 중"),
    DONE("완료");

    companion object {
        fun from(value: String?): RoadmapStatus = entries.firstOrNull { it.name == value } ?: PLANNED
    }
}
