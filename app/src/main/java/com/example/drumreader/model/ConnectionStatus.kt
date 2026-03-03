package com.example.drumreader.model

sealed class ConnectionStatus {
    data object Disconnected : ConnectionStatus()
    data object Connecting : ConnectionStatus()
    data class Connected(val deviceName: String) : ConnectionStatus()
    data class Error(val message: String) : ConnectionStatus()
}
