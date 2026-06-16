package com.example.practica_desarrollomovil.presentation.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.practica_desarrollomovil.di.AppContainer
import com.example.practica_desarrollomovil.domain.model.Sale
import com.example.practica_desarrollomovil.presentation.components.MetamercaAlertDialog
import com.example.practica_desarrollomovil.presentation.components.MetamercaCard
import com.example.practica_desarrollomovil.presentation.theme.BrandBrown
import com.example.practica_desarrollomovil.presentation.theme.CancelRed
import com.example.practica_desarrollomovil.presentation.theme.CreamBackground
import com.example.practica_desarrollomovil.presentation.theme.TextSecondary
import com.example.practica_desarrollomovil.util.CurrencyFormatter
import com.example.practica_desarrollomovil.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    container: AppContainer,
    onRegisterSale: () -> Unit
) {
    val viewModel = viewModel<SalesListViewModel>(factory = container.salesListViewModelFactory())
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var saleToEdit by remember { mutableStateOf<Sale?>(null) }
    var saleToView by remember { mutableStateOf<Sale?>(null) }
    
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.parseDateShort(uiState.dateFrom)
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateFromChange(DateTimeUtils.formatDateShort(it))
                    }
                    showFromPicker = false
                }) {
                    Text("Aceptar", color = BrandBrown, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = DateTimeUtils.parseDateShort(uiState.dateTo)
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.onDateToChange(DateTimeUtils.formatDateShort(it))
                    }
                    showToPicker = false
                }) {
                    Text("Aceptar", color = BrandBrown, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    saleToView?.let { sale ->
        MetamercaAlertDialog(
            onDismissRequest = { saleToView = null },
            onConfirm = { saleToView = null },
            title = "Detalle de venta",
            text = "Producto: ${sale.productName}\nCantidad: ${sale.quantity}\nTotal: ${CurrencyFormatter.formatSoles(sale.totalAmount, withSign = true)}\nFecha: ${DateTimeUtils.formatDateShort(sale.soldAtMillis)}",
            confirmText = "Cerrar"
        )
    }

    saleToEdit?.let { sale ->
        EditSaleDialog(
            container = container,
            saleId = sale.id,
            onDismiss = { saleToEdit = null },
            onSuccess = {
                saleToEdit = null
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Ventas",
                tint = BrandBrown,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Venta",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            Button(
                onClick = onRegisterSale,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBrown,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "+ Nueva Venta", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                DateFilterField(
                    value = uiState.dateFrom,
                    label = "Desde",
                    onClick = { showFromPicker = true }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                DateFilterField(
                    value = uiState.dateTo,
                    label = "Hasta",
                    onClick = { showToPicker = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.sales.isEmpty()) {
            Text(
                text = "No hay ventas registradas para hoy.",
                color = TextSecondary,
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn {
                itemsIndexed(uiState.sales, key = { _, sale -> sale.id }) { index, sale ->
                    MetamercaCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Venta ${index + 1}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${DateTimeUtils.formatDateShort(sale.soldAtMillis)} - ${
                                        CurrencyFormatter.formatSoles(sale.totalAmount, withSign = true)
                                    }",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            IconButton(onClick = { saleToView = sale }) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Ver detalle",
                                    tint = BrandBrown
                                )
                            }
                            
                            // Solo permitir editar si el producto existe (productId != 0)
                            if (sale.productId != 0L) {
                                IconButton(onClick = { saleToEdit = sale }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar venta", tint = BrandBrown)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun EditSaleDialog(
    container: AppContainer,
    saleId: Long,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val viewModel = viewModel<EditSaleViewModel>(
        key = "edit_sale_$saleId",
        factory = container.editSaleViewModelFactory(saleId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onSuccess()
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Venta", fontWeight = FontWeight.Bold, color = BrandBrown) },
        text = {
            Column {
                Text(
                    text = "Producto: ${uiState.sale?.productName ?: ""}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BrandBrown
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = uiState.quantity,
                    onValueChange = { viewModel.onQuantityChange(it) },
                    label = { Text("Nueva Cantidad") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    isError = uiState.errorMessage != null,
                    supportingText = {
                        if (uiState.errorMessage != null) {
                            Text(uiState.errorMessage!!, color = CancelRed)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.saveSale() },
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = BrandBrown),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (uiState.isSaving) "Guardando..." else "Guardar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun DateFilterField(
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, color = BrandBrown) },
            trailingIcon = {
                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = BrandBrown)
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                disabledTextColor = BrandBrown,
                disabledBorderColor = BrandBrown,
                disabledLabelColor = BrandBrown,
                disabledTrailingIconColor = BrandBrown
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}
