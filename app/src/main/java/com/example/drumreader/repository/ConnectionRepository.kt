package com.example.drumreader.repository

import kotlinx.coroutines.flow.Flow

interface ConnectionRepository {
    val debugMessages: Flow<String>
    suspend fun connect(deviceName: String): Boolean
    suspend fun disconnect()
}
