package com.example.practica_desarrollomovil.data.repository

import com.example.practica_desarrollomovil.data.local.dao.ProductDao
import com.example.practica_desarrollomovil.data.local.dao.SaleDao
import com.example.practica_desarrollomovil.data.local.entity.SaleEntity
import com.example.practica_desarrollomovil.data.mapper.SaleMapper
import com.example.practica_desarrollomovil.domain.model.ActivityType
import com.example.practica_desarrollomovil.domain.model.DashboardSummary
import com.example.practica_desarrollomovil.domain.model.EarningsSummary
import com.example.practica_desarrollomovil.domain.model.RecentActivity
import com.example.practica_desarrollomovil.domain.model.Sale
import com.example.practica_desarrollomovil.domain.repository.SaleRepository
import com.example.practica_desarrollomovil.util.CurrencyFormatter
import com.example.practica_desarrollomovil.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class SaleRepositoryImpl(
    private val saleDao: SaleDao,
    private val productDao: ProductDao
) : SaleRepository {

    override fun observeDashboardSummary(): Flow<DashboardSummary> =
        saleDao.observeDailyTotals(
            DateTimeUtils.startOfDayMillis(),
            DateTimeUtils.endOfDayMillis()
        ).map { row ->
            DashboardSummary(
                salesCountToday = row.salesCount,
                incomeToday = row.income ?: 0.0,
                netProfitToday = row.profit ?: 0.0
            )
        }

    override fun observeEarningsSummary(): Flow<EarningsSummary> {
        val weekStart = DateTimeUtils.startOfWeekMillis()
        val monthStart = DateTimeUtils.startOfMonthMillis()
        val dayStart = DateTimeUtils.startOfDayMillis()
        val now = System.currentTimeMillis()

        return combine(
            saleDao.observeProfitBetween(dayStart, now),
            saleDao.observeProfitBetween(weekStart, now),
            saleDao.observeProfitBetween(monthStart, now),
            saleDao.observeSalesCountBetween(dayStart, now),
            saleDao.observeSalesCountBetween(weekStart, now),
            saleDao.observeSalesCountBetween(monthStart, now)
        ) { results: Array<Any> ->
            EarningsSummary(
                profitToday = results[0] as Double,
                profitThisWeek = results[1] as Double,
                profitThisMonth = results[2] as Double,
                salesCountToday = results[3] as Int,
                salesCountThisWeek = results[4] as Int,
                salesCountThisMonth = results[5] as Int
            )
        }
    }

    override fun observeRecentActivity(limit: Int): Flow<List<RecentActivity>> =
        combine(
            saleDao.observeRecent(limit),
            productDao.observeAll()
        ) { sales, products ->
            val saleActivities = sales.map { sale ->
                RecentActivity(
                    id = "sale_${sale.id}",
                    type = ActivityType.SALE,
                    title = "Venta #${sale.id}",
                    subtitle = DateTimeUtils.formatTime(sale.soldAtMillis),
                    detail = CurrencyFormatter.formatSoles(sale.totalAmount, withSign = true),
                    timestampMillis = sale.soldAtMillis
                )
            }
            val stockActivities = products
                .sortedByDescending { it.updatedAtMillis }
                .take(limit)
                .map { product ->
                    RecentActivity(
                        id = "stock_${product.id}",
                        type = ActivityType.STOCK,
                        title = "Stock: ${product.name}",
                        subtitle = DateTimeUtils.formatTime(product.updatedAtMillis),
                        detail = "+${product.stock.toInt()} ${product.unit}",
                        timestampMillis = product.updatedAtMillis
                    )
                }
            (saleActivities + stockActivities)
                .sortedByDescending { it.timestampMillis }
                .take(limit)
        }

    override fun observeAllSales(): Flow<List<Sale>> =
        saleDao.observeAll().map { list -> list.map(SaleMapper::toDomain) }

    override suspend fun registerSale(productId: Long, quantity: Double): Result<Sale> {
        val product = productDao.getById(productId)
            ?: return Result.failure(IllegalArgumentException("Producto no encontrado"))

        if (quantity <= 0) {
            return Result.failure(IllegalArgumentException("Cantidad inválida"))
        }
        if (quantity > product.stock) {
            return Result.failure(IllegalArgumentException("Stock insuficiente"))
        }

        val costPerUnit = if (product.stock > 0) product.totalInvestment / product.stock else 0.0
        val unitPrice = product.pricePerUnit
        val totalAmount = unitPrice * quantity
        val profitAmount = (unitPrice - costPerUnit) * quantity
        val now = System.currentTimeMillis()

        val saleEntity = SaleEntity(
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = totalAmount,
            profitAmount = profitAmount,
            soldAtMillis = now
        )
        val saleId = saleDao.insert(saleEntity)

        val newStock = product.stock - quantity
        productDao.updateStock(product.id, newStock, now)

        val sale = Sale(
            id = saleId,
            productId = product.id,
            productName = product.name,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = totalAmount,
            profitAmount = profitAmount,
            soldAtMillis = now
        )
        return Result.success(sale)
    }

    override suspend fun deleteSale(id: Long) {
        saleDao.deleteById(id)
    }
}
