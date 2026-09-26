package com.nextstep.app.domain.selfdirection

import com.nextstep.app.domain.growth.StudentUiLevel

/**
 * 자기주도 사다리. 어릴 때는 어른이 계획하고 옆에서 같이 하다가, 한 칸씩 올라가며 계획 → 점검 → 돌아보기를 아이에게 넘겨줍니다
 * (책임 이양: 어른이 한다 → 같이 한다 → 아이가 하고 어른은 확인 → 아이가 혼자).
 * 기본 단계는 학생 화면 단계([StudentUiLevel])를 따르고, 학부모는 준비 신호를 보고 한 칸씩 올리거나 내릴 수 있습니다.
 * 화면은 이 값만 읽고 학년으로 분기하지 않습니다. 이름은 저장값이라 바꾸지 않습니다.
 */
enum class SelfDirectionStage(
    val label: String,
    val span: String,
    private val owners: Map<LoopStep, Owner>,
    /** 이 단계에서 아이가 하는 것(아이에게 보이는 말). */
    val childDoes: String,
    /** 이 단계에서 어른이 할 일. */
    val adultDoes: String,
    /** 다음 단계에서 넘겨줄 것. 마지막 단계는 null. */
    val handOverNext: String?,
    val reflection: ReflectionForm,
    /** 아이가 쓴 주간 계획을 어른이 "확인"해 주는 단계. */
    val needsApproval: Boolean,
    /** 주간 계획에 분(시간)을 적는지. 어린 단계는 목표만. */
    val plansMinutes: Boolean,
    /** 어른이 계획·돌아보기의 세부 내용까지 보는지. 마지막 단계는 요약만. */
    val adultSeesDetails: Boolean,
) {
    FOLLOW(
        "따라 해요", "학령 전", ownersOf(Owner.ADULT, Owner.TOGETHER, Owner.TOGETHER, Owner.TOGETHER),
        childDoes = "끝낸 것에 스티커를 붙이고, 이번 주 기분을 골라요",
        adultDoes = "이번 주 목표는 어른이 정하고 옆에서 같이 해요. 금요일에 기분을 같이 골라요",
        handOverNext = "두 가지 중 무엇을 먼저 할지 아이가 고르게 해요",
        reflection = ReflectionForm.FACES, needsApproval = false, plansMinutes = false, adultSeesDetails = true,
    ),
    CHOOSE(
        "골라요", "초1–2", ownersOf(Owner.ADULT, Owner.CHILD, Owner.CHILD, Owner.TOGETHER),
        childDoes = "무엇부터 할지 고르고, 끝낸 목표를 스스로 체크해요",
        adultDoes = "이번 주 목표 2~3개는 어른이 정하고, 금요일에 제일 좋았던 것을 같이 이야기해요",
        handOverNext = "이번 주 목표 하나를 아이가 직접 정해 보게 해요",
        reflection = ReflectionForm.FACE_AND_BEST, needsApproval = false, plansMinutes = false, adultSeesDetails = true,
    ),
    PLAN_TOGETHER(
        "같이 계획해요", "초3–4", ownersOf(Owner.TOGETHER, Owner.CHILD, Owner.CHILD, Owner.CHILD),
        childDoes = "주간 목표를 어른과 같이 정하고, 금요일에 혼자 돌아봐요",
        adultDoes = "일요일 10분, 계획을 같이 세우고 아이의 돌아보기를 읽어 줘요",
        handOverNext = "계획을 아이가 먼저 쓰고 어른은 확인만 해요",
        reflection = ReflectionForm.THREE_LINES, needsApproval = false, plansMinutes = true, adultSeesDetails = true,
    ),
    PLAN_FIRST(
        "먼저 계획해요", "초5–6", ownersOf(Owner.CHILD, Owner.CHILD, Owner.CHILD, Owner.CHILD),
        childDoes = "주간 계획을 먼저 세우고, 계획한 시간과 실제 시간을 비교해 다음 주를 고쳐요",
        adultDoes = "계획을 읽고 \"확인\"만 눌러요. 고쳐 주지 말고 질문 하나만 해요",
        handOverNext = "확인 없이 계획을 맡겨요",
        reflection = ReflectionForm.THREE_LINES, needsApproval = true, plansMinutes = true, adultSeesDetails = true,
    ),
    SELF(
        "스스로 해요", "중1–3", ownersOf(Owner.CHILD, Owner.CHILD, Owner.CHILD, Owner.CHILD),
        childDoes = "계획 · 점검 · 돌아보기를 혼자 해요. 시험 계획도 스스로 세워요",
        adultDoes = "요약만 보고, 아이가 도움을 청할 때 들어가요",
        handOverNext = "세부 내용은 아이가 보여 줄 때만 봐요",
        reflection = ReflectionForm.THREE_LINES, needsApproval = false, plansMinutes = true, adultSeesDetails = true,
    ),
    OWN(
        "내가 주인", "고1부터", ownersOf(Owner.CHILD, Owner.CHILD, Owner.CHILD, Owner.CHILD),
        childDoes = "계획부터 공유까지 전부 스스로 정해요",
        adultDoes = "주간 요약 한 줄과 잠·컨디션만 챙겨요",
        handOverNext = null,
        reflection = ReflectionForm.THREE_LINES, needsApproval = false, plansMinutes = true, adultSeesDetails = false,
    );

    fun owner(step: LoopStep): Owner = owners.getValue(step)

    /** 아이가 스스로 맡은 걸음 수(0~4). */
    val childSteps: Int get() = LoopStep.entries.count { owner(it) == Owner.CHILD }

    val next: SelfDirectionStage? get() = entries.getOrNull(ordinal + 1)
    val previous: SelfDirectionStage? get() = entries.getOrNull(ordinal - 1)

    companion object {
        fun fromName(name: String?): SelfDirectionStage? = entries.firstOrNull { it.name == name }

        /** 학생 화면 단계에 맞춘 기본 단계. */
        fun defaultFor(level: StudentUiLevel): SelfDirectionStage = when (level) {
            StudentUiLevel.SEED -> FOLLOW
            StudentUiLevel.SPROUT -> CHOOSE
            StudentUiLevel.SEEDLING -> PLAN_TOGETHER
            StudentUiLevel.STEM -> PLAN_FIRST
            StudentUiLevel.BRANCH -> SELF
            StudentUiLevel.TREE -> OWN
        }
    }
}

private fun ownersOf(plan: Owner, act: Owner, check: Owner, reflect: Owner): Map<LoopStep, Owner> =
    mapOf(LoopStep.PLAN to plan, LoopStep.DO to act, LoopStep.CHECK to check, LoopStep.REFLECT to reflect)
