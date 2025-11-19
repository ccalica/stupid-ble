package com.calica.stupidble.data.model

import java.util.UUID

data class BleServiceInfo(
    val uuid: UUID,
    val characteristics: List<BleCharacteristicInfo>
)

data class BleCharacteristicInfo(
    val uuid: UUID,
    val properties: List<String>,
    val value: ByteArray? = null,
    val valueSize: Int? = null,
    val formattedValue: String? = null
) {
    // Overriding equals and hashCode is important for data classes with Array properties
    // to ensure comparisons are based on content, not identity.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleCharacteristicInfo

        if (uuid != other.uuid) return false
        if (properties != other.properties) return false
        if (value != null) {
            if (other.value == null) return false
            if (!value.contentEquals(other.value)) return false
        } else if (other.value != null) return false
        if (valueSize != other.valueSize) return false
        if (formattedValue != other.formattedValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + properties.hashCode()
        result = 31 * result + (value?.contentHashCode() ?: 0)
        result = 31 * result + (valueSize ?: 0)
        result = 31 * result + (formattedValue?.hashCode() ?: 0)
        return result
    }
}
