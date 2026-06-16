package com.example.practica_desarrollomovil.domain.repository

import com.example.practica_desarrollomovil.domain.model.DashboardSummary
import com.example.practica_desarrollomovil.domain.model.EarningsSummary
import com.example.practica_desarrollomovil.domain.model.RecentActivity
import com.example.practica_desarrollomovil.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    fun observeDashboardSummary(): Flow<DashboardSummary>
    fun observeEarningsSummary(): Flow<EarningsSummary>
    fun observeRecentActivity(limit: Int = 10): Flow<List<RecentActivity>>
    fun observeAllSales(): Flow<List<Sale>>
    suspend fun registerSale(productId: Long, quantity: Double): Result<Sale>
    suspend fun deleteSale(id: Long)
}
