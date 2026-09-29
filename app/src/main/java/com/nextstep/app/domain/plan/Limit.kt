package com.nextstep.app.domain.plan

/** 무료일 때의 개수 한도([free])와 그 한도를 푸는 요금제. 강제하기 전에는 늘 무제한입니다. */
enum class Limit(val label: String, val free: Int, val plan: Plan) {
    CHILDREN("자녀", 1, Plan.FAMILY_PLUS),
    GUARDIANS("보호자", 2, Plan.FAMILY_PLUS),
    STUDENTS("맡는 학생", 2, Plan.TUTOR_PRO),
}
