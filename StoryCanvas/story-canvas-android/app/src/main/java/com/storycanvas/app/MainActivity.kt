package com.storycanvas.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.storycanvas.app.data.remote.BackendDiscovery
import com.storycanvas.app.data.remote.RetrofitClient
import com.storycanvas.app.navigation.AppNavigation
import com.storycanvas.app.ui.screens.DebugScreen
import com.storycanvas.app.ui.screens.MainScreen
import com.storycanvas.app.ui.theme.StorycanvasandroidTheme
import com.storycanvas.app.util.BackendPortDiscovery
import com.storycanvas.app.util.NetworkDebugTool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Debug logging
        Log.d(TAG, "onCreate started")
        
        enableEdgeToEdge()
        
        // Initialize RetrofitClient with application context
        RetrofitClient.initialize(applicationContext)
        
        // Start backend discovery in background
        lifecycleScope.launch {
            val discovery = BackendDiscovery.getInstance(applicationContext)
            discovery.discoverBackend()
        }
        
        setContent {
            Log.d(TAG, "Inside setContent")
            StorycanvasandroidTheme {
                StoryCanvasApp()
            }
        }
    }
}

sealed class BackendStatus {
    object Unknown : BackendStatus()
    object Checking : BackendStatus()
    object Available : BackendStatus()
    data class Unavailable(val reason: String) : BackendStatus()
}

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Debug : Screen("debug")
}

@Composable
fun StoryCanvasApp() {
    val navController = rememberNavController()
    var shouldShowDebug by remember { mutableStateOf(false) }
    
    // Check if backend is available and show debug screen if not
    LaunchedEffect(Unit) {
        val discovery = BackendDiscovery.getInstance(androidx.compose.ui.platform.LocalContext.current)
        val diagnostics = discovery.getNetworkDiagnostics()
        val serverReachable = diagnostics["serverReachable"] as? Boolean ?: false
        
        shouldShowDebug = !serverReachable
        
        if (shouldShowDebug) {
            navController.navigate(Screen.Debug.route)
        }
    }
    
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppNavHost(navController = navController, startDestination = Screen.Main.route)
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Main.route) {
            MainScreen(
                onNavigateToDebug = {
                    navController.navigate(Screen.Debug.route)
                }
            )
        }
        
        composable(route = Screen.Debug.route) {
            DebugScreen(
                onNavigateToMain = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainContent() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var showFullApp by remember { mutableStateOf(false) }
    var navigationLoading by remember { mutableStateOf(false) }
    var backendStatus by remember { mutableStateOf<BackendStatus>(BackendStatus.Unknown) }
    var diagnosticResults by remember { mutableStateOf<Map<String, Any>?>(null) }
    var hostInput by remember { mutableStateOf("") }
    var portInput by remember { mutableStateOf("8080") }
    var backendUrl by remember { mutableStateOf("") }
    
    // Function to check backend status - uses coroutineScope to avoid cancellation issues
    val checkBackendStatus = {
        coroutineScope.launch {
            backendStatus = BackendStatus.Checking
            
            try {
                // Update backend URL with current input values
                try {
                    val host = hostInput.ifEmpty { "10.0.2.2" }
                    val port = portInput.toIntOrNull() ?: 8080
                    
                    BackendPortDiscovery.saveHost(context, host)
                    BackendPortDiscovery.savePort(context, port)
                    backendUrl = "http://$host:$port/api/"
                    
                    // Recreate the RetrofitClient with the new URL
                    val newService = RetrofitClient.discoverBackendService(context)
                    if (newService != null) {
                        Log.d(TAG, "Successfully updated backend URL to $host:$port")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating backend URL", e)
                }
                
                // Attempt a simple API call to test connectivity
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.instance.getPingStatus()
                    }
                    
                    if (response.isSuccessful) {
                        backendStatus = BackendStatus.Available
                        diagnosticResults = null
                    } else {
                        backendStatus = BackendStatus.Unavailable("Server returned error code: ${response.code()}")
                        // Run diagnostics
                        diagnosticResults = withContext(Dispatchers.IO) {
                            NetworkDebugTool.runDiagnostics(context, backendUrl + "ping")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Backend connectivity test failed", e)
                    backendStatus = BackendStatus.Unavailable(e.message ?: "Unknown error")
                    // Run diagnostics
                    diagnosticResults = withContext(Dispatchers.IO) {
                        NetworkDebugTool.runDiagnostics(context, backendUrl + "ping")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Backend status check failed", e)
                backendStatus = BackendStatus.Unavailable(e.message ?: "Unknown error")
                // Run diagnostics
                diagnosticResults = withContext(Dispatchers.IO) {
                    NetworkDebugTool.runDiagnostics(context, backendUrl + "ping")
                }
            }
        }
    }
    
    // Initialize host/port values from saved preferences
    // This is done once when the composable is first composed
    var initialized by remember { mutableStateOf(false) }
    if (!initialized) {
        coroutineScope.launch {
            try {
                // Get device's network environment
                val deviceIP = NetworkDebugTool.getLocalIpAddress(context) ?: "Unknown"
                Log.d(TAG, "Device IP: $deviceIP")
                
                // Get saved host/port or suggest based on device environment
                var savedHost = BackendPortDiscovery.getSavedHost(context)
                val savedPort = BackendPortDiscovery.getSavedPort(context)
                
                // If we're on a physical device but using emulator IP, try to find a better IP
                if (savedHost == "10.0.2.2" && deviceIP != "Unknown" && !deviceIP.startsWith("10.0.2")) {
                    // Use the specific laptop IP from the screenshot or infer from device IP
                    val laptopIP = "172.23.33.24" // From screenshot
                    
                    if (NetworkDebugTool.isHostReachable(laptopIP)) {
                        savedHost = laptopIP
                        Log.d(TAG, "Using laptop IP: $laptopIP")
                    } else {
                        // Try to determine network prefix and check common hosts
                        val possibleHosts = BackendPortDiscovery.generatePossibleHostIps(context)
                        
                        for (host in possibleHosts) {
                            if (NetworkDebugTool.isHostReachable(host)) {
                                savedHost = host
                                Log.d(TAG, "Found reachable host: $host")
                                break
                            }
                        }
                    }
                }
                
                hostInput = savedHost
                portInput = savedPort.toString()
                backendUrl = "http://$savedHost:$savedPort/api/"
                
                Log.d(TAG, "Initialized with backend URL: $backendUrl")
                
                // Now that we're initialized, check backend status
                initialized = true
                checkBackendStatus()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing", e)
                initialized = true // Mark as initialized even if it failed
                
                // Set default values
                hostInput = "10.0.2.2"
                portInput = "8080"
                backendUrl = "http://10.0.2.2:8080/api/"
                
                // Still try to check status
                checkBackendStatus()
            }
        }
    }
    
    // Auto-discover button handler
    val performAutoDiscovery = {
        coroutineScope.launch {
            try {
                // Run auto-discovery on background thread
                val result = withContext(Dispatchers.IO) {
                    BackendPortDiscovery.discoverBackend(context)
                }
                
                if (result != null) {
                    val (host, port) = result
                    hostInput = host
                    portInput = port.toString()
                    backendUrl = "http://$host:$port/api/"
                    
                    // Check backend status with new values
                    checkBackendStatus()
                } else {
                    backendStatus = BackendStatus.Unavailable("Could not auto-discover backend")
                    
                    // Run diagnostics
                    diagnosticResults = withContext(Dispatchers.IO) {
                        NetworkDebugTool.runDiagnostics(context, backendUrl + "ping")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during auto-discovery", e)
                backendStatus = BackendStatus.Unavailable("Auto-discovery error: ${e.message}")
            }
        }
    }
    
    // Launch full app function
    val launchFullApp = {
        coroutineScope.launch {
            try {
                navigationLoading = true
                showFullApp = true
            } catch (e: Exception) {
                Log.e(TAG, "Error launching full app", e)
                navigationLoading = false
                showFullApp = false
            }
        }
    }
    
    // Render appropriate UI based on state
    if (showFullApp) {
        if (!navigationLoading) {
            Log.d(TAG, "Rendering AppNavigation()")
            AppNavigation()
        } else {
            // Show loading state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                CircularProgressIndicator(
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    } else {
        // Debug mode content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Debug Mode",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            Text(
                text = "App started successfully",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
            
            // Backend configuration section
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text(
                text = "Backend Configuration",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Host input
            TextField(
                value = hostInput,
                onValueChange = { hostInput = it },
                label = { Text("Host (e.g., 10.0.2.2 or IP address)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
            
            // Port input
            TextField(
                value = portInput,
                onValueChange = { portInput = it },
                label = { Text("Port (e.g., 8080)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
            
            // Apply button
            Button(
                onClick = { checkBackendStatus() },
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Text("Apply Configuration")
            }
            
            // Auto-discover button
            Button(
                onClick = { performAutoDiscovery() },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Auto-Discover Backend")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Show backend status
            when (backendStatus) {
                BackendStatus.Unknown -> {
                    Text("Checking backend status...", modifier = Modifier.padding(8.dp))
                }
                BackendStatus.Checking -> {
                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                    Text("Checking backend status...", modifier = Modifier.padding(8.dp))
                }
                BackendStatus.Available -> {
                    Text(
                        "Backend is available",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    Text(
                        "Connected to: $backendUrl",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Button(
                        onClick = { launchFullApp() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Launch Full App")
                    }
                }
                is BackendStatus.Unavailable -> {
                    Text(
                        "Backend is not available: ${(backendStatus as BackendStatus.Unavailable).reason}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                    
                    Button(
                        onClick = { checkBackendStatus() },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Retry Connection")
                    }
                    
                    Text(
                        "Make sure the Spring Boot backend is running and accessible at $backendUrl",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    
                    // Display diagnostic results if available
                    diagnosticResults?.let { results ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                        
                        Text(
                            "Network Diagnostics:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        
                        // Internet connectivity
                        val isInternetAvailable = results["internet_available"] as Boolean
                        Text(
                            "Internet Available: $isInternetAvailable",
                            color = if (isInternetAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        // Host checks
                        Text(
                            "Host Reachability:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        
                        @Suppress("UNCHECKED_CAST")
                        val hostChecks = results["host_checks"] as Map<String, Boolean>
                        hostChecks.forEach { (host, reachable) ->
                            Text(
                                "$host: ${if (reachable) "Reachable" else "Unreachable"}",
                                color = if (reachable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 16.dp, bottom = 2.dp)
                            )
                        }
                        
                        // Server check
                        val serverAccessible = results["server_accessible"] as Boolean
                        val serverMessage = results["server_message"] as String
                        Text(
                            "Server Status: ${if (serverAccessible) "Accessible" else "Inaccessible"}",
                            color = if (serverAccessible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                        
                        Text(
                            "Error: $serverMessage",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                        )
                        
                        // Local IP
                        Text(
                            "Device IP: ${results["local_ip"]}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}