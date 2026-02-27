package com.obodroid.kaitomm.gifplayer.common.setting

enum class WifiStatus(val level: Int) {
    LEVEL_1(1), LEVEL_2(2), LEVEL_3(3), OFF(0), UNKNOWN(-1);

    companion object {
        fun valueOf(level: Int): WifiStatus {
            for (value in values()) {
                if (value.level == level) return value
            }
            return UNKNOWN
        }
    }
}