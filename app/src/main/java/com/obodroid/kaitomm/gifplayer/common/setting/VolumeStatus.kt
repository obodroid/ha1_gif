package com.obodroid.kaitomm.gifplayer.common.setting

enum class VolumeStatus(val level: Int) {
    LOW(1), HIGH(2), UNKNOWN(-1);

    companion object {
        fun valueOf(level: Int): VolumeStatus {
            for (value in values()) {
                if (value.level == level) return value
            }
            return UNKNOWN
        }
    }
}