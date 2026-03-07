package com.example.drumreader.repository

import com.example.drumreader.model.DrumHit
import kotlinx.coroutines.flow.Flow

interface DrumRepository {
    val midiMessages: Flow<String>
    val drumHits: Flow<DrumHit>
}
