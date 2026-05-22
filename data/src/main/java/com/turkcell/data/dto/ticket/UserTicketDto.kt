package com.turkcell.data.dto.ticket

import kotlinx.serialization.Serializable

@Serializable
data class UserTicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String? = null,
    val checkedInBy: String? = null,
    val ticketType: UserTicketTypeDto,
)

@Serializable
data class UserTicketTypeDto(
    val id: String,
    val name: String,
    val priceCents: Long,
    val event: UserTicketEventDto,
)

@Serializable
data class UserTicketEventDto(
    val id: String,
    val name: String,
    val place: String? = null,
    val venue: String? = null,
    val startsAt: String,
)
