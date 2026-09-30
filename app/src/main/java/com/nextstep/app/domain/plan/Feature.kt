package com.nextstep.app.domain.plan

/**
 * 나중에 요금제로 나눌 수 있는 기능 목록 한 곳. 화면·ViewModel 은 `caps.has(Feature.X)` 로만 묻습니다(요금제 이름으로 분기하지 않음).
 * 학생 화면 기능과 매일 쓰는 가족 흐름(오늘 · 할 일 · 가족 달력 · 응원 · 주말 이야기 · 알림 · 이번 주 피드백 · 학교 일정)은 여기에 넣지 않습니다 — 늘 무료.
 */
enum class Feature(val label: String, val plan: Plan) {
    PERIOD_REPORT("월간·학기 리포트", Plan.FAMILY_PLUS),
    LONG_TRENDS("학기·학년 흐름", Plan.FAMILY_PLUS),
    GROWTH_ALBUM("올해의 성장 앨범", Plan.FAMILY_PLUS),
    TALENT_DETAIL("재능·소질 신호 전체", Plan.FAMILY_PLUS),
    PDF_EXPORT("PDF로 보내기", Plan.FAMILY_PLUS),
    MONTHLY_LESSON_REPORT("월간 수업 리포트", Plan.TUTOR_PRO),
    REPORT_SIGNATURE("리포트 서명", Plan.TUTOR_PRO),
    REPORT_LOG("리포트 보낸 기록", Plan.TUTOR_PRO),
    ROADMAP_TEMPLATES("로드맵 템플릿", Plan.TUTOR_PRO),
    BULK_ASSIGN("여러 학생에게 과제 한 번에", Plan.TUTOR_PRO),
    LESSONS("수업 일정·출결·수업료", Plan.TUTOR_PRO),
}
