package com.nextstep.app.ui.todo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TodoFilterTest {
    @Test
    fun everyFilterHasItsOwnEmptyLine() {
        assertEquals("오늘까지 할 일이 없어요", TodoFilter.NOW.empty)
        assertEquals("밀린 할 일이 없어요", TodoFilter.OVERDUE.empty)
        assertTrue(TodoFilter.entries.all { it.empty.isNotBlank() })
    }
}
