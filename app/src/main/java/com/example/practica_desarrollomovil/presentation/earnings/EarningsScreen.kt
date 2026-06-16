package com.example.practica_desarrollomovil.presentation.earnings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.practica_desarrollomovil.presentation.components.MetamercaCard
import com.example.practica_desarrollomovil.presentation.theme.BrandBrown
import com.example.practica_desarrollomovil.presentation.theme.BrandOrange
import com.example.practica_desarrollomovil.presentation.theme.CreamBackground
import com.example.practica_desarrollomovil.util.CurrencyFormatter
import com.example.practica_desarrollomovil.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsScreen(viewModel: EarningsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val summary = uiState.summary
    
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    if (showFromPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateFromMillis)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateRangeSelected(datePickerState.selectedDateMillis, uiState.dateToMillis)
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.dateToMillis)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDateRangeSelected(uiState.dateFromMillis, datePickerState.selectedDateMillis)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = BrandBrown,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Ganancias",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = BrandBrown,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Text(
            text = "Análisis de rentabilidad y ventas",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Text(
            text = "Cálculo por rango personalizado",
            style = MaterialTheme.typography.titleMedium,
            color = BrandBrown,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        MetamercaCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateFilterField(
                        value = uiState.dateFromMillis?.let { DateTimeUtils.formatDateShort(it) } ?: "",
                        label = "Desde",
                        onClick = { showFromPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    DateFilterField(
                        value = uiState.dateToMillis?.let { DateTimeUtils.formatDateShort(it) } ?: "",
                        label = "Hasta",
                        onClick = { showToPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BrandOrange)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "GANANCIA TOTAL EN RANGO", 
                            color = Color.White.copy(alpha = 0.9f), 
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = CurrencyFormatter.formatSoles(uiState.filteredProfit),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Resumen de periodos", style = MaterialTheme.typography.titleMedium, color = BrandBrown)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCardWithCount(
                title = "HOY",
                value = CurrencyFormatter.formatSoles(summary.profitToday),
                count = summary.salesCountToday,
                modifier = Modifier.weight(1f)
            )
            StatCardWithCount(
                title = "ESTA SEMANA",
                value = CurrencyFormatter.formatSoles(summary.profitThisWeek),
                count = summary.salesCountThisWeek,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCardWithCount(
                title = "ESTE MES",
                value = CurrencyFormatter.formatSoles(summary.profitThisMonth),
                count = summary.salesCountThisMonth,
                modifier = Modifier.weight(1f)
            )
            Box(modifier = Modifier.weight(1f))
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
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

@Composable
private fun StatCardWithCount(
    title: String,
    value: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    MetamercaCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = BrandBrown)
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = BrandBrown,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "$count ventas",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
