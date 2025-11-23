package com.calica.stupidble.ui.characteristicedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.calica.stupidble.data.database.CharacteristicDataType
import com.calica.stupidble.data.database.Endianness
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacteristicEditScreen(
    deviceAddress: String,
    serviceUuid: String,
    characteristicUuid: String,
    viewModel: CharacteristicEditViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dataType by viewModel.dataType.collectAsState()
    val endianness by viewModel.endianness.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(deviceAddress, serviceUuid, characteristicUuid) {
        viewModel.loadMetadata(deviceAddress, serviceUuid, characteristicUuid)
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Characteristic") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Characteristic Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Characteristic UUID",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = characteristicUuid,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Data Type Selection
            Text(
                text = "Data Type",
                style = MaterialTheme.typography.titleMedium
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CharacteristicDataType.values().forEach { type ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (type == dataType),
                                    onClick = { viewModel.setDataType(type) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (type == dataType),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = type.name.replace('_', ' '),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = getDataTypeDescription(type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Endianness Selection (only for INTEGER and FLOAT types)
            if (dataType == CharacteristicDataType.INTEGER || dataType == CharacteristicDataType.FLOAT) {
                Text(
                    text = "Byte Order (Endianness)",
                    style = MaterialTheme.typography.titleMedium
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Endianness.values().forEach { type ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (type == endianness),
                                        onClick = { viewModel.setEndianness(type) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (type == endianness),
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = getEndiannessDisplayName(type),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = getEndiannessDescription(type),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // Save Button
            Button(
                onClick = {
                    scope.launch {
                        viewModel.saveMetadata(deviceAddress, serviceUuid, characteristicUuid)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSaving) "Saving..." else "Save")
            }
        }
    }
}

private fun getDataTypeDescription(type: CharacteristicDataType): String {
    return when (type) {
        CharacteristicDataType.STRING -> "Interpret as UTF-8 text"
        CharacteristicDataType.INTEGER -> "Interpret as integer number"
        CharacteristicDataType.FLOAT -> "Interpret as floating point number"
        CharacteristicDataType.HEX_RAW -> "Show raw hex bytes (default)"
    }
}

private fun getEndiannessDisplayName(endianness: Endianness): String {
    return when (endianness) {
        Endianness.LITTLE_ENDIAN -> "Little Endian (Native)"
        Endianness.BIG_ENDIAN -> "Big Endian (Network)"
    }
}

private fun getEndiannessDescription(endianness: Endianness): String {
    return when (endianness) {
        Endianness.LITTLE_ENDIAN -> "Least significant byte first (most common for BLE)"
        Endianness.BIG_ENDIAN -> "Most significant byte first (network byte order)"
    }
}

