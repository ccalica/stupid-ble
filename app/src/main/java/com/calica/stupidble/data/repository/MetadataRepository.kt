package com.calica.stupidble.data.repository

import com.calica.stupidble.data.database.CharacteristicMetadata
import com.calica.stupidble.data.database.CharacteristicMetadataDao
import com.calica.stupidble.data.database.CharacteristicDataType
import com.calica.stupidble.data.database.Endianness
import kotlinx.coroutines.flow.Flow

class MetadataRepository(private val dao: CharacteristicMetadataDao) {

    fun getMetadataForDevice(deviceAddress: String): Flow<List<CharacteristicMetadata>> {
        return dao.getMetadataForDevice(deviceAddress)
    }

    fun getMetadata(
        deviceAddress: String,
        serviceUuid: String,
        characteristicUuid: String
    ): Flow<CharacteristicMetadata?> {
        return dao.getMetadata(deviceAddress, serviceUuid, characteristicUuid)
    }

    suspend fun getMetadataSync(
        deviceAddress: String,
        serviceUuid: String,
        characteristicUuid: String
    ): CharacteristicMetadata? {
        return dao.getMetadataSync(deviceAddress, serviceUuid, characteristicUuid)
    }

    suspend fun saveMetadata(
        deviceAddress: String,
        serviceUuid: String,
        characteristicUuid: String,
        name: String? = null,
        description: String? = null,
        dataType: CharacteristicDataType = CharacteristicDataType.HEX_RAW,
        endianness: Endianness = Endianness.LITTLE_ENDIAN,
        unit: String? = null,
        minValue: Double? = null,
        maxValue: Double? = null
    ): Long {
        val existing = dao.getMetadataSync(deviceAddress, serviceUuid, characteristicUuid)

        val metadata = if (existing != null) {
            existing.copy(
                name = name,
                description = description,
                dataType = dataType,
                endianness = endianness,
                unit = unit,
                minValue = minValue,
                maxValue = maxValue,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            CharacteristicMetadata(
                deviceAddress = deviceAddress,
                serviceUuid = serviceUuid,
                characteristicUuid = characteristicUuid,
                name = name,
                description = description,
                dataType = dataType,
                endianness = endianness,
                unit = unit,
                minValue = minValue,
                maxValue = maxValue
            )
        }

        return dao.insert(metadata)
    }

    suspend fun deleteMetadata(metadata: CharacteristicMetadata) {
        dao.delete(metadata)
    }

    suspend fun deleteAllForDevice(deviceAddress: String) {
        dao.deleteAllForDevice(deviceAddress)
    }

    fun getAllMetadata(): Flow<List<CharacteristicMetadata>> {
        return dao.getAllMetadata()
    }
}

