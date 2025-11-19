package com.calica.stupidble.ui.scanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.calica.stupidble.data.model.BluetoothDeviceInfo

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    onDeviceClick: (BluetoothDeviceInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "BLE Scanner",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (uiState.isScanning) {
                    viewModel.stopScan()
                } else {
                    viewModel.startScan()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (uiState.isScanning) "Stop Scan" else "Start Scan")
        }

        if (uiState.isScanning) {
            Text(
                text = "Scanning for devices...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(uiState.devices, key = { it.address }) { device ->
                DeviceItem(
                    device = device,
                    onConnectClick = { onDeviceClick(device) }
                )
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: BluetoothDeviceInfo,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "RSSI: ${device.rssi} dBm",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(onClick = onConnectClick) {
                Text("Connect")
            }
        }
    }
}

