package com.example.drumreader.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drumreader.model.PlaybackNote
import com.example.drumreader.repository.DrumRepository
import com.example.drumreader.ui.components.StaffNote
import com.example.drumreader.utils.DrumSoundPlayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class DrumViewModel(private val drumRepository: DrumRepository) : ViewModel() {
    private val _midiMessages = MutableStateFlow<List<String>>(emptyList())
    val midiMessages: StateFlow<List<String>> = _midiMessages.asStateFlow()

    private val _currentMeasure = MutableStateFlow<Float?>(null)
    val currentMeasure: StateFlow<Float?> = _currentMeasure.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isSynced = MutableStateFlow(false)
    val isSynced: StateFlow<Boolean> = _isSynced.asStateFlow()

    private var playbackJob: Job? = null
    private var syncFeedbackJob: Job? = null
    private var bpm = 120f
    private var maxMeasures = 0f
    private var notes: List<StaffNote> = emptyList()
    private var soundPlayer: DrumSoundPlayer? = null
    
    private val activePlaybackNotes = mutableListOf<PlaybackNote>()

    init {
        viewModelScope.launch {
            drumRepository.midiMessages.collect { message ->
                _midiMessages.value = (_midiMessages.value + message).takeLast(100)
            }
        }

        viewModelScope.launch {
            drumRepository.drumHits.collect { hit ->
                if (_isPlaying.value) {
                    checkHitSync(hit.component, hit.timestamp)
                }
            }
        }
    }

    private fun checkHitSync(drumComponent: String, hitTime: Long) {
        val syncThreshold = 250L // Tolerance window for hit sync
        
        // Find the closest expected note for this component that hasn't been hit yet
        val matchingNote = activePlaybackNotes
            .asSequence()
            .filter { !it.wasHit && abs(it.expectedTimeMillis - hitTime) <= syncThreshold }
            .filter { isComponentMatch(it.note, drumComponent) }
            .minByOrNull { abs(it.expectedTimeMillis - hitTime) }

        matchingNote?.let {
            it.wasHit = true
            val diff = abs(it.expectedTimeMillis - hitTime)
            Log.d("DrumViewModel", "Sync Hit! Component: $drumComponent, Diff: ${diff}ms")
            
            // Verification logic for chords (multiple notes at the same time)
            // We only trigger the green tracker feedback if ALL notes in the chord are hit
            val chordNotes = activePlaybackNotes.filter { n -> n.note.xOffset == it.note.xOffset }
            val hitCount = chordNotes.count { n -> n.wasHit }
            
            if (hitCount == chordNotes.size) {
                Log.d("DrumViewModel", "Full sync verified for ${chordNotes.size} note(s) at measure ${it.note.xOffset}")
                triggerSyncFeedback()
            } else {
                Log.d("DrumViewModel", "Partial chord sync: $hitCount/${chordNotes.size} notes hit")
            }
        }
    }

    private fun triggerSyncFeedback() {
        syncFeedbackJob?.cancel()
        syncFeedbackJob = viewModelScope.launch {
            _isSynced.value = true
            delay(200) // Feedback duration (slightly longer for visibility)
            _isSynced.value = false
        }
    }

    private fun isComponentMatch(note: StaffNote, drumComponent: String): Boolean {
        // Simplified mapping logic based on standard Alesis/MIDI drum notation
        return when (note.step) {
            "F" if note.octave == 4 && drumComponent.contains("Kick", true) -> true
            "C" if note.octave == 5 && drumComponent.contains("Snare", true) -> true
            "G" if note.octave == 5 && (drumComponent.contains("Hi-Hat", true) || drumComponent.contains("HH", true)) -> true
            "F" if note.octave == 5 && drumComponent.contains("Ride", true) -> true
            "A" if note.octave == 5 && drumComponent.contains("Crash", true) -> true
            "E" if note.octave == 5 && drumComponent.contains("Tom 1", true) -> true
            "D" if note.octave == 5 && drumComponent.contains("Tom 2", true) -> true
            "A" if note.octave == 4 && drumComponent.contains("Tom 3", true) -> true
            else -> false
        }
    }

    fun initPlayback(notes: List<StaffNote>, maxMeasures: Float, soundPlayer: DrumSoundPlayer) {
        this.notes = notes
        this.maxMeasures = maxMeasures
        this.soundPlayer = soundPlayer
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
        activePlaybackNotes.clear()
        
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val startMeasure = _currentMeasure.value ?: 0f
            
            val beatsPerMeasure = 4f
            val measuresPerSecond = (bpm / 60f) / beatsPerMeasure
            
            // Pre-calculate expected times for all notes relative to startTime
            notes.forEach { note ->
                val noteTimeOffsetMillis = ((note.xOffset - startMeasure) / measuresPerSecond * 1000).toLong()
                activePlaybackNotes.add(PlaybackNote(note, startTime + noteTimeOffsetMillis))
            }

            var lastPlayedNoteIndex = -1
            if (startMeasure > 0) {
                lastPlayedNoteIndex = notes.indexOfLast { it.xOffset < startMeasure }
            }

            while (_isPlaying.value) {
                val elapsedMillis = System.currentTimeMillis() - startTime
                val elapsedSeconds = elapsedMillis / 1000f
                val newMeasure = startMeasure + (elapsedSeconds * measuresPerSecond)
                
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
                delay(10)
            }
            // Reset when playback stops
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
