package com.nextstep.app.domain.plan

/** 요금제. 지금은 모두 무료로 열려 있고([PlanPolicy.ENFORCED] = false), 결제를 붙이면 [Entitlements.plans] 로 들어옵니다. */
enum class Plan(val label: String) {
    FREE("무료"),
    /** 학부모: 더 깊이(월간·학기 리포트) · 더 오래(학기·학년 흐름) · 더 많이(자녀·보호자) · 성장 앨범 · 광고 없음. */
    FAMILY_PLUS("가족 플러스"),
    /** 멘토: 학생 여러 명 · 월간 수업 리포트(서명·PDF·보낸 기록) · 로드맵 템플릿 · 과제 한 번에 · 수업·출결·수업료. */
    TUTOR_PRO("튜터 Pro"),
}
