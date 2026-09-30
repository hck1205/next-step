package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 상단 바 오른쪽 ⋮ 메뉴. 본문에 흩어져 있던 "다른 화면으로 가기"(영상 저장소 · 멘토 화면 · 학습 계획 · 목표 · 활동)를 여기에 모읍니다.
 * 본문에는 내용 카드만, 화면 사이 이동은 머리(⋮)와 하단 탭이 맡습니다. 항목이 없으면 그리지 않습니다.
 */
@Composable
fun AppBarMenu(items: List<AppBarMenuItem>) {
    if (items.isEmpty()) return
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Default.MoreVert, contentDescription = "메뉴") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    leadingIcon = { Icon(item.icon, contentDescription = null) },
                    onClick = { open = false; item.onClick() },
                )
            }
        }
    }
}
