package com.yangsheng.astrocal.ui.screens.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.util.SphericalUtils

@Composable
fun AngleScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    var showLang by remember { mutableStateOf(false) }

    LangPickerDialog(
        visible = showLang,
        title = ui.chooseLang,
        current = lang,
        onDismiss = { showLang = false },
        onSelect = onLangSelected
    )

    var ra1 by remember { mutableStateOf("") }
    var dec1 by remember { mutableStateOf("") }
    var ra2 by remember { mutableStateOf("") }
    var dec2 by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.angleTitle,
                ui = ui,
                onBack = onBack,
                onLang = { showLang = true },
                onClose = onClose
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumField(value = ra1, onValueChange = { ra1 = it; error = null }, label = ui.ra1, modifier = Modifier.weight(1f))
                NumField(value = dec1, onValueChange = { dec1 = it; error = null }, label = ui.dec1, modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumField(value = ra2, onValueChange = { ra2 = it; error = null }, label = ui.ra2, modifier = Modifier.weight(1f))
                NumField(value = dec2, onValueChange = { dec2 = it; error = null }, label = ui.dec2, modifier = Modifier.weight(1f))
            }

            Button(
                onClick = {
                    val a1 = ra1.toDoubleOrNull()
                    val d1 = dec1.toDoubleOrNull()
                    val a2 = ra2.toDoubleOrNull()
                    val d2 = dec2.toDoubleOrNull()
                    if (a1 == null || d1 == null || a2 == null || d2 == null) {
                        error = ui.parseError
                        result = null
                    } else {
                        val sepDeg = SphericalUtils.angularSeparationDeg(a1, d1, a2, d2)
                        result = "${ui.sep}: ${"%.6f".format(sepDeg)} deg"
                        error = null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) { Text(ui.compute) }

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            if (result != null) {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Text(result!!, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun NumField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = modifier
    )
}