package com.calica.stupidble.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter
    fun fromDataType(value: CharacteristicDataType): String {
        return value.name
    }

    @TypeConverter
    fun toDataType(value: String): CharacteristicDataType {
        return CharacteristicDataType.valueOf(value)
    }

    @TypeConverter
    fun fromEndianness(value: Endianness): String {
        return value.name
    }

    @TypeConverter
    fun toEndianness(value: String): Endianness {
        return Endianness.valueOf(value)
    }
}

@Database(
    entities = [CharacteristicMetadata::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BleDatabase : RoomDatabase() {
    abstract fun characteristicMetadataDao(): CharacteristicMetadataDao
}

