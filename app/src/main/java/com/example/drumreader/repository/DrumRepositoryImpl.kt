package com.example.drumreader.repository

import com.example.drumreader.midi.MidiConnectionManager
import kotlinx.coroutines.flow.Flow

class DrumRepositoryImpl(midiConnectionManager: MidiConnectionManager) : DrumRepository {
    override val drumHits: Flow<String> = midiConnectionManager.midiMessages
}
