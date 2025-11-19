package com.calica.stupidble.data

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import com.calica.stupidble.data.model.BleCharacteristicInfo
import com.calica.stupidble.data.model.BleServiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    DISCOVERING_SERVICES,
    READY
}

class BleConnectionManager(context: Context) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val context = context.applicationContext

    private var bluetoothGatt: BluetoothGatt? = null
    private val readQueue = ArrayDeque<BluetoothGattCharacteristic>()
    private var isReadingCharacteristics = false

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _services = MutableStateFlow<List<BleServiceInfo>>(emptyList())
    val services: StateFlow<List<BleServiceInfo>> = _services.asStateFlow()

    companion object {
        val GAP_SERVICE_UUID: UUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
        val GATT_SERVICE_UUID: UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connectionState.value = ConnectionState.CONNECTED
                _connectionState.value = ConnectionState.DISCOVERING_SERVICES
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connectionState.value = ConnectionState.DISCONNECTED
                gatt.close()
                bluetoothGatt = null
                _services.value = emptyList()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readQueue.clear()
                val serviceList = gatt.services
                    .filter { it.uuid != GAP_SERVICE_UUID && it.uuid != GATT_SERVICE_UUID }
                    .map { service ->
                        service.characteristics.forEach { char ->
                            if ((char.properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                                readQueue.add(char)
                            }
                        }
                        BleServiceInfo(
                            uuid = service.uuid,
                            characteristics = service.characteristics.map { char ->
                                BleCharacteristicInfo(
                                    uuid = char.uuid,
                                    properties = getPropertiesList(char.properties)
                                )
                            }
                        )
                    }
                _services.value = serviceList

                if (readQueue.isNotEmpty()) {
                    isReadingCharacteristics = true
                    processReadQueue()
                }
                _connectionState.value = ConnectionState.READY
            } else {
                Log.e("BleConnectionManager", "onServicesDiscovered received: $status")
            }
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                handleCharacteristicRead(gatt, characteristic, characteristic.value, status)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            handleCharacteristicRead(gatt, characteristic, value, status)
        }
    }
    
    @SuppressLint("MissingPermission")
    private fun processReadQueue() {
        if (readQueue.isEmpty()) {
            isReadingCharacteristics = false
            return
        }
        val characteristic = readQueue.removeFirst()
        if (bluetoothGatt?.readCharacteristic(characteristic) == false) {
            Log.e("BleConnectionManager", "Failed to initiate read for ${characteristic.uuid}")
            processReadQueue() // Try next one
        }
    }

    private fun handleCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray?,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            _services.value = _services.value.map { service ->
                service.copy(
                    characteristics = service.characteristics.map { charInfo ->
                        if (charInfo.uuid == characteristic.uuid) {
                            charInfo.copy(
                                value = value,
                                valueSize = value?.size ?: 0,
                                formattedValue = formatBytes(value)
                            )
                        } else {
                            charInfo
                        }
                    }
                )
            }
        } else {
            Log.e("BleConnectionManager", "onCharacteristicRead failed for ${characteristic.uuid} with status $status")
        }
        processReadQueue()
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String) {
        if (adapter == null || !adapter.isEnabled) {
            Log.e("BleConnectionManager", "BluetoothAdapter not initialized or disabled")
            return
        }
        val device = adapter.getRemoteDevice(address)
        _connectionState.value = ConnectionState.CONNECTING
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        readQueue.clear()
        isReadingCharacteristics = false
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = ConnectionState.DISCONNECTED
        _services.value = emptyList()
    }

    private fun getPropertiesList(properties: Int): List<String> {
        val props = mutableListOf<String>()
        if ((properties and BluetoothGattCharacteristic.PROPERTY_READ) > 0) props.add("READ")
        if ((properties and BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) props.add("WRITE")
        if ((properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) props.add("WRITE_NO_RESP")
        if ((properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) props.add("NOTIFY")
        if ((properties and BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) props.add("INDICATE")
        return props
    }

    private fun formatBytes(bytes: ByteArray?): String {
        if (bytes == null || bytes.isEmpty()) return "N/A"
        val hex = bytes.joinToString(separator = " ") { "%02X".format(it) }
        val ascii = bytes.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString(separator = "")
        return "$hex\n\"$ascii\""
    }
}