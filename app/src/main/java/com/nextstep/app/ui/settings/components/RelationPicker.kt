package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.GuardianRelation

/** 보호자 관계 고르기(엄마·아빠·할머니·할아버지·보호자). 같은 칩을 다시 누르면 해제. */
@Composable
fun RelationPicker(selected: GuardianRelation?, onSelect: (GuardianRelation?) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(GuardianRelation.entries, key = { it.name }) { r ->
            FilterChip(selected = r == selected, onClick = { onSelect(if (r == selected) null else r) }, label = { Text(r.label) })
        }
    }
}
