package com.nextstep.app.ui.settings.components

/** 가족 탭에서 한 번에 하나만 열리는 창. */
internal sealed interface SettingsDialog {
    data object SignOut : SettingsDialog
    data class RemoveMember(val id: String) : SettingsDialog
    data object Subjects : SettingsDialog
    data object AddChild : SettingsDialog
    data object LinkChild : SettingsDialog
    data object EditYear : SettingsDialog
}
