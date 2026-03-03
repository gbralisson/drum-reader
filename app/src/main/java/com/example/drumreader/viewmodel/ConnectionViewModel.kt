package com.example.drumreader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drumreader.model.ConnectionStatus
import com.example.drumreader.repository.ConnectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel(private val repository: ConnectionRepository) : ViewModel() {

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _debugMessages = MutableStateFlow<List<String>>(emptyList())
    val debugMessages: StateFlow<List<String>> = _debugMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.debugMessages.collect { message ->
                _debugMessages.value = (_debugMessages.value + message).takeLast(100)
            }
        }
    }

    fun connect(deviceName: String) {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Connecting
            val success = repository.connect(deviceName)
            if (success) {
                _connectionStatus.value = ConnectionStatus.Connected(deviceName)
            } else {
                _connectionStatus.value = ConnectionStatus.Error("Could not connect to $deviceName")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
            _connectionStatus.value = ConnectionStatus.Disconnected
        }
    }
}
