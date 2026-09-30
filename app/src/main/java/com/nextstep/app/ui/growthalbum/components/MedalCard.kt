package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.album.AlbumGoal
import com.nextstep.app.domain.time.DateUtils

/** "해낸 것" 한 장: 메달 · 목표 · 이룬 날. */
@Composable
internal fun MedalCard(goal: AlbumGoal) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.fillMaxWidth().background(cs.tertiaryContainer, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(44.dp).background(cs.surface, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = cs.tertiary, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(goal.title, style = MaterialTheme.typography.titleMedium, color = cs.onTertiaryContainer)
            Text("${DateUtils.formatShortDate(goal.doneOn)} 이뤘어요", style = MaterialTheme.typography.bodySmall, color = cs.onTertiaryContainer)
        }
    }
}
