package com.turkcell.core.domain.purchase

enum class PurchaseStatus {
    PENDING,
    PAID;

    companion object {
        fun fromApi(value: String): PurchaseStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: PENDING
    }
}
