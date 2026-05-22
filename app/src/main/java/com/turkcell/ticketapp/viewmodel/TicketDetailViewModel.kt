package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.core.domain.ticket.UserTicket
import com.turkcell.ticketapp.navigation.TicketDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val ticket: UserTicket? = null,
    val errorMessage: String? = null,
)

class TicketDetailViewModel(
    private val ticketRepository: TicketRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val ticketId: String = savedStateHandle.toRoute<TicketDetail>().ticketId

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init {
        loadTicket()
    }

    fun loadTicket() {
        if (_state.value.isLoading) return

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            ticketRepository.getTicketById(ticketId).fold(
                onSuccess = { ticket ->
                    _state.update { it.copy(isLoading = false, ticket = ticket, errorMessage = null) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.toHomeErrorMessage())
                    }
                },
            )
        }
    }
}
