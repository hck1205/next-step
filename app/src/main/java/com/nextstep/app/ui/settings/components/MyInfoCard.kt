package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.model.GuardianRelation
import com.nextstep.app.data.prefs.UserProfile
import com.nextstep.app.ui.components.card.AppCard

/** 내 역할 · 이름 · 학생. 학부모([showsRelation])는 아이와의 관계(엄마·아빠…)도 고릅니다. */
@Composable
internal fun MyInfoCard(profile: UserProfile?, me: MemberEntity?, showsRelation: Boolean, onRelation: (me: MemberEntity, label: String) -> Unit) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            InfoRow("역할", me?.roleLabel ?: profile?.role?.label ?: "-")
            InfoRow("이름", profile?.displayName ?: "-")
            InfoRow("학생", profile?.studentName ?: "-")
            if (showsRelation && me != null) {
                RelationPicker(GuardianRelation.fromLabel(me.title), onSelect = { onRelation(me, it?.label.orEmpty()) })
            }
        }
    }
}
