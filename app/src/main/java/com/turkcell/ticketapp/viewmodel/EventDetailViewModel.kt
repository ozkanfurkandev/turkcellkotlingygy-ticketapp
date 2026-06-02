package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.network.ApiException
import com.turkcell.core.util.toPurchaseMessage
import com.turkcell.core.util.toUserMessage
import com.turkcell.ticketapp.navigation.EventDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val errorMessage: String? = null,
    val quantities: Map<String, Int> = emptyMap(),
    val isProcessing: Boolean = false,
    val showPaymentDialog: Boolean = false,
    val pendingPurchaseId: String? = null,
    val actionError: String? = null,
    val purchaseSucceeded: Boolean = false,
) {
    fun quantityFor(ticketTypeId: String): Int = quantities[ticketTypeId] ?: 0

    fun totalCents(event: Event?): Long {
        if (event == null) return 0L
        return event.ticketTypes.sumOf { type ->
            val qty = quantities[type.id] ?: 0
            qty * type.priceCents
        }
    }

    val hasSelection: Boolean get() = quantities.values.any { it > 0 }
}

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val eventId: String = savedStateHandle.toRoute<EventDetail>().id

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            eventRepository.getEvent(eventId).fold(
                onSuccess = { event ->
                    _state.update {
                        it.copy(isLoading = false, event = event, errorMessage = null)
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = error.toUserMessage())
                    }
                },
            )
        }
    }

    fun incrementQuantity(ticketTypeId: String) {
        val event = _state.value.event ?: return
        val ticketType = event.ticketTypes.find { it.id == ticketTypeId } ?: return
        val current = _state.value.quantities[ticketTypeId] ?: 0
        val maxQty = minOf(20, ticketType.remaining.toInt())
        if (current >= maxQty) return

        _state.update {
            it.copy(
                quantities = it.quantities + (ticketTypeId to current + 1),
                actionError = null,
            )
        }
    }

    fun decrementQuantity(ticketTypeId: String) {
        val current = _state.value.quantities[ticketTypeId] ?: 0
        if (current <= 0) return
        val next = current - 1
        _state.update {
            val updated = if (next == 0) it.quantities - ticketTypeId else it.quantities + (ticketTypeId to next)
            it.copy(quantities = updated, actionError = null)
        }
    }

    fun onBuyClick() {
        val current = _state.value
        if (!current.hasSelection || current.isProcessing) return

        val items = current.quantities.filterValues { it > 0 }
        _state.update { it.copy(isProcessing = true, actionError = null) }

        viewModelScope.launch {
            purchaseRepository.createPurchase(items).fold(
                onSuccess = { purchase ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            showPaymentDialog = true,
                            pendingPurchaseId = purchase.id,
                        )
                    }
                },
                onFailure = { error ->
                    handlePurchaseError(error)
                },
            )
        }
    }

    fun dismissPaymentDialog() {
        _state.update { it.copy(showPaymentDialog = false, pendingPurchaseId = null) }
    }

    fun confirmPayment() {
        val purchaseId = _state.value.pendingPurchaseId ?: return
        if (_state.value.isProcessing) return

        _state.update { it.copy(isProcessing = true, actionError = null) }

        viewModelScope.launch {
            purchaseRepository.pay(purchaseId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            showPaymentDialog = false,
                            pendingPurchaseId = null,
                            purchaseSucceeded = true,
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isProcessing = false, showPaymentDialog = false) }
                    handlePurchaseError(error)
                },
            )
        }
    }

    fun consumePurchaseSuccess() {
        _state.update { it.copy(purchaseSucceeded = false) }
    }

    private fun handlePurchaseError(error: Throwable) {
        if (error is ApiException && error.code == 409 &&
            error.errorMessage?.contains("capacity_exceeded", ignoreCase = true) == true
        ) {
            loadEvent()
        }
        _state.update {
            it.copy(isProcessing = false, actionError = error.toPurchaseMessage())
        }
    }
}
