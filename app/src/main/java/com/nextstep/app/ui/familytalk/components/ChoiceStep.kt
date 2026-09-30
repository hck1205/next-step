package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.components.card.StoryHero
import com.nextstep.app.ui.components.input.NoteTextField
import com.nextstep.app.ui.components.input.OptionCardGrid

/**
 * 하나 고르기 걸음: 굵은 질문 → 크게 누르는 고르기 카드(다시 누르면 비움) → 공책에 적듯 직접 쓰기. 비워 두고 넘어가도 됩니다.
 * [wide] 면 고르기 카드를 한 줄에 하나(자랑처럼 긴 말).
 */
@Composable
internal fun ChoiceStep(title: String, hint: String, icon: ImageVector, ideas: List<String>, value: String, onChange: (String) -> Unit, wide: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StoryHero(title, hint, icon)
        if (ideas.isNotEmpty()) OptionCardGrid(ideas, ::ideaIcon, value, onChange, wide)
        NoteTextField(
            value = if (value in ideas) "" else value, onChange = onChange,
            label = if (ideas.isEmpty()) "직접 적어요" else "아니면 직접 적어요", placeholder = "여기에 적어요",
        )
    }
}
