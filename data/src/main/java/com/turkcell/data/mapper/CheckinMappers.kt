package com.turkcell.data.mapper

import com.turkcell.core.domain.checkin.CheckinResult
import com.turkcell.data.dto.checkin.ScanResponseDto

fun ScanResponseDto.toDomain(): CheckinResult = CheckinResult(
    ticketId = ticketId,
    ticketType = ticketType,
    eventName = event.name,
    eventVenue = event.venue,
    eventStartsAt = event.startsAt,
    checkedInAt = checkedInAt,
)
