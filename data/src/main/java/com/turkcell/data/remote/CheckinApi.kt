package com.turkcell.data.remote

import com.turkcell.data.dto.checkin.ScanRequestDto
import com.turkcell.data.dto.checkin.ScanResponseDto
import com.turkcell.data.dto.event.EventDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CheckinApi {
    @GET("/checkin/events")
    suspend fun getAssignedEvents(): List<EventDto>

    @POST("/checkin/scan")
    suspend fun scan(@Body body: ScanRequestDto): ScanResponseDto
}
