package com.turkcell.core.domain.purchase

import com.turkcell.core.domain.ticket.TicketStatus

data class PurchaseTicket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val ticketTypeId: String,
)
