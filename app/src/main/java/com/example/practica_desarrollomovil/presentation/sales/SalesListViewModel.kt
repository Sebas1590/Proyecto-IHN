package com.example.practica_desarrollomovil.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_desarrollomovil.domain.model.Sale
import com.example.practica_desarrollomovil.domain.repository.SaleRepository
import com.example.practica_desarrollomovil.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesListUiState(
    val sales: List<Sale> = emptyList(),
    val dateFrom: String = DateTimeUtils.todayDateShort(),
    val dateTo: String = DateTimeUtils.todayDateShort()
)

class SalesListViewModel(
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val filters = MutableStateFlow(
        Pair(DateTimeUtils.todayDateShort(), DateTimeUtils.todayDateShort())
    )

    val uiState: StateFlow<SalesListUiState> = combine(
        saleRepository.observeAllSales(),
        filters
    ) { allSales, (from, to) ->
        val fromMillis = DateTimeUtils.parseDateShort(from)?.let(DateTimeUtils::startOfDayMillisFor)
        val toMillis = DateTimeUtils.parseDateShort(to)?.let(DateTimeUtils::endOfDayMillisFor)

        val filtered = allSales.filter { sale ->
            val afterFrom = fromMillis?.let { sale.soldAtMillis >= it } ?: true
            val beforeTo = toMillis?.let { sale.soldAtMillis < it } ?: true
            afterFrom && beforeTo
        }

        SalesListUiState(
            sales = filtered,
            dateFrom = from,
            dateTo = to
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SalesListUiState())

    fun onDateFromChange(value: String) {
        filters.update { (_, to) -> value to to }
    }

    fun onDateToChange(value: String) {
        filters.update { (from, _) -> from to value }
    }

    fun deleteSale(id: Long) {
        viewModelScope.launch {
            saleRepository.deleteSale(id)
        }
    }

    class Factory(
        private val saleRepository: SaleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SalesListViewModel(saleRepository) as T
        }
    }
}
