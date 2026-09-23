package com.nextstep.app.ui.progress.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nextstep.app.data.local.entity.TopicEntity

/** 학급 진도를 어느 단원까지로 할지 고르는 목록. -1 은 "아직 시작 전". */
@Composable
internal fun ClassProgressDialog(topics: List<TopicEntity>, classIndex: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학급 진도는 어디까지?") },
        text = {
            LazyColumn {
                item { TextButton(onClick = { onSelect(-1); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text("아직 시작 전") } }
                items(topics, key = { it.id }) { t ->
                    TextButton(onClick = { onSelect(t.orderIndex); onDismiss() }, modifier = Modifier.fillMaxWidth()) {
                        Text((if (t.orderIndex == classIndex) "● " else "") + t.title)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } },
    )
}
