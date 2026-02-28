package com.yangsheng.astrocal.ui.screens.features

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

enum class MarkerShape { CIRCLE, TRIANGLE, DIAMOND, SQUARE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinderScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    fun t(key: String): String = when (lang) {
        Lang.ZH_HANS -> when (key) {
            "title" -> "寻星图"
            "subtitle" -> "PS1 影像 + Gaia DR3"
            "ra" -> "赤经（度）"
            "dec" -> "赤纬（度）"
            "fov" -> "视场（角分）"
            "rad" -> "星表半径（角分）"
            "filter" -> "波段"
            "gen" -> "生成"
            "loading" -> "加载中..."
            "copy_center" -> "复制中心坐标"
            "copy_csv" -> "复制星表（前50）"
            "no_src" -> "未检索到盖亚源：可尝试增大半径"
            "src_n" -> "Gaia DR3：共 %d 个源"
            "fmt_err" -> "解析失败：请检查格式"
            "overlay" -> "标记样式"
            "shape" -> "形状"
            "color" -> "颜色"
            else -> key
        }
        else -> key
    }

    var showLang by remember { mutableStateOf(false) }
    LangPickerDialog(
        visible = showLang,
        title = ui.chooseLang,
        current = lang,
        onDismiss = { showLang = false },
        onSelect = { showLang = false; onLangSelected(it) }
    )

    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var ra by remember { mutableStateOf("") }
    var dec by remember { mutableStateOf("") }
    var fovArcmin by remember { mutableStateOf("1.0") }
    var radiusArcmin by remember { mutableStateOf("2.0") }

    var filter by remember { mutableStateOf("r") }
    val filters = listOf("g", "r", "i", "z", "y")

    var markerShape by remember { mutableStateOf(MarkerShape.CIRCLE) }
    var markerColorName by remember { mutableStateOf("Green") }
    val colorOptions = listOf("Green", "Red", "Blue", "Yellow", "Cyan", "Magenta", "White")
    fun markerColor(): Color = when (markerColorName) {
        "Red" -> Color.Red
        "Blue" -> Color.Blue
        "Yellow" -> Color.Yellow
        "Cyan" -> Color.Cyan
        "Magenta" -> Color.Magenta
        "White" -> Color.White
        else -> Color(0xFF00C853)
    }

    var loading by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }

    var bmp by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var stars by remember { mutableStateOf<List<GaiaStar>>(emptyList()) }
    var wcs by remember { mutableStateOf<WcsHeader?>(null) }

    var debug by remember { mutableStateOf(false) }
    var dbgMsg by remember { mutableStateOf("") }
    var yFlip by remember { mutableStateOf(true) }

    fun run() {
        err = null
        bmp = null
        stars = emptyList()
        wcs = null

        val raDeg = ra.trim().toDoubleOrNull()
        val decDeg = dec.trim().toDoubleOrNull()
        val fov = fovArcmin.trim().toDoubleOrNull()
        val rad = radiusArcmin.trim().toDoubleOrNull()
        if (raDeg == null || decDeg == null || fov == null || rad == null) {
            err = t("fmt_err"); return
        }

        loading = true
        scope.launch {
            try {
                val req = Ps1ImageRequest(raDeg, decDeg, fov, filter)
                val imgDeferred = async(Dispatchers.IO) {
                    val bytes = Ps1Service.downloadPng(req)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
                val wcsDeferred = async(Dispatchers.IO) {
                    val fitsBytes = Ps1Service.downloadFitsHeader(req)
                    WcsUtils.parseFitsHeader(fitsBytes)
                }
                val catDeferred = async(Dispatchers.IO) {
                    VizierGaiaService.coneSearchGaiaDr3(raDeg, decDeg, rad, maxRows = 500)
                }

                bmp = imgDeferred.await()
                wcs = wcsDeferred.await()
                stars = catDeferred.await()
                dbgMsg = "wcs=${wcs != null} stars=${stars.size}"
            } catch (th: Throwable) {
                err = th.message ?: t("fmt_err")
            } finally {
                loading = false
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = t("title"), ui = ui, onBack = onBack, onLang = { showLang = true }, onClose = onClose) }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(t("subtitle"), style = MaterialTheme.typography.bodyMedium) }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ra, onValueChange = { ra = it; err = null },
                        label = { Text(t("ra")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = dec, onValueChange = { dec = it; err = null },
                        label = { Text(t("dec")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fovArcmin, onValueChange = { fovArcmin = it; err = null },
                        label = { Text(t("fov")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = radiusArcmin, onValueChange = { radiusArcmin = it; err = null },
                        label = { Text(t("rad")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                var expandedFilter by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedFilter, onExpandedChange = { expandedFilter = !expandedFilter }) {
                    OutlinedTextField(
                        value = filter,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(t("filter")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFilter) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedFilter, onDismissRequest = { expandedFilter = false }) {
                        filters.forEach { f ->
                            DropdownMenuItem(text = { Text(f) }, onClick = { filter = f; expandedFilter = false })
                        }
                    }
                }
            }

            item {
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(t("overlay"), style = MaterialTheme.typography.titleSmall)

                        var expandedShape by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expandedShape, onExpandedChange = { expandedShape = !expandedShape }) {
                            OutlinedTextField(
                                value = markerShape.name, onValueChange = {},
                                readOnly = true, label = { Text(t("shape")) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedShape) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedShape, onDismissRequest = { expandedShape = false }) {
                                MarkerShape.values().forEach { s ->
                                    DropdownMenuItem(text = { Text(s.name) }, onClick = { markerShape = s; expandedShape = false })
                                }
                            }
                        }

                        var expandedColor by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expandedColor, onExpandedChange = { expandedColor = !expandedColor }) {
                            OutlinedTextField(
                                value = markerColorName, onValueChange = {},
                                readOnly = true, label = { Text(t("color")) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedColor) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = expandedColor, onDismissRequest = { expandedColor = false }) {
                                colorOptions.forEach { c ->
                                    DropdownMenuItem(text = { Text(c) }, onClick = { markerColorName = c; expandedColor = false })
                                }
                            }
                        }

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Debug")
                            Spacer(Modifier.weight(1f))
                            Switch(checked = debug, onCheckedChange = { debug = it })
                        }
                        if (debug) {
                            TextButton(onClick = { yFlip = !yFlip }) { Text("Toggle Y-Flip") }
                            Text(dbgMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { run() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    enabled = !loading
                ) { Text(if (loading) t("loading") else t("gen")) }
            }

            item { if (err != null) Text(err!!, color = MaterialTheme.colorScheme.error) }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString("RA=${ra.trim()}, Dec=${dec.trim()}")) },
                        modifier = Modifier.weight(1f)
                    ) { Text(t("copy_center")) }

                    OutlinedButton(
                        onClick = {
                            val csv = buildString {
                                append("ra_deg,dec_deg,gmag\n")
                                stars.take(50).forEach { s -> append("${s.raDeg},${s.decDeg},${s.gmag ?: ""}\n") }
                            }
                            clipboard.setText(AnnotatedString(csv))
                        },
                        enabled = stars.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) { Text(t("copy_csv")) }
                }
            }

            item {
                if (bmp != null) {
                    ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                        Box(Modifier.fillMaxWidth().aspectRatio(1f)) {
                            Image(bmp!!.asImageBitmap(), contentDescription = "PS1", modifier = Modifier.fillMaxSize())
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val col = markerColor()
                                val cx = size.width / 2f
                                val cy = size.height / 2f
                                drawLine(col, Offset(cx - 22f, cy), Offset(cx + 22f, cy), strokeWidth = 2.5f)
                                drawLine(col, Offset(cx, cy - 22f), Offset(cx, cy + 22f), strokeWidth = 2.5f)

                                val header = wcs
                                if (header != null && stars.isNotEmpty()) {
                                    var plotted = 0
                                    var out = 0
                                    stars.take(300).forEach { s ->
                                        val (x0, y0) = WcsUtils.worldToPixel(header, s.raDeg, s.decDeg)
                                        if (!WcsUtils.isInside(header, x0, y0)) { out++; return@forEach }
                                        val y = if (yFlip) (header.naxis2 - 1).toDouble() - y0 else y0
                                        val sx = (x0 / (header.naxis1 - 1).toDouble()) * size.width
                                        val sy = (y / (header.naxis2 - 1).toDouble()) * size.height
                                        drawMarker(markerShape, col, Offset(sx.toFloat(), sy.toFloat()), 10f)
                                        plotted++
                                    }
                                    dbgMsg = "plotted=$plotted out=$out yFlip=$yFlip"
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (stars.isNotEmpty()) Text(String.format(t("src_n"), stars.size))
                else if (!loading && bmp != null) Text(t("no_src"), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(stars.take(50)) { s ->
                ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("RA ${"%.6f".format(s.raDeg)}  Dec ${"%.6f".format(s.decDeg)}")
                            Text("G=${s.gmag ?: "?"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { clipboard.setText(AnnotatedString("${s.raDeg}, ${s.decDeg}")) }) { Text("Copy") }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMarker(shape: MarkerShape, color: Color, center: Offset, r: Float) {
    when (shape) {
        MarkerShape.CIRCLE -> drawCircle(color = color, radius = r, center = center, style = Stroke(width = 2.5f))
        MarkerShape.SQUARE -> drawRect(
            color = color,
            topLeft = Offset(center.x - r, center.y - r),
            size = Size(2 * r, 2 * r),
            style = Stroke(width = 2.5f)
        )
        MarkerShape.DIAMOND -> {
            val p = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x + r, center.y)
                lineTo(center.x, center.y + r)
                lineTo(center.x - r, center.y)
                close()
            }
            drawPath(p, color = color, style = Stroke(width = 2.5f))
        }
        MarkerShape.TRIANGLE -> {
            val p = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x + r, center.y + r)
                lineTo(center.x - r, center.y + r)
                close()
            }
            drawPath(p, color = color, style = Stroke(width = 2.5f))
        }
    }
}