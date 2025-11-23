package com.calica.stupidble.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CharacteristicDataType {
    STRING,
    INTEGER,
    FLOAT,
    HEX_RAW
}

enum class Endianness {
    LITTLE_ENDIAN,  // Native (most common for BLE)
    BIG_ENDIAN      // Network byte order
}

@Entity(tableName = "characteristic_metadata")
data class CharacteristicMetadata(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Device identifier
    val deviceAddress: String,

    // Service and characteristic identifiers
    val serviceUuid: String,
    val characteristicUuid: String,

    // Metadata fields
    val name: String? = null,
    val description: String? = null,
    val dataType: CharacteristicDataType = CharacteristicDataType.HEX_RAW,
    val endianness: Endianness = Endianness.LITTLE_ENDIAN,
    val unit: String? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null,

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

