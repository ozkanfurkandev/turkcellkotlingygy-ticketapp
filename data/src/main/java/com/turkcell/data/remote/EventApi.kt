package com.turkcell.data.remote

import com.turkcell.data.dto.event.EventDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {
    @GET("/events")
    suspend fun getUpcomingEvents(@Query("upcoming") upcoming: Boolean = true): List<EventDto>

    @GET("/events")
    suspend fun getAllEvents(): List<EventDto>

    @GET("/events/{id}")
    suspend fun getEvent(@Path("id") id: String): EventDto
}