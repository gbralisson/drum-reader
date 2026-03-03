package com.example.drumreader.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drumreader.repository.ConnectionRepository
import com.example.drumreader.repository.DrumRepository

/**
 * A shared factory for creating ViewModels.
 * You can pass multiple repositories here to handle different ViewModels.
 */
class DrumViewModelFactory(
    private val connectionRepository: ConnectionRepository? = null,
    private val drumRepository: DrumRepository? = null
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ConnectionViewModel::class.java) -> {
                connectionRepository?.let { ConnectionViewModel(it) as T }
                    ?: throw IllegalArgumentException("ConnectionRepository required for ConnectionViewModel")
            }
            modelClass.isAssignableFrom(DrumViewModel::class.java) -> {
                drumRepository?.let { DrumViewModel(it) as T }
                    ?: throw IllegalArgumentException("DrumRepository required for DrumViewModel")
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
