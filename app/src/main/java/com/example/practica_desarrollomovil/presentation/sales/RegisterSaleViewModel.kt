package com.example.practica_desarrollomovil.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.practica_desarrollomovil.domain.model.Product
import com.example.practica_desarrollomovil.domain.repository.ProductRepository
import com.example.practica_desarrollomovil.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class SaleLineItem(
    val id: String = UUID.randomUUID().toString(),
    val productId: Long? = null,
    val quantity: String = ""
)

data class RegisterSaleUiState(
    val products: List<Product> = emptyList(),
    val lineItems: List<SaleLineItem> = listOf(SaleLineItem()),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
) {
    val totalAmount: Double
        get() {
            return lineItems.sumOf { line ->
                val product = products.find { it.id == line.productId } ?: return@sumOf 0.0
                val qty = line.quantity.toDoubleOrNull() ?: 0.0
                product.pricePerUnit * qty
            }
        }
}

class RegisterSaleViewModel(
    productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val formState = MutableStateFlow(RegisterSaleUiState())

    val uiState: StateFlow<RegisterSaleUiState> = combine(
        productRepository.observeAllProducts(),
        formState
    ) { products, form ->
        form.copy(products = products)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RegisterSaleUiState())

    fun addLineItem() {
        formState.update { state ->
            state.copy(lineItems = state.lineItems + SaleLineItem())
        }
    }

    fun removeLineItem(id: String) {
        formState.update { state ->
            val updated = state.lineItems.filterNot { it.id == id }
            state.copy(
                lineItems = if (updated.isEmpty()) listOf(SaleLineItem()) else updated,
                errorMessage = null
            )
        }
    }

    fun selectProduct(lineId: String, productId: Long) {
        formState.update { state ->
            state.copy(
                lineItems = state.lineItems.map { line ->
                    if (line.id == lineId) line.copy(productId = productId) else line
                },
                errorMessage = null
            )
        }
    }

    fun onQuantityChange(lineId: String, value: String) {
        formState.update { state ->
            state.copy(
                lineItems = state.lineItems.map { line ->
                    if (line.id == lineId) line.copy(quantity = value) else line
                },
                errorMessage = null
            )
        }
    }

    fun registerSale() {
        val state = uiState.value
        val validLines = state.lineItems.filter { line ->
            line.productId != null && line.quantity.toDoubleOrNull()?.let { it > 0 } == true
        }

        if (validLines.isEmpty()) {
            formState.update { it.copy(errorMessage = "Agrega al menos un producto con cantidad válida") }
            return
        }

        viewModelScope.launch {
            formState.update { it.copy(isSaving = true, errorMessage = null) }

            var lastError: String? = null
            for (line in validLines) {
                val qty = line.quantity.toDoubleOrNull() ?: continue
                saleRepository.registerSale(line.productId!!, qty)
                    .onFailure { error -> lastError = error.message }
            }

            if (lastError != null) {
                formState.update { it.copy(isSaving = false, errorMessage = lastError) }
            } else {
                formState.update {
                    it.copy(
                        isSaving = false,
                        savedSuccessfully = true,
                        lineItems = listOf(SaleLineItem())
                    )
                }
            }
        }
    }

    fun consumeSaveSuccess() {
        formState.update { it.copy(savedSuccessfully = false) }
    }

    class Factory(
        private val productRepository: ProductRepository,
        private val saleRepository: SaleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RegisterSaleViewModel(productRepository, saleRepository) as T
        }
    }
}
