package com.calica.stupidble.ui.devicedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.calica.stupidble.data.BleConnectionManager
import com.calica.stupidble.data.ConnectionState
import com.calica.stupidble.data.database.CharacteristicMetadata
import com.calica.stupidble.data.model.BleServiceInfo
import com.calica.stupidble.data.model.BluetoothDeviceInfo
import com.calica.stupidble.data.repository.MetadataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceDetailViewModel(
    private val bleConnectionManager: BleConnectionManager,
    private val metadataRepository: MetadataRepository
) : ViewModel() {
    private val _device = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val device: StateFlow<BluetoothDeviceInfo?> = _device.asStateFlow()

    private val _metadata = MutableStateFlow<List<CharacteristicMetadata>>(emptyList())
    val metadata: StateFlow<List<CharacteristicMetadata>> = _metadata.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = bleConnectionManager.connectionState
    val services: StateFlow<List<BleServiceInfo>> = bleConnectionManager.services

    fun setDevice(device: BluetoothDeviceInfo) {
        _device.value = device
        bleConnectionManager.connect(device.address)
        loadMetadata(device.address)
    }

    private fun loadMetadata(deviceAddress: String) {
        viewModelScope.launch {
            metadataRepository.getMetadataForDevice(deviceAddress).collect { metadataList ->
                _metadata.value = metadataList
            }
        }
    }

    fun getMetadataForCharacteristic(serviceUuid: String, characteristicUuid: String): CharacteristicMetadata? {
        return _metadata.value.find {
            it.serviceUuid == serviceUuid && it.characteristicUuid == characteristicUuid
        }
    }

    fun disconnect() {
        bleConnectionManager.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        bleConnectionManager.disconnect()
    }
}

class DeviceDetailViewModelFactory(
    private val bleConnectionManager: BleConnectionManager,
    private val metadataRepository: MetadataRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeviceDetailViewModel(bleConnectionManager, metadataRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
