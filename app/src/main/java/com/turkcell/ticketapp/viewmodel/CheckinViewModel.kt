package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.checkin.CheckinRepository
import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.core.domain.event.Event
import com.turkcell.core.util.toCheckinMessage
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckinUiState(
    val isLoadingEvents: Boolean = false,
    val events: List<Event> = emptyList(),
    val eventsError: String? = null,
    val isScanning: Boolean = false,
    val lastResult: CheckinResult? = null,
    val scanError: String? = null,
)

class CheckinViewModel(
    private val checkinRepository: CheckinRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(CheckinUiState())
    val state: StateFlow<CheckinUiState> = _state.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        if (_state.value.isLoadingEvents) return
        _state.update { it.copy(isLoadingEvents = true, eventsError = null) }

        viewModelScope.launch {
            checkinRepository.getAssignedEvents().fold(
                onSuccess = { list ->
                    _state.update {
                        it.copy(isLoadingEvents = false, events = list, eventsError = null)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoadingEvents = false, eventsError = error.toUserMessage())
                    }
                },
            )
        }
    }

    fun onScanStarted() {
        _state.update { it.copy(isScanning = true, scanError = null, lastResult = null) }
    }

    fun onQrScanned(qrCode: String) {
        _state.update { it.copy(isScanning = true, scanError = null) }

        viewModelScope.launch {
            checkinRepository.scan(qrCode).fold(
                onSuccess = { result ->
                    _state.update {
                        it.copy(isScanning = false, lastResult = result, scanError = null)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isScanning = false, scanError = error.toCheckinMessage())
                    }
                },
            )
        }
    }

    fun onScanCancelled() {
        _state.update { it.copy(isScanning = false) }
    }

    fun clearResult() {
        _state.update { it.copy(lastResult = null, scanError = null) }
    }
}
