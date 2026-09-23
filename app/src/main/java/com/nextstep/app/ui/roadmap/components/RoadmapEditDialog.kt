package com.nextstep.app.ui.roadmap.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.SubjectPicker
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun RoadmapEditDialog(
    existing: RoadmapItemEntity?,
    subjects: List<SubjectEntity>,
    contents: List<ContentEntity>,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)?,
    onSave: (String?, String, String, String, LocalDate?, String?) -> Unit,
) {
    var contentId by remember { mutableStateOf(existing?.contentId) }
    var contentMenu by remember { mutableStateOf(false) }
    var subjectId by remember { mutableStateOf(existing?.subjectId) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var res by remember { mutableStateOf(existing?.resource ?: "") }
    var hasDate by remember { mutableStateOf(existing?.targetDate != null) }
    var date by remember { mutableStateOf(existing?.targetDate?.let { DateUtils.fromEpochDay(it) } ?: DateUtils.today().plusDays(7)) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "로드맵 항목 추가" else "로드맵 항목 편집") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("무엇을 (예: 일차방정식 개념 정리)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("어떻게 (방법·범위·주의점)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = res, onValueChange = { res = it }, label = { Text("자료 (교재명, 강의, 링크)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (contents.isNotEmpty()) {
                    val candidates = contents.filter { c -> subjectId == null || c.subjectKey.isBlank() || subjects.firstOrNull { it.id == subjectId }?.name == c.subjectKey }
                    val selected = contents.firstOrNull { it.id == contentId }
                    androidx.compose.foundation.layout.Box {
                        OutlinedButton(onClick = { contentMenu = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selected?.let { "영상: ${it.title}" } ?: "저장소 영상 연결 (선택)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = contentMenu, onDismissRequest = { contentMenu = false }) {
                            DropdownMenuItem(text = { Text("연결 안 함") }, onClick = { contentId = null; contentMenu = false })
                            candidates.take(30).forEach { c ->
                                DropdownMenuItem(text = { Text("${c.subjectKey.ifBlank { "-" }} · ${c.title}", maxLines = 1, overflow = TextOverflow.Ellipsis) }, onClick = { contentId = c.id; contentMenu = false })
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("목표일 설정", modifier = Modifier.weight(1f))
                    Switch(checked = hasDate, onCheckedChange = { hasDate = it })
                }
                if (hasDate) DateField("목표일", date, onChange = { date = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(subjectId, title.trim(), desc.trim(), res.trim(), if (hasDate) date else null, contentId); onDismiss() }) { Text("저장") } },
        dismissButton = {
            Row {
                if (onDelete != null) TextButton(onClick = { onDelete(); onDismiss() }) { Text("삭제", color = MaterialTheme.colorScheme.error) }
                TextButton(onClick = onDismiss) { Text("취소") }
            }
        },
    )
}
