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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.calica.stupidble.data.repository.BleRepository
import com.calica.stupidble.ui.devicedetail.DeviceDetailScreen
import com.calica.stupidble.ui.devicedetail.DeviceDetailViewModel
import com.calica.stupidble.ui.scanner.ScannerScreen
import com.calica.stupidble.ui.scanner.ScannerViewModel
import com.calica.stupidble.ui.scanner.ScannerViewModelFactory
import com.calica.stupidble.ui.theme.StupidbleTheme
import com.calica.stupidble.util.PermissionHelper
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    private lateinit var bleRepository: BleRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bleRepository = BleRepository(this)

        enableEdgeToEdge()
        setContent {
            StupidbleTheme {
                BleApp(bleRepository)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BleApp(bleRepository: BleRepository) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = PermissionHelper.REQUIRED_PERMISSIONS.toList()
    )

    if (permissionsState.allPermissionsGranted) {
        BleNavigation(bleRepository)
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
fun BleNavigation(bleRepository: BleRepository) {
    val navController = rememberNavController()
    val scannerViewModel: ScannerViewModel = viewModel(
        factory = ScannerViewModelFactory(bleRepository)
    )
    val deviceDetailViewModel: DeviceDetailViewModel = viewModel()

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
                DeviceDetailScreen(
                    viewModel = deviceDetailViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}