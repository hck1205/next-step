package com.nextstep.app.domain.cheer

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CheersTest {
    private val today = LocalDate.of(2029, 3, 7)
    private fun doneOn(title: String, day: LocalDate, hour: Int = 18) = Fixtures.task(title, day, done = true).copy(doneAt = DateUtils.toMillis(day, LocalTime.of(hour, 0)))
    private fun cheer(taskId: String, kind: CheerKind, from: String = "mom", at: Long = 1L, seen: Long? = null) =
        CheerEntity(id = "c-$taskId-$from", familyId = Fixtures.FAMILY, taskId = taskId, taskTitle = "t", kind = kind.name, fromId = from, fromName = "엄마", createdAt = at, seenAt = seen)

    @Test
    fun targetsAreRecentlyDoneTasksWithMyCheer() {
        val tasks = listOf(
            doneOn("오늘 분수", today), doneOn("어제 일기", today.minusDays(1)), doneOn("그저께 독서", today.minusDays(2)),
            Fixtures.task("안 끝남", today), doneOn("지운 것", today).copy(deleted = true),
        )
        val cheers = listOf(cheer(tasks[0].id, CheerKind.HEART), cheer(tasks[1].id, CheerKind.STAR, from = "dad"))
        val t = Cheers.targets(tasks, cheers, "mom", today)
        assertEquals(listOf("오늘 분수", "어제 일기"), t.map { it.task.title }) // 최근 끝낸 것부터, 이틀 안
        assertEquals(CheerKind.HEART, t[0].given); assertNull(t[1].given) // 아빠의 응원은 내 것이 아님
    }

    @Test
    fun unseenLineAndToggle() {
        val list = listOf(cheer("a", CheerKind.CLAP, at = 1L), cheer("b", CheerKind.STAR, at = 5L), cheer("c", CheerKind.HEART, seen = 9L), cheer("d", CheerKind.CLAP).copy(deleted = true))
        assertEquals(listOf("c-b-mom", "c-a-mom"), Cheers.unseen(list).map { it.id })
        assertEquals("엄마가 't'에 👏 짝짝짝", Cheers.line(list[0]))
        assertEquals("할머니가 't'에 ⭐ 최고야", Cheers.line(list[1].copy(fromName = "할머니")))
        assertEquals("가족이 't'에 👏 짝짝짝", Cheers.line(list[0].copy(fromName = "")))
        assertNull(Cheers.toggle(CheerKind.CLAP, CheerKind.CLAP)); assertEquals(CheerKind.STAR, Cheers.toggle(CheerKind.CLAP, CheerKind.STAR))
        assertEquals(CheerKind.CLAP, CheerKind.from("??"))
    }
}
