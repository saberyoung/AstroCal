@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yangsheng.astrocal.ui.screens.features

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.theme.AstroBackground

@Composable
fun CloudMapScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onAi: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var showLang by remember { mutableStateOf(false) }

    // ✅ 不依赖 LangPickerDialog，直接用 AlertDialog（避免签名冲突）
    if (showLang) {
        AlertDialog(
            onDismissRequest = { showLang = false },
            title = { Text("Language") },
            text = {
                Column {
                    Lang.entries.forEach { l ->
                        TextButton(
                            onClick = {
                                onLangSelected(l)
                                showLang = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(l.name) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLang = false }) { Text("OK") } }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.cloudMapTitle,
                ui = ui,
                onBack = onBack,
                onLang = { showLang = true },
                onAi = onAi,
                onClose = onClose
            )
        }
    ) { pad ->
        AstroBackground {
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(ui.cloudMapOpenInBrowser, style = MaterialTheme.typography.titleMedium)

            Button(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.windy.com/?clouds"))
                context.startActivity(intent)
            }) {
                Text(ui.cloudMapWindy)
            }

            OutlinedButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ventusky.com/?p=41.9;12.5;6&l=clouds-total"))
                context.startActivity(intent)
            }) {
                Text(ui.cloudMapVentusky)
            }
        }
            }
}
}
