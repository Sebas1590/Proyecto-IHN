package com.example.practica_desarrollomovil.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_desarrollomovil.domain.model.Product
import com.example.practica_desarrollomovil.domain.model.ProductUnit
import com.example.practica_desarrollomovil.domain.repository.ProductRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InvestmentMode {
    TOTAL, UNIT_COST, NONE
}

data class ProductFormUiState(
    val productId: Long? = null,
    val name: String = "",
    val stock: String = "",
    val unit: ProductUnit = ProductUnit.UNID,
    val pricePerUnit: String = "",
    val investmentMode: InvestmentMode = InvestmentMode.TOTAL,
    val unitCost: String = "",
    val totalInvestment: String = "",
    val imageUri: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
)

class ProductFormViewModel(
    private val productRepository: ProductRepository,
    productId: Long?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductFormUiState(productId = productId))
    val uiState: StateFlow<ProductFormUiState> = _uiState.asStateFlow()

    init {
        productId?.let { loadProduct(it) }
    }

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val product = productRepository.getProduct(id)
            if (product != null) {
                _uiState.update {
                    it.copy(
                        name = product.name,
                        stock = product.stock.toString(),
                        unit = product.unit,
                        pricePerUnit = product.pricePerUnit.toString(),
                        totalInvestment = product.totalInvestment.toString(),
                        imageUri = product.imageUri,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Producto no encontrado")
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onStockChange(value: String) = _uiState.update { it.copy(stock = value) }
    fun onUnitChange(unit: ProductUnit) = _uiState.update { it.copy(unit = unit) }
    fun onPriceChange(value: String) = _uiState.update { it.copy(pricePerUnit = value) }
    
    fun onInvestmentModeChange(mode: InvestmentMode) = _uiState.update { it.copy(investmentMode = mode) }
    fun onUnitCostChange(value: String) = _uiState.update { it.copy(unitCost = value) }
    fun onTotalInvestmentChange(value: String) = _uiState.update { it.copy(totalInvestment = value) }
    
    fun onImageUriChange(uri: String?) = _uiState.update { it.copy(imageUri = uri) }

    fun saveProduct() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa el nombre del producto") }
            return
        }

        val stock = state.stock.toDoubleOrNull() ?: 0.0
        val price = state.pricePerUnit.toDoubleOrNull() ?: 0.0
        
        val finalInvestment = when (state.investmentMode) {
            InvestmentMode.TOTAL -> state.totalInvestment.toDoubleOrNull() ?: 0.0
            InvestmentMode.UNIT_COST -> {
                val cost = state.unitCost.toDoubleOrNull() ?: 0.0
                cost * stock
            }
            InvestmentMode.NONE -> 0.0
        }

        if (stock < 0) {
            _uiState.update { it.copy(errorMessage = "Stock inválido") }
            return
        }
        if (price < 0) {
            _uiState.update { it.copy(errorMessage = "Precio inválido") }
            return
        }

        val totalRevenue = price * stock
        if (finalInvestment > totalRevenue) {
            _uiState.update { 
                it.copy(errorMessage = "La inversión no puede ser mayor a la venta proyectada (S/ ${String.format(Locale.getDefault(), "%.2f", totalRevenue)})")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                productRepository.upsertProduct(
                    Product(
                        id = state.productId ?: 0L,
                        name = state.name,
                        stock = stock,
                        unit = state.unit,
                        pricePerUnit = price,
                        totalInvestment = finalInvestment,
                        imageUri = state.imageUri
                    )
                )
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = e.message ?: "Error al guardar")
                }
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.update { it.copy(savedSuccessfully = false) }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val productId: Long?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductFormViewModel(productRepository, productId) as T
        }
    }
}
