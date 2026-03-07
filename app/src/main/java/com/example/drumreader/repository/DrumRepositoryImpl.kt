package com.example.drumreader.repository

import com.example.drumreader.midi.MidiConnectionManager
import com.example.drumreader.model.DrumHit
import kotlinx.coroutines.flow.Flow

class DrumRepositoryImpl(midiConnectionManager: MidiConnectionManager) : DrumRepository {
    override val midiMessages: Flow<String> = midiConnectionManager.midiMessages
    override val drumHits: Flow<DrumHit> = midiConnectionManager.drumHits
}
