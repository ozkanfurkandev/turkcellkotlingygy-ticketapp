package com.turkcell.ticketapp.navigation

import kotlinx.serialization.Serializable

@Serializable
object Login
@Serializable
object Register
@Serializable
object Home
@Serializable
object MyPurchases
@Serializable
object Staff
@Serializable
data class EventDetail(val id: String)
@Serializable
data class TicketDetail(val ticketId: String)