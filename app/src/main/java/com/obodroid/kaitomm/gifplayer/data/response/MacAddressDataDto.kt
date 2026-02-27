package com.obodroid.kaitomm.gifplayer.data.response

import com.google.gson.annotations.SerializedName

class MacAddressDataDto(
        @SerializedName("Projects")
        val project: String?,
        @SerializedName("Building")
        val building: String?,
        @SerializedName("devices")
        val devices: List<MacAddressDeviceDto>,
        @SerializedName("Room_No")
        val roomNumber: String?,
        @SerializedName("Room_Type")
        val roomType: String?
)