package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.purchase.Purchase
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.domain.purchase.PurchaseStatus
import com.turkcell.core.util.toPurchaseMessage
import com.turkcell.core.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyPurchasesUiState(
    val isLoading: Boolean = false,
    val purchases: List<Purchase> = emptyList(),
    val errorMessage: String? = null,
    val payingPurchaseId: String? = null,
    val actionMessage: String? = null,
)

class MyPurchasesViewModel(
    private val purchaseRepository: PurchaseRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MyPurchasesUiState())
    val state: StateFlow<MyPurchasesUiState> = _state.asStateFlow()

    init {
        loadPurchases()
    }

    fun loadPurchases(force: Boolean = false) {
        if (_state.value.isLoading && !force) return
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            purchaseRepository.getMyPurchases().fold(
                onSuccess = { list ->
                    _state.update {
                        it.copy(isLoading = false, purchases = list, errorMessage = null)
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

    fun payPurchase(purchaseId: String) {
        if (_state.value.payingPurchaseId != null) return
        _state.update { it.copy(payingPurchaseId = purchaseId, actionMessage = null) }

        viewModelScope.launch {
            purchaseRepository.pay(purchaseId).fold(
                onSuccess = {
                    _state.update { it.copy(payingPurchaseId = null, actionMessage = null) }
                    loadPurchases(force = true)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            payingPurchaseId = null,
                            actionMessage = error.toPurchaseMessage(),
                        )
                    }
                },
            )
        }
    }

    fun consumeActionMessage() {
        _state.update { it.copy(actionMessage = null) }
    }
}
