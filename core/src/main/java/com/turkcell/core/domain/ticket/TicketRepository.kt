package com.turkcell.core.domain.ticket

interface TicketRepository {
    suspend fun getMyTickets(): Result<List<UserTicket>>
    suspend fun getTicketById(id: String): Result<UserTicket>
}
