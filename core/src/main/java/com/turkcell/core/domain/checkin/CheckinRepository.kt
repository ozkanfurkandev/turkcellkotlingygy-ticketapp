package com.turkcell.core.domain.checkin

import com.turkcell.core.domain.event.Event

data class CheckinResult(
    val ticketId: String,
    val ticketType: String,
    val eventName: String,
    val eventVenue: String,
    val eventStartsAt: String,
    val checkedInAt: String,
)

interface CheckinRepository {
    suspend fun getAssignedEvents(): Result<List<Event>>
    suspend fun scan(qrCode: String): Result<CheckinResult>
}
