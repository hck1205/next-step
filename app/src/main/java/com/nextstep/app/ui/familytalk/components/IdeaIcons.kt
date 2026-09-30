package com.nextstep.app.ui.familytalk.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.nextstep.app.domain.familytalk.FamilyTalk

/** 고르기 카드의 그림: 해 보고 싶은 것 · 가족 즐거움 예시(FamilyTalk.WISH_IDEAS · TREAT_IDEAS)는 뜻에 맞는 그림, 나머지(자랑 등)는 별. */
internal fun ideaIcon(idea: String): ImageVector = ICONS[idea] ?: Icons.Filled.Star

private val ICONS: Map<String, ImageVector> = FamilyTalk.WISH_IDEAS.zip(
    listOf(Icons.Filled.AutoStories, Icons.Filled.DirectionsBike, Icons.Filled.Restaurant, Icons.Filled.MusicNote, Icons.Filled.Casino, Icons.Filled.Brush),
).toMap() + FamilyTalk.TREAT_IDEAS.zip(
    listOf(Icons.Filled.Casino, Icons.AutoMirrored.Filled.DirectionsWalk, Icons.Filled.Movie, Icons.Filled.Park, Icons.Filled.Restaurant, Icons.Filled.AutoStories),
).toMap()
