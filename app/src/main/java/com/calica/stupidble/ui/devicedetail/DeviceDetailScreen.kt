package com.calica.stupidble.ui.devicedetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calica.stupidble.data.ConnectionState
import com.calica.stupidble.data.database.CharacteristicDataType
import com.calica.stupidble.data.database.CharacteristicMetadata
import com.calica.stupidble.data.database.Endianness
import com.calica.stupidble.data.model.BleCharacteristicInfo
import com.calica.stupidble.data.model.BleServiceInfo
import com.calica.stupidble.util.CharacteristicFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    viewModel: DeviceDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (serviceUuid: String, characteristicUuid: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val device by viewModel.device.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val services by viewModel.services.collectAsState()
    val metadata by viewModel.metadata.collectAsState()

    val handleBack = {
        viewModel.disconnect()
        onNavigateBack()
    }

    BackHandler {
        handleBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.displayName ?: "Device Details") },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status: ${connectionState.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    device?.let {
                        Text("Address: ${it.address}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (connectionState == ConnectionState.READY) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(services) { service ->
                        ServiceItem(
                            service = service,
                            viewModel = viewModel,
                            onEditCharacteristic = { characteristicUuid ->
                                onNavigateToEdit(service.uuid.toString(), characteristicUuid)
                            }
                        )
                    }
                }
            } else if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.DISCOVERING_SERVICES) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun ServiceItem(
    service: BleServiceInfo,
    viewModel: DeviceDetailViewModel,
    onEditCharacteristic: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Service",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = service.uuid.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    service.characteristics.forEach { characteristic ->
                        val metadata = viewModel.getMetadataForCharacteristic(
                            service.uuid.toString(),
                            characteristic.uuid.toString()
                        )
                        CharacteristicItem(
                            characteristic = characteristic,
                            metadata = metadata,
                            onEdit = { onEditCharacteristic(characteristic.uuid.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CharacteristicItem(
    characteristic: BleCharacteristicInfo,
    metadata: CharacteristicMetadata?,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Characteristic",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    // Data Type Badge
                    metadata?.let {
                        AssistChip(
                            onClick = { },
                            label = {
                                Text(
                                    text = it.dataType.name.replace('_', ' '),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
                Text(
                    text = characteristic.uuid.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace
                )
            }
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (characteristic.properties.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                characteristic.properties.forEach { prop ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(prop, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
        
        characteristic.valueSize?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Value Size: $it bytes",
                style = MaterialTheme.typography.bodySmall
            )
        }

        characteristic.value?.let { value ->
            Spacer(modifier = Modifier.height(4.dp))
            val dataType = metadata?.dataType ?: CharacteristicDataType.HEX_RAW
            val endianness = metadata?.endianness ?: Endianness.LITTLE_ENDIAN
            val formattedValue = CharacteristicFormatter.formatValue(value, dataType, endianness)
            Text(
                text = "Value: $formattedValue",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 6
            )
        }
    }
}