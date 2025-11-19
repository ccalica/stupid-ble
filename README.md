# Stupid BLE

A Bluetooth Low Energy (BLE) scanner application for Android built with Kotlin and Jetpack Compose.

## Features

- 📱 **BLE Device Scanning**: Discover nearby Bluetooth Low Energy devices
- 🔍 **Real-time Device List**: Devices appear in the list as they're discovered
- ⏱️ **Configurable Scan Timeout**: 60-second scan duration with manual stop option
- 🔐 **Runtime Permission Handling**: Automatic BLE permission requests
- 📄 **Device Details**: View basic information about discovered devices
- 💾 **Persistent Results**: Scan results survive configuration changes
- 🎨 **Modern UI**: Built with Jetpack Compose and Material Design 3

## Architecture

This project follows **MVVM (Model-View-ViewModel)** architecture pattern with clean separation of concerns:

```
app/
├── data/
│   ├── model/          # Data models
│   └── repository/     # Data layer & BLE operations
├── ui/
│   ├── scanner/        # Scanner screen & ViewModel
│   ├── devicedetail/   # Device detail screen & ViewModel
│   └── theme/          # Compose theme configuration
└── util/               # Helper utilities
```

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Navigation**: Jetpack Navigation Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Min SDK**: 31 (Android 12)
- **Bluetooth**: Android BLE APIs

## Key Components

### Scan Screen
- Start/Stop scan buttons
- Real-time device discovery
- Device list with RSSI values
- Connect button for each device

### Device Detail Screen
- Device name and address
- Connection status
- RSSI signal strength
- Expandable for future features

### BLE Repository
- Handles Bluetooth adapter operations
- Manages scan lifecycle
- Coordinates permission checks

## Configuration

The app includes a `BleConfig` object for build-time configuration:

```kotlin
object BleConfig {
    const val SCAN_TIMEOUT_MS = 60_000L  // 60 seconds
}
```

## Permissions

The app requires the following permissions:
- `BLUETOOTH_SCAN` - For discovering BLE devices
- `BLUETOOTH_CONNECT` - For connecting to devices
- `ACCESS_FINE_LOCATION` - Required for BLE scanning on Android

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- Android device or emulator running Android 12 (API 31) or higher
- Physical device recommended for actual BLE scanning

### Installation

1. Clone the repository:
```bash
git clone https://github.com/ccalica/stupid-ble.git
cd stupid-ble
```

2. Open the project in Android Studio

3. Build and run the app on your device

## Usage

1. Launch the app
2. Grant Bluetooth and location permissions when prompted
3. Tap **"Start Scan"** to begin discovering BLE devices
4. Devices will appear in the list as they're found
5. Tap **"Stop Scan"** to end scanning early
6. Tap **"Connect"** on any device to view its details

## Future Enhancements

This project is designed to be extensible. Planned features include:

- [ ] Dependency Injection (Hilt/Koin)
- [ ] Actual BLE connection and GATT service discovery
- [ ] Read/Write characteristic values
- [ ] Advanced device filtering
- [ ] Save favorite devices
- [ ] Export scan results
- [ ] More detailed device information

## Contributing

Contributions are welcome! Feel free to submit issues and pull requests.

## License

This project is open source and available under the [MIT License](LICENSE).

## Author

**Carlo J. Calica**
- Email: carlo@calica.com

## Acknowledgments

- Built with Android Jetpack libraries
- Inspired by the need for a simple, extensible BLE scanner

