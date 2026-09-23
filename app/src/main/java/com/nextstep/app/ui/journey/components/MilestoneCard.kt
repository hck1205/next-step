package com.nextstep.app.ui.journey.components

import androidx.compose.runtime.Composable
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.ui.journey.JourneyEvent
import java.time.LocalDate

@Composable
internal fun MilestoneCard(
    item: JourneyItem, today: LocalDate, expandedKey: String?, setExpanded: (String?) -> Unit, onEvent: (JourneyEvent) -> Unit,
    onNote: (JourneyItem) -> Unit, onDate: (JourneyItem) -> Unit,
) {
    val key = item.templateId ?: item.entityId ?: item.title
    MilestoneRow(
        item = item, today = today, expanded = expandedKey == key,
        onToggleExpand = { setExpanded(if (expandedKey == key) null else key) },
        onSetStatus = { onEvent(JourneyEvent.SetStatus(item, it)) },
        onEditNote = { onNote(item) },
        onEditDate = { onDate(item) },
        onDelete = if (item.isCustom) ({ onEvent(JourneyEvent.DeleteCustom(item)) }) else null,
    )
}
