package com.turkcell.data.repository

import com.turkcell.core.domain.ticket.TicketRepository
import com.turkcell.core.domain.ticket.UserTicket
import com.turkcell.data.mapper.toDomain
import com.turkcell.data.remote.TicketApi
import com.turkcell.data.util.runCatchingApi

class TicketRepositoryImpl(
    private val ticketApi: TicketApi,
) : TicketRepository {
    override suspend fun getMyTickets(): Result<List<UserTicket>> =
        runCatchingApi { ticketApi.getMyTickets() }
            .map { tickets -> tickets.map { it.toDomain() } }
}
