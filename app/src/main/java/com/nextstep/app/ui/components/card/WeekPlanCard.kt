package com.nextstep.app.ui.components.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.WeekAccess
import com.nextstep.app.domain.selfdirection.WeekStatus
import com.nextstep.app.ui.common.ratio
import com.nextstep.app.ui.components.dialog.ReflectionDialog
import com.nextstep.app.ui.components.dialog.WeekPlanDialog
import com.nextstep.app.ui.components.icon.moodIcon
import com.nextstep.app.ui.components.icon.moodLabel
import java.time.LocalDate

/**
 * 나의 이번 주(학생) · 이번 주 계획(어른): 자기주도 한 바퀴를 한 장에. 계획(목표 3개 · 시간) → 점검(체크 · 계획 대비 시간) → 확인 → 돌아보기.
 * 누가 무엇을 누를 수 있는지는 [access](역할 × 자기주도 단계)가 정하고, 계획·돌아보기 창은 이 카드가 엽니다.
 * [big] 은 어린 학생 화면(큰 줄, 숫자 없음)입니다.
 */
@Composable
fun WeekPlanCard(
    week: WeekStatus, access: WeekAccess, onSavePlan: (List<String>, Int) -> Unit, onToggle: (planId: String, index: Int) -> Unit,
    onApprove: (planId: String) -> Unit, onReflect: (week: LocalDate, mood: Int, good: String, hard: String, change: String) -> Unit,
    big: Boolean = false, onOpen: (() -> Unit)? = null,
) {
    var planning by remember { mutableStateOf(false) }
    var reflecting by remember { mutableStateOf(false) }
    val stage = week.stage
    val plan = week.plan
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (access.forChild) "나의 이번 주" else "이번 주 계획", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(
                    "자기주도 · ${stage.label}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary,
                    modifier = if (onOpen != null) Modifier.clickable(onClick = onOpen) else Modifier,
                )
            }
            if (!access.seesDetails) {
                Text(
                    if (week.hasPlan) "스스로 계획했어요 · 목표 ${plan?.goalList?.size ?: 0}개 중 ${plan?.doneCount ?: 0}개" else "아직 이번 주 계획 전이에요",
                    style = MaterialTheme.typography.bodyMedium,
                )
                week.lastReflection?.let { Text("돌아보기 · ${moodLabel(it.mood)}", style = MaterialTheme.typography.bodySmall) }
            } else {
                if (plan != null && week.hasPlan) {
                    plan.goalList.forEachIndexed { i, goal ->
                        val done = plan.isDone(i)
                        Row(
                            Modifier.heightIn(min = if (big) 52.dp else 36.dp).then(if (access.canCheck) Modifier.clickable { onToggle(plan.id, i) } else Modifier),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = if (done) "끝냈어요" else "아직",
                                tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(if (big) 30.dp else 22.dp),
                            )
                            Text(goal, style = if (big) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium, textDecoration = if (done) TextDecoration.LineThrough else null)
                        }
                    }
                    if (stage.plansMinutes && plan.plannedMinutes > 0 && !big) {
                        LabeledProgress(
                            label = "계획 ${plan.plannedMinutes}분 · 한 만큼 ${week.actualMinutes}분", ratio = ratio(week.actualMinutes, plan.plannedMinutes).coerceAtMost(1f),
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    if (stage.needsApproval) {
                        when {
                            plan.approvedAt != null -> Text("어른이 확인했어요", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            access.canApprove -> Button(onClick = { onApprove(plan.id) }) { Text("계획 확인했어요") }
                            else -> Text("어른의 확인을 기다려요", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (access.canPlan) TextButton(onClick = { planning = true }) { Text("계획 고치기") }
                } else if (access.canPlan) {
                    Button(onClick = { planning = true }) { Text(if (access.forChild) "이번 주 계획 세우기" else "이번 주 목표 정하기") }
                } else {
                    Text(
                        if (access.forChild) "이번 주 목표는 어른과 정해요" else "아이가 스스로 계획해요 · 아직 세우지 않았어요",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val reflectWeek = week.reflectWeek
                if (reflectWeek != null) {
                    if (access.canReflect) {
                        OutlinedButton(onClick = { reflecting = true }) { Text(if (week.reflectsLastWeek) "지난주 돌아보기" else "이번 주 돌아보기") }
                    } else {
                        Text("금요일에 아이가 한 주를 돌아봐요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    week.lastReflection?.let { r ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(moodIcon(r.mood), contentDescription = moodLabel(r.mood), tint = MaterialTheme.colorScheme.secondary)
                            Text(listOf(r.good, r.change.takeIf { it.isNotBlank() }?.let { "다음엔 $it" }.orEmpty()).filter { it.isNotBlank() }.joinToString(" · ").ifBlank { moodLabel(r.mood) }, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Text(
                if (access.forChild) "지금 내가 하는 것: ${stage.childDoes}" else "어른이 할 일: ${stage.adultDoes}",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (planning) {
        WeekPlanDialog(
            stage = stage, forChild = access.forChild, goals = plan?.goalList.orEmpty(), minutes = plan?.plannedMinutes ?: 0, hint = week.lastReflection?.change,
            onDismiss = { planning = false }, onSave = { goals, minutes -> onSavePlan(goals, minutes); planning = false },
        )
    }
    val reflectWeek = week.reflectWeek
    if (reflecting && reflectWeek != null) {
        ReflectionDialog(
            form = stage.reflection, title = if (week.reflectsLastWeek) "지난주 돌아보기" else "이번 주 돌아보기",
            onDismiss = { reflecting = false }, onSave = { mood, good, hard, change -> onReflect(reflectWeek, mood, good, hard, change); reflecting = false },
        )
    }
}
