package com.example.drumreader.repository

import kotlinx.coroutines.flow.Flow

interface DrumRepository {
    val drumHits: Flow<String>
}
