package com.tiredvpn.android.vpn

/** Shared by native argv and VpnService so imported settings cannot diverge. */
internal object TunnelMtu {
    const val DEFAULT = 1280
    const val MAX = 9000

    /** Zero is automatic. Invalid imported values also use automatic mode. */
    fun overrideOrAuto(value: Int): Int = value.takeIf { it in DEFAULT..MAX } ?: 0

    /** The Android interface must never exceed the native core's current limit. */
    fun effective(requested: Int, core: Int): Int {
        val coreMtu = overrideOrAuto(core).takeIf { it != 0 } ?: DEFAULT
        val cap = overrideOrAuto(requested).takeIf { it != 0 } ?: coreMtu
        return minOf(cap, coreMtu)
    }
}
