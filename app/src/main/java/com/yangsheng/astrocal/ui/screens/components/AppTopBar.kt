@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yangsheng.astrocal.ui.screens.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.yangsheng.astrocal.ui.i18n.UiStrings

@Composable
fun AppTopBar(
    title: String,
    ui: UiStrings,
    onBack: (() -> Unit)? = null,
    onLang: (() -> Unit)? = null,
    onAi: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    var showExitConfirm by remember { mutableStateOf(false) }

    if (showExitConfirm && onClose != null) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(ui.exitTitle) },
            text = { Text(ui.exitConfirmText) },
            confirmButton = {
                TextButton(onClick = {
                    showExitConfirm = false
                    onClose()
                }) { Text(ui.yes) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) { Text(ui.cancel) }
            }
        )
    }

    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            if (onLang != null) {
                IconButton(onClick = onLang) {
                    Icon(Icons.Outlined.Language, contentDescription = "Language")
                }
            }
            if (onAi != null) {
                IconButton(onClick = onAi) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI")
                }
            }
            if (onClose != null) {
                IconButton(onClick = { showExitConfirm = true }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close")
                }
            }
        }
    )
}