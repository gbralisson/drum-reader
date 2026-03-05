package com.example.drumreader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drumreader.repository.DrumRepository
import com.example.drumreader.ui.components.StaffNote
import com.example.drumreader.utils.DrumSoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrumViewModel(private val drumRepository: DrumRepository) : ViewModel() {
    private val _midiMessages = MutableStateFlow<List<String>>(emptyList())
    val midiMessages: StateFlow<List<String>> = _midiMessages.asStateFlow()

    private val _currentMeasure = MutableStateFlow<Float?>(null)
    val currentMeasure: StateFlow<Float?> = _currentMeasure.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var playbackJob: Job? = null
    private var bpm = 120f
    private var maxMeasures = 0f
    private var notes: List<StaffNote> = emptyList()
    private var soundPlayer: DrumSoundPlayer? = null

    init {
        viewModelScope.launch {
            drumRepository.drumHits.collect { message ->
                _midiMessages.value = (_midiMessages.value + message).takeLast(100)
            }
        }
    }

    fun initPlayback(notes: List<StaffNote>, maxMeasures: Float, soundPlayer: DrumSoundPlayer) {
        this.notes = notes
        this.maxMeasures = maxMeasures
        this.soundPlayer = soundPlayer
    }

    fun setMaxMeasures(count: Float) {
        maxMeasures = count
    }

    fun togglePlayback() {
        if (_isPlaying.value) {
            stopPlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val startMeasure = _currentMeasure.value ?: 0f
            
            val beatsPerMeasure = 4f
            val measuresPerSecond = (bpm / 60f) / beatsPerMeasure
            
            var lastPlayedNoteIndex = -1
            // If starting from middle, skip notes before startMeasure
            if (startMeasure > 0) {
                lastPlayedNoteIndex = notes.indexOfLast { it.xOffset < startMeasure }
            }

            while (_isPlaying.value) {
                val elapsedMillis = System.currentTimeMillis() - startTime
                val elapsedSeconds = elapsedMillis / 1000f
                val newMeasure = startMeasure + (elapsedSeconds * measuresPerSecond)
                
                // Play sounds for notes between last checked position and current position
                notes.forEachIndexed { index, note ->
                    if (index > lastPlayedNoteIndex && note.xOffset <= newMeasure) {
                        soundPlayer?.playNote(note.step, note.octave)
                        lastPlayedNoteIndex = index
                    }
                }

                if (maxMeasures > 0 && newMeasure >= maxMeasures) {
                    _currentMeasure.value = maxMeasures
                    _isPlaying.value = false
                    break
                }
                
                _currentMeasure.value = newMeasure
                delay(10) // Higher resolution for audio timing
            }
            _currentMeasure.value = 0f
        }
    }

    private fun stopPlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
    }
    
    fun resetPlayback() {
        stopPlayback()
        _currentMeasure.value = 0f
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer?.release()
    }
}
