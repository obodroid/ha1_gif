package com.obodroid.kaitomm.gifplayer.common.setting

enum class BatteryStatus(val level: Int) {
    EMPTY(0),
    LEVEL_1(1),
    LEVEL_2(2),
    LEVEL_3(3),
    LEVEL_4(4),
    LEVEL_5(5),
    LEVEL_6(6),
    LEVEL_7(7),
    FULL(8),
    CHARGING(-1),
    UNKNOWN(-1);

    companion object {
        fun valueOf(level: Int): BatteryStatus {
            for (value in values()) {
                if (value.level == level) return value
            }
            return UNKNOWN
        }
    }
}