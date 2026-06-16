package com.example.practica_desarrollomovil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.practica_desarrollomovil.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY soldAtMillis DESC")
    fun observeAll(): Flow<List<SaleEntity>>

    @Query(
        """
        SELECT COUNT(*) as salesCount, 
               TOTAL(totalAmount) as income, 
               TOTAL(profitAmount) as profit 
        FROM sales 
        WHERE soldAtMillis >= :startOfDay AND soldAtMillis <= :endOfDay
        """
    )
    fun observeDailyTotals(startOfDay: Long, endOfDay: Long): Flow<DailyTotalsRow>

    @Query(
        """
        SELECT COALESCE(SUM(profitAmount), 0) FROM sales 
        WHERE soldAtMillis >= :startMillis AND soldAtMillis <= :endMillis
        """
    )
    fun observeProfitBetween(startMillis: Long, endMillis: Long): Flow<Double>

    @Query(
        """
        SELECT COUNT(*) FROM sales 
        WHERE soldAtMillis >= :startMillis AND soldAtMillis <= :endMillis
        """
    )
    fun observeSalesCountBetween(startMillis: Long, endMillis: Long): Flow<Int>

    @Query(
        """
        SELECT * FROM sales 
        ORDER BY soldAtMillis DESC 
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<SaleEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM sales WHERE productId = :productId)")
    suspend fun hasSalesForProduct(productId: Long): Boolean

    @Insert
    suspend fun insert(sale: SaleEntity): Long

    @Update
    suspend fun update(sale: SaleEntity)

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): SaleEntity?

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)
}

data class DailyTotalsRow(
    val salesCount: Int,
    val income: Double?,
    val profit: Double?
)
