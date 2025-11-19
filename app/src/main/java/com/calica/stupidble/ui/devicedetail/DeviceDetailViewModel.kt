package com.calica.stupidble.ui.devicedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.calica.stupidble.data.BleConnectionManager
import com.calica.stupidble.data.ConnectionState
import com.calica.stupidble.data.model.BleServiceInfo
import com.calica.stupidble.data.model.BluetoothDeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceDetailViewModel(
    private val bleConnectionManager: BleConnectionManager
) : ViewModel() {
    private val _device = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val device: StateFlow<BluetoothDeviceInfo?> = _device.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = bleConnectionManager.connectionState
    val services: StateFlow<List<BleServiceInfo>> = bleConnectionManager.services

    fun setDevice(device: BluetoothDeviceInfo) {
        _device.value = device
        bleConnectionManager.connect(device.address)
    }

    fun disconnect() {
        bleConnectionManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        bleConnectionManager.disconnect()
    }
}

class DeviceDetailViewModelFactory(private val bleConnectionManager: BleConnectionManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceDetailViewModel(bleConnectionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
