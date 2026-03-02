package com.yangsheng.astrocal.ui.screens.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.ui.screens.components.OutputRow
import com.yangsheng.astrocal.util.TimeInputType
import com.yangsheng.astrocal.util.TimeUtils
import com.yangsheng.astrocal.ui.theme.AstroBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onAi: (() -> Unit)? = null,
) {
    var showLang by remember { mutableStateOf(false) }
    LangPickerDialog(
        visible = showLang,
        title = ui.chooseLang,
        current = lang,
        onDismiss = { showLang = false },
        onSelect = {
            showLang = false
            onLangSelected(it)
        }
    )

    val clipboard = LocalClipboardManager.current

    var inputType by remember { mutableStateOf(TimeInputType.ISO_UTC) }
    var inputText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showHelp by remember { mutableStateOf(false) }

    var outIso by remember { mutableStateOf<String?>(null) }
    var outJd by remember { mutableStateOf<String?>(null) }
    var outMjd by remember { mutableStateOf<String?>(null) }
    var outGps by remember { mutableStateOf<String?>(null) }

    fun clearOutputs() {
        outIso = null; outJd = null; outMjd = null; outGps = null
    }

    fun labelOf(t: TimeInputType): String = when (t) {
        TimeInputType.ISO_UTC -> "ISO UTC"
        TimeInputType.JD -> "Julian Date (JD)"
        TimeInputType.MJD -> "Modified Julian Date (MJD)"
        TimeInputType.GPS -> "GPS Time (s)"
    }

    fun helpTextFor(t: TimeInputType): String = when (t) {
        TimeInputType.ISO_UTC ->
            "请输入 ISO 8601 时间（UTC），例如：\n" +
                    "• 2026-02-28T12:34:56Z\n" +
                    "• 2026-02-28 12:34:56Z\n" +
                    "• 2026-02-28T12:34:56+00:00"
        TimeInputType.JD ->
            "请输入儒略日 JD（十进制），例如：\n• 2460370.123456"
        TimeInputType.MJD ->
            "请输入修正儒略日 MJD（十进制），例如：\n• 60369.623456\n\n关系：MJD = JD - 2400000.5"
        TimeInputType.GPS ->
            "请输入 GPS 秒（自 1980-01-06 00:00:00 起），例如：\n• 1400000000\n\n注意：GPS 与 UTC 存在闰秒差，程序使用内置闰秒表换算。"
    }

    fun compute() {
        errorText = null
        clearOutputs()
        val raw = inputText.trim()
        if (raw.isEmpty()) {
            errorText = ui.parseError
            return
        }
        try {
            val instant = TimeUtils.parseToInstantUtc(inputType, raw)
            val out = TimeUtils.computeAllOutputs(instant)
            outIso = out.isoUtc
            outJd = "%.9f".format(out.jd)
            outMjd = "%.9f".format(out.mjd)
            outGps = "%.3f".format(out.gpsSeconds)
        } catch (_: Throwable) {
            errorText = ui.parseError
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.timeTitle,
                ui = ui,
                onBack = onBack,
                onLang = { showLang = true },
                onAi = onAi,
                onClose = onClose
            )
        }
    ) { inner ->
        AstroBackground {
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(ui.timeDesc, style = MaterialTheme.typography.bodyLarge)

            // input type dropdown
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = labelOf(inputType),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(ui.input) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    TimeInputType.values().forEach { t ->
                        DropdownMenuItem(
                            text = { Text(labelOf(t)) },
                            onClick = {
                                inputType = t
                                expanded = false
                                errorText = null
                                clearOutputs()
                            }
                        )
                    }
                }
            }

            // input + help
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it; errorText = null },
                    label = { Text("Value") },
                    placeholder = { Text("Enter value…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (inputType == TimeInputType.ISO_UTC) KeyboardType.Ascii else KeyboardType.Number
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showHelp = true }, modifier = Modifier.padding(top = 6.dp)) {
                    Icon(Icons.Outlined.HelpOutline, contentDescription = "Help")
                }
            }

            if (showHelp) {
                AlertDialog(
                    onDismissRequest = { showHelp = false },
                    title = { Text("Input format") },
                    text = { Text(helpTextFor(inputType)) },
                    confirmButton = { TextButton(onClick = { showHelp = false }) { Text("OK") } }
                )
            }

            Button(
                onClick = { compute() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) { Text(ui.compute) }

            if (errorText != null) {
                Text(errorText!!, color = MaterialTheme.colorScheme.error)
            }

            if (outIso != null || outJd != null || outMjd != null || outGps != null) {
                Text(ui.output, style = MaterialTheme.typography.titleMedium)

                OutputRow("ISO UTC", outIso) { v -> clipboard.setText(AnnotatedString(v)) }
                OutputRow("JD", outJd) { v -> clipboard.setText(AnnotatedString(v)) }
                OutputRow("MJD", outMjd) { v -> clipboard.setText(AnnotatedString(v)) }
                OutputRow("GPS (s)", outGps) { v -> clipboard.setText(AnnotatedString(v)) }
            }
        }
            }
}
}
