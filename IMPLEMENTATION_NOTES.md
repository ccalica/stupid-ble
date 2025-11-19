# BLE Device Detail Implementation Notes

This document outlines the design decisions and reasoning behind the implementation of the BLE Device Detail feature in the StupidBLE application.

## 1. Architecture: `BleConnectionManager`

To separate concerns and keep the codebase clean, a dedicated `BleConnectionManager` class was created in `com.calica.stupidble.data`.

*   **Reasoning**: The `BleRepository` was primarily focused on scanning. Handling active connections, service discovery, and characteristic reading involves different APIs (`BluetoothGatt` vs `BluetoothLeScanner`) and state management. A separate manager prevents the repository from becoming a "God object" and allows for better testability and separation of scanning vs. connection logic.
*   **Dependency Injection**: The manager is instantiated in `MainActivity` and passed to the `DeviceDetailViewModel` via a factory. This ensures that the ViewModel has a valid instance and allows for potential future scope management (e.g., if we wanted the connection to survive across multiple screens, we could scope the manager differently).

## 2. State Management

The application uses `StateFlow` for reactive UI updates.

*   **Connection State**: A `ConnectionState` enum (`DISCONNECTED`, `CONNECTING`, `CONNECTED`, `DISCOVERING_SERVICES`, `READY`) tracks the precise status of the BLE connection. This allows the UI to show appropriate loading indicators or error states.
*   **Service Data**: The discovered GATT services and characteristics are mapped to domain models (`BleServiceInfo`, `BleCharacteristicInfo`) and exposed as a `StateFlow<List<BleServiceInfo>>`. This decouples the UI from the Android framework's `BluetoothGattService` objects.

## 3. Lifecycle and Disconnection

One of the key requirements was to disconnect when navigating away from the detail screen.

*   **Implementation**: 
    *   A `disconnect()` method was added to `DeviceDetailViewModel` which delegates to the manager.
    *   In `DeviceDetailScreen`, a `BackHandler` intercepts the system back gesture to ensure `disconnect()` is called before popping the back stack.
    *   The navigation logic in `MainActivity` also ensures that explicit "Back" button clicks trigger the disconnect.
    *   `DeviceDetailViewModel.onCleared()` also calls disconnect as a safety net (e.g., if the ViewModel scope is cleared).
*   **Reasoning**: BLE connections are resource-intensive. keeping them open unnecessarily drains battery and can prevent other apps (or other parts of this app) from connecting. Explicit disconnection ensures the app behaves predictably.

## 4. UI Design

The Device Detail screen uses Jetpack Compose to render the hierarchy.

*   **Structure**: A `LazyColumn` displays the list of services.
*   **Interactivity**: Each service item is an expandable Card. Clicking a service reveals its characteristics. This prevents overwhelming the user with too much information at once, given that some devices have many services and characteristics.
*   **Feedback**: A status header clearly shows the current connection state (e.g., "Connecting...", "Ready").

## 5. Permissions

The `AndroidManifest.xml` was updated to support a wider range of Android versions.

*   **Additions**: `ACCESS_FINE_LOCATION` and `BLUETOOTH_ADMIN` were added with `maxSdkVersion="30"`.
*   **Reasoning**: While Android 12+ (API 31) uses `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`, older versions require Location permissions to perform BLE scans. Adding these ensures the app works on Android 10 and 11 devices as well.
