package com.calica.stupidble.data.model

data class BluetoothDeviceInfo(
    val address: String,
    val name: String?,
    val rssi: Int
) {
    val displayName: String
        get() = name ?: "Unknown Device"
}

