package com.storycanvas.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.storycanvas.app.viewmodel.DebugViewModel

@Composable
fun DebugScreen(
    viewModel: DebugViewModel = viewModel(),
    onNavigateToMain: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    
    var hostInput by remember { mutableStateOf(uiState.host) }
    var portInput by remember { mutableStateOf(uiState.port.toString()) }
    
    // Update local state when viewModel state changes
    LaunchedEffect(uiState.host, uiState.port) {
        hostInput = uiState.host
        portInput = uiState.port.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Debug Mode",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Status message
        Text(
            text = uiState.statusMessage,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Backend Configuration Section
        Text(
            text = "Backend Configuration",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Host input
        OutlinedTextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("Host (e.g., 10.0.2.2 or IP address)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true
        )
        
        // Port input
        OutlinedTextField(
            value = portInput,
            onValueChange = { portInput = it },
            label = { Text("Port (e.g., 8080)") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        // Apply Configuration Button
        Button(
            onClick = { 
                viewModel.updateBackendConfig(hostInput, portInput.toIntOrNull() ?: 8080)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Apply Configuration")
        }
        
        // Auto-Discover Backend Button
        Button(
            onClick = { viewModel.discoverBackend() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Auto-Discover Backend")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Network Diagnostics Section
        if (uiState.diagnostics.isNotEmpty()) {
            Text(
                text = "Network Diagnostics:",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Internet status
            val internetAvailable = uiState.diagnostics["internetAvailable"] as? Boolean ?: false
            DiagnosticItem(
                label = "Internet Available:", 
                value = "$internetAvailable",
                isPositive = internetAvailable
            )
            
            // Host reachability
            val hostReachable = uiState.diagnostics["hostReachable"] as? Boolean ?: false
            DiagnosticItem(
                label = "Host Reachability:", 
                value = "${uiState.host}: ${if (hostReachable) "Reachable" else "Unreachable"}",
                isPositive = hostReachable
            )
            
            // Server reachability
            val serverReachable = uiState.diagnostics["serverReachable"] as? Boolean ?: false
            DiagnosticItem(
                label = "${uiState.host}:${uiState.port}:", 
                value = if (serverReachable) "Reachable" else "Unreachable",
                isPositive = serverReachable
            )
            
            // Server status
            val serverStatus = uiState.diagnostics["serverStatus"] as? String ?: "Unknown"
            DiagnosticItem(
                label = "Server Status:", 
                value = serverStatus,
                isPositive = serverStatus == "Available"
            )
            
            // Error message if any
            if (uiState.diagnostics.containsKey("error")) {
                val error = uiState.diagnostics["error"] as? String ?: ""
                if (error.isNotEmpty()) {
                    Text(
                        text = "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            
            // Device IP
            val deviceIp = uiState.diagnostics["deviceIp"] as? String ?: "Unknown"
            DiagnosticItem(
                label = "Device IP:", 
                value = deviceIp,
                isHighlight = true
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Retry Connection Button
        Button(
            onClick = { viewModel.retryConnection() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Retry Connection")
        }
        
        // Help text
        if (!uiState.isConnected) {
            Text(
                text = "Make sure the Spring Boot backend is running and accessible at http://${uiState.host}:${uiState.port}/api/",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun DiagnosticItem(
    label: String,
    value: String,
    isPositive: Boolean? = null,
    isHighlight: Boolean = false
) {
    val textColor = when {
        isHighlight -> MaterialTheme.colorScheme.primary
        isPositive == true -> Color(0xFF4CAF50) // Green
        isPositive == false -> Color(0xFFF44336) // Red
        else -> Color.Unspecified
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = textColor,
            fontWeight = if (isHighlight || isPositive != null) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
} 