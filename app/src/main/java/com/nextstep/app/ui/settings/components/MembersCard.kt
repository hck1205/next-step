package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.ui.components.card.AppCard

/** 연결된 구성원 목록. 나와 학생은 제거할 수 없고, [canRemove] 인 역할만 다른 구성원을 끊을 수 있습니다. */
@Composable
internal fun MembersCard(members: List<MemberEntity>, me: MemberEntity?, subjects: List<SubjectEntity>, canRemove: Boolean, onRemove: (String) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (members.isEmpty()) Text("구성원 정보가 아직 동기화되지 않았어요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            members.forEach { m ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(m.name + (if (m.id == me?.id) " (나)" else ""), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(memberDetail(m, subjects), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (canRemove && m.id != me?.id && !m.isStudent) TextButton(onClick = { onRemove(m.id) }) { Text("연결 끊기") }
                }
            }
            Text("학부모(엄마, 아빠 등)와 멘토(선생님·과외·튜터)는 여러 명이 같은 코드로 연결할 수 있어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun memberDetail(m: MemberEntity, subjects: List<SubjectEntity>): String = buildList {
    add(m.roleLabel)
    if (m.title.isNotBlank() && !m.isParent) add(m.title)
    if (m.isParent && m.mentorEnabled) add("멘토 겸")
    if (m.isMentor || m.mentorEnabled) add(if (m.subjectIdList.isEmpty()) "전 과목" else subjects.filter { it.id in m.subjectIdList }.joinToString { it.name })
}.joinToString(" · ")
