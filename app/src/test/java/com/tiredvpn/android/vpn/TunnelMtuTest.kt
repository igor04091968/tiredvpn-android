package com.tiredvpn.android.vpn

import org.junit.Assert.assertEquals
import org.junit.Test

class TunnelMtuTest {
    @Test fun `invalid imports and automatic mode use safe defaults`() {
        for (value in listOf(Int.MIN_VALUE, -1, 0, 1279, 9001, Int.MAX_VALUE)) {
            assertEquals(0, TunnelMtu.overrideOrAuto(value))
            assertEquals(1280, TunnelMtu.effective(value, 0))
        }
    }

    @Test fun `custom MTU is a cap on the core limit`() {
        assertEquals(1280, TunnelMtu.effective(1500, 1280))
        assertEquals(1400, TunnelMtu.effective(1400, 1500))
        assertEquals(1500, TunnelMtu.effective(0, 1500))
        assertEquals(1280, TunnelMtu.effective(0, 1279))
        assertEquals(9000, TunnelMtu.effective(9000, 9000))
    }

    @Test fun `valid IPv6 minimum and jumbo override survive`() {
        for (value in listOf(1280, 1400, 1500, 9000)) {
            assertEquals(value, TunnelMtu.overrideOrAuto(value))
            assertEquals(value, TunnelMtu.effective(value, value))
        }
    }
}
