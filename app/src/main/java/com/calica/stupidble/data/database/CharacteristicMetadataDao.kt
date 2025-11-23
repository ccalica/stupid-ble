package com.calica.stupidble.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacteristicMetadataDao {

    @Query("SELECT * FROM characteristic_metadata WHERE deviceAddress = :deviceAddress")
    fun getMetadataForDevice(deviceAddress: String): Flow<List<CharacteristicMetadata>>

    @Query("""
        SELECT * FROM characteristic_metadata 
        WHERE deviceAddress = :deviceAddress 
        AND serviceUuid = :serviceUuid 
        AND characteristicUuid = :characteristicUuid
        LIMIT 1
    """)
    fun getMetadata(
        deviceAddress: String,
        serviceUuid: String,
        characteristicUuid: String
    ): Flow<CharacteristicMetadata?>

    @Query("""
        SELECT * FROM characteristic_metadata 
        WHERE deviceAddress = :deviceAddress 
        AND serviceUuid = :serviceUuid 
        AND characteristicUuid = :characteristicUuid
        LIMIT 1
    """)
    suspend fun getMetadataSync(
        deviceAddress: String,
        serviceUuid: String,
        characteristicUuid: String
    ): CharacteristicMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: CharacteristicMetadata): Long

    @Update
    suspend fun update(metadata: CharacteristicMetadata)

    @Delete
    suspend fun delete(metadata: CharacteristicMetadata)

    @Query("DELETE FROM characteristic_metadata WHERE deviceAddress = :deviceAddress")
    suspend fun deleteAllForDevice(deviceAddress: String)

    @Query("SELECT * FROM characteristic_metadata ORDER BY updatedAt DESC")
    fun getAllMetadata(): Flow<List<CharacteristicMetadata>>
}

