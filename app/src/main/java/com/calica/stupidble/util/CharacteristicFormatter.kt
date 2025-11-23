package com.calica.stupidble.util

import com.calica.stupidble.data.database.CharacteristicDataType
import com.calica.stupidble.data.database.Endianness
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale

object CharacteristicFormatter {

    fun formatValue(
        bytes: ByteArray?,
        dataType: CharacteristicDataType,
        endianness: Endianness = Endianness.LITTLE_ENDIAN
    ): String {
        if (bytes == null || bytes.isEmpty()) return "N/A"

        return when (dataType) {
            CharacteristicDataType.STRING -> {
                // String only - no hex
                formatAsString(bytes)
            }
            CharacteristicDataType.INTEGER -> {
                // Integer format
                formatAsInteger(bytes, endianness)
            }
            CharacteristicDataType.FLOAT -> {
                // Float format
                formatAsFloat(bytes, endianness)
            }
            CharacteristicDataType.HEX_RAW -> {
                // Hex + ASCII (default)
                formatAsHexAndAscii(bytes)
            }
        }
    }

    private fun formatAsString(bytes: ByteArray): String {
        return try {
            bytes.toString(Charsets.UTF_8)
        } catch (e: Exception) {
            "Invalid UTF-8"
        }
    }

    private fun formatAsInteger(bytes: ByteArray, endianness: Endianness): String {
        return try {
            val byteOrder = if (endianness == Endianness.LITTLE_ENDIAN) {
                ByteOrder.LITTLE_ENDIAN
            } else {
                ByteOrder.BIG_ENDIAN
            }

            when (bytes.size) {
                1 -> {
                    val signed = bytes[0].toInt()
                    val unsigned = bytes[0].toInt() and 0xFF
                    "Signed: $signed\nUnsigned: $unsigned"
                }
                2 -> {
                    val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
                    val signed = buffer.getShort().toInt()
                    buffer.rewind()
                    val unsigned = buffer.getShort().toInt() and 0xFFFF
                    "Signed: $signed\nUnsigned: $unsigned"
                }
                4 -> {
                    val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
                    val signed = buffer.getInt()
                    buffer.rewind()
                    val unsigned = buffer.getInt().toLong() and 0xFFFFFFFFL
                    "Signed: $signed\nUnsigned: $unsigned"
                }
                else -> {
                    "Size ${bytes.size} bytes - use Int8/16/32"
                }
            }
        } catch (e: Exception) {
            "Parse error: ${e.message}"
        }
    }

    private fun formatAsFloat(bytes: ByteArray, endianness: Endianness): String {
        return try {
            val byteOrder = if (endianness == Endianness.LITTLE_ENDIAN) {
                ByteOrder.LITTLE_ENDIAN
            } else {
                ByteOrder.BIG_ENDIAN
            }

            when (bytes.size) {
                4 -> {
                    val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
                    val value = buffer.getFloat()
                    String.format(Locale.US, "%.6f", value)
                }
                8 -> {
                    val buffer = ByteBuffer.wrap(bytes).order(byteOrder)
                    val value = buffer.getDouble()
                    String.format(Locale.US, "%.6f", value)
                }
                else -> {
                    "Size ${bytes.size} bytes - use Float(4) or Double(8)"
                }
            }
        } catch (e: Exception) {
            "Parse error: ${e.message}"
        }
    }

    private fun formatAsHexAndAscii(bytes: ByteArray): String {
        val hex = bytes.joinToString(separator = " ") { "%02X".format(it) }
        val ascii = bytes.map { if (it in 32..126) it.toInt().toChar() else '.' }.joinToString(separator = "")
        return "$hex\n\"$ascii\""
    }
}

