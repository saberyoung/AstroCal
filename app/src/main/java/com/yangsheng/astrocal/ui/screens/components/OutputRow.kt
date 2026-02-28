package com.yangsheng.astrocal.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OutputRow(
    title: String,
    value: String?,
    onCopy: (String) -> Unit
) {
    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(value ?: "—", style = MaterialTheme.typography.bodyLarge)
            }
            IconButton(
                onClick = { if (!value.isNullOrBlank()) onCopy(value) },
                enabled = !value.isNullOrBlank()
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy")
            }
        }
    }
}