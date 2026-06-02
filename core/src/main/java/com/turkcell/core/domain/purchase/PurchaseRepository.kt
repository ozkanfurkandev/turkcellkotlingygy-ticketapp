package com.turkcell.core.domain.purchase

interface PurchaseRepository {
    suspend fun createPurchase(items: Map<String, Int>): Result<Purchase>
    suspend fun pay(purchaseId: String): Result<Purchase>
    suspend fun getPurchase(purchaseId: String): Result<Purchase>
    suspend fun getMyPurchases(): Result<List<Purchase>>
}
