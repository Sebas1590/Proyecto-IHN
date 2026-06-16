package com.example.practica_desarrollomovil.presentation.sales

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.practica_desarrollomovil.domain.model.Product
import com.example.practica_desarrollomovil.presentation.components.MetamercaAlertDialog
import com.example.practica_desarrollomovil.presentation.components.MetamercaCard
import com.example.practica_desarrollomovil.presentation.components.MetamercaSnackbarHost
import com.example.practica_desarrollomovil.presentation.theme.BrandBrown
import com.example.practica_desarrollomovil.presentation.theme.CancelRed
import com.example.practica_desarrollomovil.presentation.theme.CreamBackground
import com.example.practica_desarrollomovil.presentation.theme.TextSecondary
import com.example.practica_desarrollomovil.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSaleScreen(
    viewModel: RegisterSaleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showExitDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    val handleBack = {
        if (uiState.lineItems.any { it.productId != -1L }) {
            showExitDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(onBack = handleBack)

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            snackbarHostState.showSnackbar("Venta registrada correctamente")
            kotlinx.coroutines.delay(1000)
            viewModel.consumeSaveSuccess()
            onBack()
        }
    }

    if (showExitDialog) {
        MetamercaAlertDialog(
            onDismissRequest = { showExitDialog = false },
            onConfirm = onBack,
            title = "Cancelar venta",
            text = "¿Deseas cancelar el registro de esta venta? Se perderán los datos ingresados.",
            confirmText = "Salir",
            isDestructive = true
        )
    }

    if (showSaveDialog) {
        MetamercaAlertDialog(
            onDismissRequest = { showSaveDialog = false },
            onConfirm = {
                showSaveDialog = false
                viewModel.registerSale()
            },
            title = "Confirmar venta",
            text = "¿Estás seguro de registrar esta venta por un total de ${CurrencyFormatter.formatSoles(uiState.totalAmount)}?",
            confirmText = "Registrar"
        )
    }

    Scaffold(
        snackbarHost = { MetamercaSnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = BrandBrown)
                        Text(
                            text = "Agregar venta",
                            modifier = Modifier.padding(start = 8.dp),
                            color = BrandBrown,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier.semantics { contentDescription = "Volver a ventas" }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BrandBrown)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CreamBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            MetamercaCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    uiState.lineItems.forEach { line ->
                        SaleLineRow(
                            line = line,
                            products = uiState.products,
                            onProductSelected = { productId ->
                                viewModel.selectProduct(line.id, productId)
                            },
                            onQuantityChange = { qty -> viewModel.onQuantityChange(line.id, qty) },
                            onRemove = { viewModel.removeLineItem(line.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedButton(
                        onClick = viewModel::addLineItem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Agregar otro producto a la venta" },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandBrown)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = BrandBrown)
                        Text(
                            text = " Agregar Producto",
                            color = BrandBrown,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamBackground)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = CurrencyFormatter.formatSoles(uiState.totalAmount, withSign = true),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    uiState.errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = CancelRed,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showSaveDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Guardar venta" },
                            enabled = !uiState.isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBrown,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Text(
                                text = " Guardar",
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Button(
                            onClick = handleBack,
                            modifier = Modifier
                                .weight(1f)
                                .semantics { contentDescription = "Cancelar venta" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CancelRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                            Text(
                                text = " Cancelar",
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleLineRow(
    line: SaleLineItem,
    products: List<Product>,
    onProductSelected: (Long) -> Unit,
    onQuantityChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember(line.id) { mutableStateOf(false) }
    val selectedProduct = products.find { it.id == line.productId }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1.4f)
        ) {
            OutlinedTextField(
                value = selectedProduct?.name ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .semantics { contentDescription = "Seleccionar producto" },
                placeholder = { Text("Escriba un producto", color = TextSecondary) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (products.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay productos") },
                        onClick = { expanded = false }
                    )
                } else {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "${product.name} (${product.stock.toInt()} ${product.unit.label})",
                                    color = BrandBrown
                                )
                            },
                            onClick = {
                                onProductSelected(product.id)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = line.quantity,
            onValueChange = onQuantityChange,
            modifier = Modifier
                .weight(0.7f)
                .semantics { contentDescription = "Cantidad del producto" },
            placeholder = { Text("Cant.") },
            suffix = {
                selectedProduct?.let {
                    Text(it.unit.label, color = BrandBrown)
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CancelRed)
                .semantics { contentDescription = "Eliminar producto de la venta" }
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
        }
    }
}
