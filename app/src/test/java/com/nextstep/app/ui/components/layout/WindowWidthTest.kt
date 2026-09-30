package com.nextstep.app.ui.components.layout

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowWidthTest {
    @Test
    fun phonesAreCompactFoldablesMediumTabletsExpanded() {
        assertEquals(WindowWidth.COMPACT, WindowWidth.of(360))
        assertEquals(WindowWidth.COMPACT, WindowWidth.of(599))
        assertEquals(WindowWidth.MEDIUM, WindowWidth.of(600)) // 폴더블 펼침·태블릿 세로
        assertEquals(WindowWidth.MEDIUM, WindowWidth.of(839))
        assertEquals(WindowWidth.EXPANDED, WindowWidth.of(840)) // 태블릿 가로
    }

    @Test
    fun onlyPhonesUseTheBottomBar() {
        assertFalse(WindowWidth.COMPACT.usesRail)
        assertTrue(WindowWidth.MEDIUM.usesRail)
        assertTrue(WindowWidth.EXPANDED.usesRail)
    }
}
