package com.calica.stupidble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.calica.stupidble.data.BleConnectionManager
import com.calica.stupidble.data.database.DatabaseProvider
import com.calica.stupidble.data.repository.BleRepository
import com.calica.stupidble.data.repository.MetadataRepository
import com.calica.stupidble.ui.characteristicedit.CharacteristicEditScreen
import com.calica.stupidble.ui.characteristicedit.CharacteristicEditViewModel
import com.calica.stupidble.ui.characteristicedit.CharacteristicEditViewModelFactory
import com.calica.stupidble.ui.devicedetail.DeviceDetailScreen
import com.calica.stupidble.ui.devicedetail.DeviceDetailViewModel
import com.calica.stupidble.ui.devicedetail.DeviceDetailViewModelFactory
import com.calica.stupidble.ui.scanner.ScannerScreen
import com.calica.stupidble.ui.scanner.ScannerViewModel
import com.calica.stupidble.ui.scanner.ScannerViewModelFactory
import com.calica.stupidble.ui.theme.StupidbleTheme
import com.calica.stupidble.util.PermissionHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private lateinit var bleRepository: BleRepository
    private lateinit var bleConnectionManager: BleConnectionManager
    private lateinit var metadataRepository: MetadataRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleRepository = BleRepository(this)
        bleConnectionManager = BleConnectionManager(this)

        val database = DatabaseProvider.getDatabase(this)
        metadataRepository = MetadataRepository(database.characteristicMetadataDao())

        enableEdgeToEdge()
        setContent {
            StupidbleTheme {
                BleApp(bleRepository, bleConnectionManager, metadataRepository)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BleApp(
    bleRepository: BleRepository,
    bleConnectionManager: BleConnectionManager,
    metadataRepository: MetadataRepository
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = PermissionHelper.REQUIRED_PERMISSIONS.toList()
    )

    if (permissionsState.allPermissionsGranted) {
        BleNavigation(bleRepository, bleConnectionManager, metadataRepository)
    } else {
        Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Bluetooth permissions are required")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}

@Composable
fun BleNavigation(
    bleRepository: BleRepository,
    bleConnectionManager: BleConnectionManager,
    metadataRepository: MetadataRepository
) {
    val navController = rememberNavController()
    val scannerViewModel: ScannerViewModel = viewModel(
        factory = ScannerViewModelFactory(bleRepository)
    )
    val deviceDetailViewModel: DeviceDetailViewModel = viewModel(
        factory = DeviceDetailViewModelFactory(bleConnectionManager, metadataRepository)
    )
    val characteristicEditViewModel: CharacteristicEditViewModel = viewModel(
        factory = CharacteristicEditViewModelFactory(metadataRepository)
    )

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        NavHost(
            navController = navController,
            startDestination = "scanner",
            modifier = Modifier.padding(padding)
        ) {
            composable("scanner") {
                ScannerScreen(
                    viewModel = scannerViewModel,
                    onDeviceClick = { device ->
                        deviceDetailViewModel.setDevice(device)
                        navController.navigate("deviceDetail")
                    }
                )
            }
            composable("deviceDetail") {
                val device = deviceDetailViewModel.device.value
                DeviceDetailScreen(
                    viewModel = deviceDetailViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { serviceUuid, characteristicUuid ->
                        device?.let {
                            navController.navigate("characteristicEdit/${it.address}/$serviceUuid/$characteristicUuid")
                        }
                    }
                )
            }
            composable(
                route = "characteristicEdit/{deviceAddress}/{serviceUuid}/{characteristicUuid}",
                arguments = listOf(
                    navArgument("deviceAddress") { type = NavType.StringType },
                    navArgument("serviceUuid") { type = NavType.StringType },
                    navArgument("characteristicUuid") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val deviceAddress = backStackEntry.arguments?.getString("deviceAddress") ?: ""
                val serviceUuid = backStackEntry.arguments?.getString("serviceUuid") ?: ""
                val characteristicUuid = backStackEntry.arguments?.getString("characteristicUuid") ?: ""

                CharacteristicEditScreen(
                    deviceAddress = deviceAddress,
                    serviceUuid = serviceUuid,
                    characteristicUuid = characteristicUuid,
                    viewModel = characteristicEditViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}