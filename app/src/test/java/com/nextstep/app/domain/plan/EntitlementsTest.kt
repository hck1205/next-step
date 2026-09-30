package com.nextstep.app.domain.plan

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.access.Capabilities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntitlementsTest {
    @Test
    fun beforeEnforcementEverythingIsOpenButAdsStay() {
        assertFalse(PlanPolicy.ENFORCED)
        Feature.entries.forEach { assertTrue(Entitlements.OPEN.has(it)) }
        Limit.entries.forEach { assertEquals(Int.MAX_VALUE, Entitlements.OPEN.limit(it)) }
        assertFalse(Entitlements.OPEN.adFree) // 광고 빼기는 강제할 때 가족 플러스만
        assertTrue(Capabilities(Role.PARENT, false).has(Feature.PERIOD_REPORT))
    }

    @Test
    fun whenEnforcedPlansUnlockTheirOwnFeaturesAndLimits() {
        val family = Entitlements(setOf(Plan.FAMILY_PLUS), enforced = true)
        assertTrue(family.has(Feature.GROWTH_ALBUM)); assertFalse(family.has(Feature.BULK_ASSIGN)); assertTrue(family.adFree)
        assertEquals(Int.MAX_VALUE, family.limit(Limit.CHILDREN)); assertEquals(2, family.limit(Limit.STUDENTS))
        val free = Entitlements(enforced = true)
        assertFalse(free.has(Feature.PERIOD_REPORT)); assertEquals(1, free.limit(Limit.CHILDREN)); assertFalse(free.adFree)
        assertFalse(Capabilities(Role.MENTOR, true, free).has(Feature.LESSONS))
    }
}
