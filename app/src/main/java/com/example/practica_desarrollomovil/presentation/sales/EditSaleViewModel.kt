package com.example.practica_desarrollomovil.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_desarrollomovil.domain.model.Sale
import com.example.practica_desarrollomovil.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditSaleUiState(
    val sale: Sale? = null,
    val quantity: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
)

class EditSaleViewModel(
    private val saleRepository: SaleRepository,
    private val saleId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditSaleUiState())
    val uiState: StateFlow<EditSaleUiState> = _uiState.asStateFlow()

    init {
        loadSale()
    }

    private fun loadSale() {
        viewModelScope.launch {
            val sale = saleRepository.getSale(saleId)
            if (sale != null) {
                _uiState.update { 
                    it.copy(
                        sale = sale,
                        quantity = sale.quantity.toString()
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Venta no encontrada") }
            }
        }
    }

    fun onQuantityChange(value: String) = _uiState.update { it.copy(quantity = value) }

    fun saveSale() {
        val quantity = _uiState.value.quantity.toDoubleOrNull()
        if (quantity == null || quantity <= 0) {
            _uiState.update { it.copy(errorMessage = "Ingresa una cantidad válida") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            saleRepository.updateSale(saleId, quantity)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                }
        }
    }

    class Factory(
        private val saleRepository: SaleRepository,
        private val saleId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EditSaleViewModel(saleRepository, saleId) as T
        }
    }
}
