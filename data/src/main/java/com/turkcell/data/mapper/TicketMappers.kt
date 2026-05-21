package com.turkcell.data.mapper

import com.turkcell.core.domain.ticket.TicketStatus
import com.turkcell.core.domain.ticket.UserTicket
import com.turkcell.data.dto.ticket.UserTicketDto

internal fun UserTicketDto.toDomain(): UserTicket = UserTicket(
    id = id,
    qrCode = qrCode,
    status = TicketStatus.fromApi(status),
    usedAt = usedAt,
    ticketTypeName = ticketType.name,
    priceCents = ticketType.priceCents,
    eventId = ticketType.event.id,
    eventName = ticketType.event.name,
    venue = ticketType.event.venue,
    startsAt = ticketType.event.startsAt,
)
