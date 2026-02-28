package com.yangsheng.astrocal.ui.screens.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang

@Composable
fun LangPickerDialog(
    visible: Boolean,
    title: String,
    current: Lang,
    onDismiss: () -> Unit,
    onSelect: (Lang) -> Unit,
) {
    if (!visible) return

    val options = try {
        Lang.entries.toList()
    } catch (_: Throwable) {
        listOf(Lang.ZH_HANS, Lang.ZH_HANT, Lang.EN, Lang.IT, Lang.JA, Lang.KO)
    }

    fun labelOf(l: Lang): String = when (l) {
        Lang.ZH_HANS -> "简体中文"
        Lang.ZH_HANT -> "繁體中文"
        Lang.EN -> "English"
        Lang.IT -> "Italiano"
        Lang.JA -> "日本語"
        Lang.KO -> "한국어"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                options.forEach { l ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(l)
                                onDismiss()
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (l == current),
                            onClick = {
                                onSelect(l)
                                onDismiss()
                            }
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(labelOf(l), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        }
    )
}