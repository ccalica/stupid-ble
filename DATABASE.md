# BLE Metadata Database

## Overview
The app uses Room Database to store per-device characteristic metadata, allowing users to customize how characteristic data is interpreted and displayed.

## Database Structure

### Entity: CharacteristicMetadata

Stores metadata for individual characteristics on specific devices.

**Fields:**
- `id` (Long): Auto-generated primary key
- `deviceAddress` (String): MAC address of the BLE device
- `serviceUuid` (String): UUID of the GATT service
- `characteristicUuid` (String): UUID of the characteristic
- `name` (String?): User-defined friendly name
- `description` (String?): User-defined description
- `dataType` (CharacteristicDataType): How to interpret the value
  - `STRING`: UTF-8 string
  - `INTEGER`: Integer value
  - `FLOAT`: Floating point value
  - `HEX_RAW`: Hex bytes (default)
- `unit` (String?): Unit of measurement (e.g., "°C", "bpm", "mV")
- `minValue` (Double?): Expected minimum value
- `maxValue` (Double?): Expected maximum value
- `createdAt` (Long): Timestamp when created
- `updatedAt` (Long): Timestamp when last updated

## Usage

### Initialize
The database is initialized in `MainActivity` using `DatabaseProvider`:
```kotlin
val database = DatabaseProvider.getDatabase(this)
val metadataRepository = MetadataRepository(database.characteristicMetadataDao())
```

### Query Metadata
```kotlin
// Get all metadata for a device
metadataRepository.getMetadataForDevice(deviceAddress).collect { metadata ->
    // Use metadata
}

// Get metadata for specific characteristic
metadataRepository.getMetadata(deviceAddress, serviceUuid, charUuid).collect { metadata ->
    // Use metadata
}
```

### Save Metadata
```kotlin
metadataRepository.saveMetadata(
    deviceAddress = "AA:BB:CC:DD:EE:FF",
    serviceUuid = "0000180d-0000-1000-8000-00805f9b34fb",
    characteristicUuid = "00002a37-0000-1000-8000-00805f9b34fb",
    name = "Heart Rate",
    description = "Heart rate measurement in BPM",
    dataType = CharacteristicDataType.INTEGER,
    unit = "bpm",
    minValue = 40.0,
    maxValue = 220.0
)
```

### Delete Metadata
```kotlin
// Delete specific metadata
metadataRepository.deleteMetadata(metadata)

// Delete all metadata for a device
metadataRepository.deleteAllForDevice(deviceAddress)
```

## Next Steps
1. Create UI for editing characteristic metadata
2. Integrate metadata display in DeviceDetailScreen
3. Add data formatting based on dataType
4. Add validation for min/max values

