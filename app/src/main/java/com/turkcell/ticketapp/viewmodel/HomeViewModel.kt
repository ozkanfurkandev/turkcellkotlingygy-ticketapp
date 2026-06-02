package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.core.domain.ticket.UserTicket
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class HomeTab {
    Events,
    Tickets,
}

data class HomeUiState(
    val selectedTab: HomeTab = HomeTab.Events,
    val isEventsLoading: Boolean = false,
    val events: List<Event> = emptyList(),
    val eventsError: String? = null,
    val isTicketsLoading: Boolean = false,
    val tickets: List<UserTicket> = emptyList(),
    val ticketsError: String? = null,
    val isLoggingOut: Boolean = false,
)

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val ticketRepository: TicketRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadEvents()
        loadTickets()
    }

    fun onTabSelected(tab: HomeTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun openTicketsTab() {
        _state.update { it.copy(selectedTab = HomeTab.Tickets) }
        loadTickets(force = true)
    }

    fun refreshCurrentTab() {
        when (_state.value.selectedTab) {
            HomeTab.Events -> loadEvents(force = true)
            HomeTab.Tickets -> loadTickets(force = true)
        }
    }

    fun logout() {
        if (_state.value.isLoggingOut) return
        _state.update { it.copy(isLoggingOut = true) }
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(isLoggingOut = false) }
        }
    }

    fun loadEvents(force: Boolean = false) {
        if (_state.value.isEventsLoading && !force) return

        _state.update { it.copy(isEventsLoading = true, eventsError = null) }

        viewModelScope.launch {
            eventRepository.getEvents().fold(
                onSuccess = { list ->
                    _state.update {
                        it.copy(events = list, isEventsLoading = false, eventsError = null)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isEventsLoading = false, eventsError = error.toUserMessage())
                    }
                },
            )
        }
    }

    fun loadTickets(force: Boolean = false) {
        if (_state.value.isTicketsLoading && !force) return

        _state.update { it.copy(isTicketsLoading = true, ticketsError = null) }

        viewModelScope.launch {
            ticketRepository.getMyTickets().fold(
                onSuccess = { list ->
                    _state.update {
                        it.copy(tickets = list, isTicketsLoading = false, ticketsError = null)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isTicketsLoading = false, ticketsError = error.toUserMessage())
                    }
                },
            )
        }
    }
}
