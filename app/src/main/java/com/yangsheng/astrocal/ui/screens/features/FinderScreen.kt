package com.yangsheng.astrocal.ui.screens.features

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.util.GaiaDr3
import com.yangsheng.astrocal.util.Ps1Client
import com.yangsheng.astrocal.util.WcsHeader
import com.yangsheng.astrocal.util.WcsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.*
import com.yangsheng.astrocal.ui.theme.AstroBackground

@OptIn(ExperimentalMaterial3Api::class)
private enum class FinderOutput { PNG, FITS }
@Composable
fun FinderScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit = onBack,
    onAi: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
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

    // Inputs
    var raText by remember { mutableStateOf("0.0") }
    var decText by remember { mutableStateOf("0.0") }
    var fovArcminText by remember { mutableStateOf("6.0") }
    var pixscaleText by remember { mutableStateOf("0.25") } // arcsec/px

    var output by remember { mutableStateOf(FinderOutput.PNG) }

    // PNG options
    var pngColor by remember { mutableStateOf(true) }
    var band by remember { mutableStateOf(Ps1Client.Band.R) }

    // FITS options
    var overlayGaia by remember { mutableStateOf(true) }
    var gaiaRadiusArcminText by remember { mutableStateOf("3.0") }

    // Output state
    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }

    var pngBytes by remember { mutableStateOf<ByteArray?>(null) }
    var fitsBytes by remember { mutableStateOf<ByteArray?>(null) }

    // (Optional) preview bitmap
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    fun parseDouble(s: String): Double? = s.trim().toDoubleOrNull()

    suspend fun compute() {
        err = null
        pngBytes = null
        fitsBytes = null
        bitmap = null

        val ra = parseDouble(raText) ?: run { err = ui.parseError; return }
        val dec = parseDouble(decText) ?: run { err = ui.parseError; return }
        val fovArcmin = parseDouble(fovArcminText) ?: run { err = ui.parseError; return }
        val pixscale = parseDouble(pixscaleText) ?: run { err = ui.parseError; return }

        loading = true
        try {
            if (output == FinderOutput.PNG) {
                val mode = if (pngColor) {
                    Ps1Client.PngMode.ColorGRI
                } else {
                    Ps1Client.PngMode.Single(band)
                }
                val bytes = withContext(Dispatchers.IO) {
                    Ps1Client.downloadPng(
                        raDeg = ra,
                        decDeg = dec,
                        fovArcmin = fovArcmin,
                        pixscaleArcsecPerPx = pixscale,
                        mode = mode
                    )
                }
                pngBytes = bytes
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } else {
                val bytes = withContext(Dispatchers.IO) {
                    Ps1Client.downloadFits(
                        raDeg = ra,
                        decDeg = dec,
                        fovArcmin = fovArcmin,
                        pixscaleArcsecPerPx = pixscale,
                        band = band
                    )
                }
                fitsBytes = bytes

                // 你后面已经有 FITS/WCS + Gaia overlay 的实现（WcsHeader/WcsUtils + GaiaDr3）
                // 这里先只保留“下载 FITS”的主链路，overlay 部分后续我们再做成更专业版本（compass/grid一起做）
                // 目前先保证 Finder 全链路稳定可用、可编译、可跑通。
            }
        } catch (t: Throwable) {
            err = (t.message ?: t.toString())
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.finderTitle,
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
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Inputs
            OutlinedTextField(
                value = raText,
                onValueChange = { raText = it },
                label = { Text("RA (deg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = decText,
                onValueChange = { decText = it },
                label = { Text("Dec (deg)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fovArcminText,
                    onValueChange = { fovArcminText = it },
                    label = { Text("FOV (arcmin)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = pixscaleText,
                    onValueChange = { pixscaleText = it },
                    label = { Text("Pixscale (arcsec/px)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f)
                )
            }

            // Output type
            Text(ui.output, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilterChip(
                    selected = output == FinderOutput.PNG,
                    onClick = { output = FinderOutput.PNG },
                    label = { Text("PNG") }
                )
                FilterChip(
                    selected = output == FinderOutput.FITS,
                    onClick = { output = FinderOutput.FITS },
                    label = { Text("FITS") }
                )
            }

            // Band chooser (common for single-band PNG / FITS)
            var bandMenu by remember { mutableStateOf(false) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Band:", modifier = Modifier.padding(top = 14.dp))
                Box {
                    OutlinedButton(onClick = { bandMenu = true }) { Text(band.name) }
                    DropdownMenu(expanded = bandMenu, onDismissRequest = { bandMenu = false }) {
                        Ps1Client.Band.values().forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name) },
                                onClick = { band = b; bandMenu = false }
                            )
                        }
                    }
                }
            }

            // PNG options
            if (output == FinderOutput.PNG) {
                Text("PNG options", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilterChip(
                        selected = pngColor,
                        onClick = { pngColor = true },
                        label = { Text("Color (g/r/i)") }
                    )
                    FilterChip(
                        selected = !pngColor,
                        onClick = { pngColor = false },
                        label = { Text("Single band") }
                    )
                }
                if (pngColor) {
                    Text("Color uses: blue=g, green=r, red=i")
                } else {
                    Text("Single-band uses selected Band above")
                }
            }

            // FITS options
            if (output == FinderOutput.FITS) {
                Text("FITS options", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = overlayGaia, onCheckedChange = { overlayGaia = it })
                    Spacer(Modifier.width(8.dp))
                    Text("Overlay Gaia (WCS)")
                }
                if (overlayGaia) {
                    OutlinedTextField(
                        value = gaiaRadiusArcminText,
                        onValueChange = { gaiaRadiusArcminText = it },
                        label = { Text("Gaia radius (arcmin)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text("FITS cutout includes WCS → suitable for professional overlay/compass/grid later.")
            }

            // Compute button
            Button(
                onClick = { scope.launch { compute() } },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (loading) ui.weatherLoading else ui.compute)
            }

            if (err != null) {
                Text(err!!, color = MaterialTheme.colorScheme.error)
            }

            // Preview
            if (bitmap != null) {
                Text("Preview", style = MaterialTheme.typography.titleMedium)
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = "PS1 image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                )
            }

            if (fitsBytes != null) {
                Text("FITS downloaded: ${fitsBytes!!.size} bytes")
                Text("Next step: WCS decode + Gaia overlay + compass/grid/scalebar.")
            }
        }
            }
}
}
