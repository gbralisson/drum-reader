package com.example.drumreader.repository

import android.util.Log
import com.example.drumreader.midi.MidiConnectionManager
import kotlinx.coroutines.flow.Flow

class ConnectionRepositoryImpl(private val midiConnectionManager: MidiConnectionManager) : ConnectionRepository {

    override val debugMessages: Flow<String> = midiConnectionManager.debugMessages

    companion object {
        private const val TAG = "ConnectionRepository"
    }

    override suspend fun connect(deviceName: String): Boolean {
        Log.d(TAG, "Connecting to device: $deviceName")
        val success = midiConnectionManager.scanAndConnect()
        if (success) {
            Log.d(TAG, "Connected to device: $deviceName")
        } else {
            Log.e(TAG, "Could not connect to device")
        }
        return success
    }

    override suspend fun disconnect() {
        midiConnectionManager.close()
    }
}
