package com.turkcell.core.domain.ticket

data class UserTicket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val usedAt: String?,
    val ticketTypeName: String,
    val priceCents: Long,
    val eventId: String,
    val eventName: String,
    val venue: String,
    val startsAt: String,
)
