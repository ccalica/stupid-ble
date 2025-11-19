package com.calica.stupidble.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calica.stupidble.BleConfig
import com.calica.stupidble.data.model.BluetoothDeviceInfo
import com.calica.stupidble.data.repository.BleRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ScannerUiState(
    val devices: List<BluetoothDeviceInfo> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null
)

class ScannerViewModel(private val bleRepository: BleRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null
    private var timeoutJob: Job? = null

    fun startScan() {
        if (!bleRepository.isBluetoothEnabled()) {
            _uiState.value = _uiState.value.copy(error = "Bluetooth is not enabled")
            return
        }

        stopScan()

        _uiState.value = _uiState.value.copy(isScanning = true, error = null)

        scanJob = viewModelScope.launch {
            bleRepository.scanForDevices()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isScanning = false,
                        error = e.message ?: "Scan failed"
                    )
                }
                .collect { device ->
                    val currentDevices = _uiState.value.devices
                    val updatedDevices = if (BleConfig.UPDATE_DUPLICATE_DEVICES) {
                        val existingIndex = currentDevices.indexOfFirst { it.address == device.address }
                        if (existingIndex >= 0) {
                            currentDevices.toMutableList().apply { this[existingIndex] = device }
                        } else {
                            currentDevices + device
                        }
                    } else {
                        currentDevices + device
                    }
                    _uiState.value = _uiState.value.copy(devices = updatedDevices)
                }
        }

        timeoutJob = viewModelScope.launch {
            delay(BleConfig.SCAN_TIMEOUT_MS)
            stopScan()
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        timeoutJob?.cancel()
        scanJob = null
        timeoutJob = null
        _uiState.value = _uiState.value.copy(isScanning = false)
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }
}

