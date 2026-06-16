package com.example.practica_desarrollomovil.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_desarrollomovil.domain.model.Product
import com.example.practica_desarrollomovil.domain.model.ProductUnit
import com.example.practica_desarrollomovil.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductFormUiState(
    val productId: Long? = null,
    val name: String = "",
    val stock: String = "0",
    val unit: ProductUnit = ProductUnit.UNID,
    val pricePerUnit: String = "0",
    val totalInvestment: String = "0",
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
    fun onInvestmentChange(value: String) = _uiState.update { it.copy(totalInvestment = value) }
    fun onImageUriChange(uri: String?) = _uiState.update { it.copy(imageUri = uri) }

    fun saveProduct() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa el nombre del producto") }
            return
        }

        val stock = state.stock.toDoubleOrNull()
        val price = state.pricePerUnit.toDoubleOrNull()
        val investment = state.totalInvestment.toDoubleOrNull()

        if (stock == null || stock < 0) {
            _uiState.update { it.copy(errorMessage = "Stock inválido") }
            return
        }
        if (price == null || price < 0) {
            _uiState.update { it.copy(errorMessage = "Precio inválido") }
            return
        }
        if (investment == null || investment < 0) {
            _uiState.update { it.copy(errorMessage = "Inversión inválida") }
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
                        totalInvestment = investment,
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
