package com.example.drumreader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drumreader.repository.DrumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrumViewModel(private val drumRepository: DrumRepository) : ViewModel() {
    private val _midiMessages = MutableStateFlow<List<String>>(emptyList())
    val midiMessages: StateFlow<List<String>> = _midiMessages.asStateFlow()

    init {
        viewModelScope.launch {
            drumRepository.drumHits.collect { message ->
                _midiMessages.value = (_midiMessages.value + message).takeLast(100)
            }
        }
    }

}
