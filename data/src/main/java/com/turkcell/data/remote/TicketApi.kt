package com.turkcell.data.remote

import com.turkcell.data.dto.ticket.UserTicketDto
import retrofit2.http.GET

interface TicketApi {
    @GET("/me/tickets")
    suspend fun getMyTickets(): List<UserTicketDto>
}
