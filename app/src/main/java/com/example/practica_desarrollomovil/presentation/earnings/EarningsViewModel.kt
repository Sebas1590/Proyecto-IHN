package com.example.practica_desarrollomovil.presentation.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_desarrollomovil.domain.model.EarningsSummary
import com.example.practica_desarrollomovil.domain.repository.SaleRepository
import com.example.practica_desarrollomovil.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class EarningsUiState(
    val summary: EarningsSummary = EarningsSummary(),
    val dateFromMillis: Long? = DateTimeUtils.startOfDayMillis(),
    val dateToMillis: Long? = DateTimeUtils.startOfDayMillis(),
    val filteredProfit: Double = 0.0
)

class EarningsViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _dateFrom = MutableStateFlow<Long?>(DateTimeUtils.startOfDayMillis())
    private val _dateTo = MutableStateFlow<Long?>(DateTimeUtils.startOfDayMillis())

    val uiState: StateFlow<EarningsUiState> = combine(
        saleRepository.observeEarningsSummary(),
        saleRepository.observeAllSales(),
        _dateFrom,
        _dateTo
    ) { summary, sales, from, to ->
        val start = from ?: 0L
        val end = (to ?: System.currentTimeMillis())
        
        // Normalize end to end of day
        val normalizedEnd = DateTimeUtils.endOfDayMillisFor(end)

        val filtered = sales.filter { it.soldAtMillis in start..normalizedEnd }
            .sumOf { it.profitAmount }

        EarningsUiState(
            summary = summary,
            dateFromMillis = from,
            dateToMillis = to,
            filteredProfit = filtered
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EarningsUiState())

    fun onDateRangeSelected(from: Long?, to: Long?) {
        _dateFrom.value = from
        _dateTo.value = to
    }

    class Factory(
        private val saleRepository: SaleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EarningsViewModel(saleRepository) as T
        }
    }
}
