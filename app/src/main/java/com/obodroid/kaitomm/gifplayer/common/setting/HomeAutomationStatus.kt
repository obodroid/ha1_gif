package com.obodroid.kaitomm.gifplayer.common.setting

enum class HomeAutomationStatus(val level: Int) {
    ON(1), OFF(0), UNKNOWN(-1);

    companion object {
        fun valueOf(level: Int): HomeAutomationStatus {
            for (value in values()) {
                if (value.level == level) return value
            }
            return UNKNOWN
        }
    }
}