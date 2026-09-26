package com.nextstep.app.domain.project

import com.nextstep.app.domain.journey.GoalArea

/**
 * 교육 프로젝트의 분류. 학교 과목과 달리 몇 년에 걸쳐 조금씩 키우는 힘입니다.
 * [goalArea] 는 목표 저장소(GoalEntity.area)에 남길 영역입니다.
 */
enum class ProjectCategory(val label: String, val goalArea: GoalArea) {
    ENGLISH("영어", GoalArea.LANGUAGE),
    READING("독서·문해력", GoalArea.KOREAN),
    MATH("수 감각", GoalArea.MATH),
    MUSIC("악기", GoalArea.EXPERIENCE),
    SPORT("운동·체력", GoalArea.EXPERIENCE),
    CODING("코딩", GoalArea.HOBBY),
    HABIT("자기주도", GoalArea.HABIT),
}
