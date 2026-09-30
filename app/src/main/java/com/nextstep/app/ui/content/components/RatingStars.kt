package com.nextstep.app.ui.content.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun RatingStars(value: Float, enabled: Boolean, onRate: (Int) -> Unit) {
    Row {
        (1..5).forEach { i ->
            IconButton(onClick = { onRate(i) }, enabled = enabled, modifier = Modifier.width(24.dp).height(24.dp)) {
                Icon(if (value >= i - 0.5f) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "$i 점", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.width(18.dp))
            }
        }
    }
}
