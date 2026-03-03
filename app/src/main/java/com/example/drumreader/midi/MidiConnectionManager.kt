package com.example.drumreader.midi

import android.content.Context
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.drumreader.model.AlesisDrumKit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Handles MIDI device connection and message processing for Alesis drum kits.
 */
class MidiConnectionManager(context: Context) {

    private val midiManager: MidiManager = context.getSystemService(Context.MIDI_SERVICE) as MidiManager
    
    // Flow for drum hits
    private val _midiMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val midiMessages: SharedFlow<String> = _midiMessages.asSharedFlow()

    // Flow for connectivity debug messages
    private val _debugMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 100,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val debugMessages: SharedFlow<String> = _debugMessages.asSharedFlow()
    
    companion object {
        private const val TAG = "MidiConnectionManager"
    }

    private fun logDebug(message: String) {
        Log.d(TAG, message)
        _debugMessages.tryEmit(message)
    }

    private fun logInfo(message: String) {
        Log.i(TAG, message)
        _debugMessages.tryEmit(message)
    }

    private fun logWarn(message: String) {
        Log.w(TAG, message)
        _debugMessages.tryEmit(message)
    }

    private fun logError(message: String, e: Throwable? = null) {
        if (e != null) Log.e(TAG, message, e) else Log.e(TAG, message)
        _debugMessages.tryEmit("ERROR: $message")
    }

    /**
     * Scans for available MIDI devices and attempts to connect to an Alesis device.
     * @return true if an Alesis device was found and the opening process was initiated.
     */
    fun scanAndConnect(): Boolean {
        logInfo("Scanning for MIDI devices...")
        val infos = midiManager.devices
        logDebug("Found ${infos.size} MIDI devices")
        for (info in infos) {
            val name = info.properties.getString(MidiDeviceInfo.PROPERTY_NAME)
            logDebug("Found MIDI device: $name")
            if (name?.contains("Alesis", ignoreCase = true) == true) {
                logInfo("Found Alesis device: $name")
                return openDevice(info)
            }
        }
        logWarn("No Alesis device found")
        return false
    }

    private fun openDevice(info: MidiDeviceInfo): Boolean {
        return try {
            midiManager.openDevice(info, { device ->
                if (device == null) {
                    logError("Could not open MIDI device")
                } else {
                    val outputPort = device.openOutputPort(0)
                    if (outputPort == null) {
                        logError("Could not open output port")
                    } else {
                        logInfo("MIDI device opened successfully")
                        outputPort.connect(object : MidiReceiver() {
                            override fun onSend(data: ByteArray, offset: Int, count: Int, timestamp: Long) {
                                processMidiMessage(data, offset, count)
                            }
                        })
                    }
                }
            }, Handler(Looper.getMainLooper()))
            true
        } catch (e: Exception) {
            logError("Error initiating MIDI device opening", e)
            false
        }
    }

    fun close() {
        // TODO: Implement actual MIDI device closing logic if needed
        logInfo("MIDI connection closed")
    }

    private fun processMidiMessage(data: ByteArray, offset: Int, count: Int) {
        if (count < 3) return
        
        val status = data[offset].toInt() and 0xFF
        val note = data[offset + 1].toInt() and 0xFF
        val velocity = data[offset + 2].toInt() and 0xFF

        // Note On message usually starts with 0x90 (144)
        if (status in 144..159 && velocity > 0) {
            val drumComponent = AlesisDrumKit.fromMidiNote(note)
            val message = "Hit: $drumComponent (Vel: $velocity)"
            _midiMessages.tryEmit(message)
        }
    }
}
