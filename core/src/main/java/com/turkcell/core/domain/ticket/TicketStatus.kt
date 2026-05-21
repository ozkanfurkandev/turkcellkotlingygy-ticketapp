package com.turkcell.core.domain.ticket

enum class TicketStatus {
    VALID,
    USED;

    companion object {
        fun fromApi(value: String): TicketStatus =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: VALID
    }
}
