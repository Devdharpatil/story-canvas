package com.storycanvas.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.storycanvas.app.data.remote.BackendDiscovery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DebugUiState(
    val host: String = "10.0.2.2",
    val port: Int = 8080,
    val isConnected: Boolean = false,
    val statusMessage: String = "App started successfully",
    val diagnostics: Map<String, Any> = emptyMap()
)

class DebugViewModel(application: Application) : AndroidViewModel(application) {
    
    private val backendDiscovery = BackendDiscovery.getInstance(application)
    
    private val _uiState = MutableStateFlow(
        DebugUiState(
            host = backendDiscovery.host,
            port = backendDiscovery.port
        )
    )
    val uiState: StateFlow<DebugUiState> = _uiState.asStateFlow()
    
    init {
        runDiagnostics()
    }
    
    /**
     * Update backend configuration
     */
    fun updateBackendConfig(host: String, port: Int) {
        if (backendDiscovery.updateConfig(host, port)) {
            _uiState.update { it.copy(
                host = host,
                port = port,
                statusMessage = "Configuration updated. Testing connection..."
            ) }
            runDiagnostics()
        }
    }
    
    /**
     * Attempt to discover the backend automatically
     */
    fun discoverBackend() {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Attempting to discover backend...") }
            
            val success = backendDiscovery.discoverBackend()
            if (success) {
                _uiState.update { it.copy(
                    host = backendDiscovery.host,
                    port = backendDiscovery.port,
                    statusMessage = "Backend discovered successfully at ${backendDiscovery.host}:${backendDiscovery.port}"
                ) }
                runDiagnostics()
            } else {
                _uiState.update { it.copy(
                    statusMessage = "Auto-discovery failed. Check that backend is running."
                ) }
                runDiagnostics()
            }
        }
    }
    
    /**
     * Retry connection to the backend
     */
    fun retryConnection() {
        _uiState.update { it.copy(statusMessage = "Retrying connection...") }
        runDiagnostics()
    }
    
    /**
     * Run network diagnostics
     */
    private fun runDiagnostics() {
        viewModelScope.launch {
            val diagnostics = backendDiscovery.getNetworkDiagnostics()
            
            // Check if server is reachable
            val serverReachable = diagnostics["serverReachable"] as? Boolean ?: false
            val serverStatus = diagnostics["serverStatus"] as? String ?: "Unknown"
            
            val isConnected = serverReachable && serverStatus == "Available"
            val statusMessage = if (isConnected) {
                "Connected to backend at ${backendDiscovery.host}:${backendDiscovery.port}"
            } else {
                "Backend is not available: Expected BEGIN_OBJECT but was STRING at line 1 column 1 path \$"
            }
            
            _uiState.update { it.copy(
                isConnected = isConnected,
                diagnostics = diagnostics,
                statusMessage = statusMessage
            ) }
        }
    }
} 