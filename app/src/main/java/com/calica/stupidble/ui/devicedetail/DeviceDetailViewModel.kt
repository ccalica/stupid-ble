package com.calica.stupidble.ui.devicedetail

import androidx.lifecycle.ViewModel
import com.calica.stupidble.data.model.BluetoothDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceDetailViewModel : ViewModel() {
    private val _device = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val device: StateFlow<BluetoothDeviceInfo?> = _device.asStateFlow()

    fun setDevice(device: BluetoothDeviceInfo) {
        _device.value = device
    }
}

