package com.yangsheng.astrocal.ui.screens.features

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.theme.AstroBackground
import com.yangsheng.astrocal.util.OacApiClient
import kotlinx.coroutines.launch
import kotlin.math.*

/** Result row shown in list */
data class SnRow(
    val name: String,
    val claimedType: String? = null,
    val redshift: String? = null,
    val raDeg: Double? = null,
    val decDeg: Double? = null,
    val distArcsec: Double? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupernovaScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onAi: () -> Unit
) {
    var showLang by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Tabs
    val tabs = listOf(ui.supernovaTabByName, ui.supernovaTabCone, ui.supernovaTabClaimedType)
    var tabIndex by remember { mutableStateOf(0) }

    // Inputs
    var name by remember { mutableStateOf("2020faa") }

    var ra by remember { mutableStateOf("150.0") }
    var dec by remember { mutableStateOf("2.0") }
    var radiusArcsec by remember { mutableStateOf("120") }
    var nLimitCone by remember { mutableStateOf("30") }

    var claimedType by remember { mutableStateOf("Ia") }
    var nLimitType by remember { mutableStateOf("50") }

    // State
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var rows by remember { mutableStateOf<List<SnRow>>(emptyList()) }

    // Detail dialog
    var detailTitle by remember { mutableStateOf("") }
    var detailRaw by remember { mutableStateOf("") }
    var showDetail by remember { mutableStateOf(false) }

    fun parseIntSafe(s: String, fallback: Int): Int =
        s.trim().toIntOrNull()?.coerceIn(1, 500) ?: fallback

    fun parseRaToDeg(s: String): Double? {
        val t = s.trim()
        if (t.isEmpty()) return null
        t.toDoubleOrNull()?.let { return it }
        val parts = t.split(":")
        if (parts.size < 2) return null
        val h = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
        val m = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val sec = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
        val hours = h + m / 60.0 + sec / 3600.0
        return (hours * 15.0 + 360.0) % 360.0
    }

    fun parseDecToDeg(s: String): Double? {
        val t = s.trim()
        if (t.isEmpty()) return null
        t.toDoubleOrNull()?.let { return it }
        val sign = if (t.startsWith("-")) -1.0 else 1.0
        val clean = t.removePrefix("+").removePrefix("-")
        val parts = clean.split(":")
        if (parts.size < 2) return null
        val d = parts.getOrNull(0)?.toDoubleOrNull() ?: return null
        val m = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val sec = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
        val deg = d + m / 60.0 + sec / 3600.0
        return sign * deg
    }

    fun angSepArcsec(ra1Deg: Double, dec1Deg: Double, ra2Deg: Double, dec2Deg: Double): Double {
        val ra1 = Math.toRadians(ra1Deg)
        val dec1 = Math.toRadians(dec1Deg)
        val ra2 = Math.toRadians(ra2Deg)
        val dec2 = Math.toRadians(dec2Deg)
        val cosd = sin(dec1) * sin(dec2) + cos(dec1) * cos(dec2) * cos(ra1 - ra2)
        val d = acos(cosd.coerceIn(-1.0, 1.0))
        return Math.toDegrees(d) * 3600.0
    }

    /** Robust TSV -> rows map with soft column matching */
    fun parseTsvToMaps(tsv: String): List<Map<String, String>> {
        val lines = tsv
            .split("\n")
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
        if (lines.size < 2) return emptyList()

        val header = lines.first().split("\t").map { it.trim() }
        val lower = header.map { it.lowercase() }

        fun idxOf(vararg candidates: String): Int? {
            for (c in candidates) {
                val i = lower.indexOf(c.lowercase())
                if (i >= 0) return i
            }
            return null
        }

        val idxName = idxOf("name", "object", "obj", "event") ?: 0
        val idxType = idxOf("claimedtype", "type", "spectype")
        val idxZ = idxOf("redshift", "z")
        val idxRa = idxOf("ra", "ra_deg")
        val idxDec = idxOf("dec", "dec_deg")

        fun get(cols: List<String>, idx: Int?): String =
            if (idx == null) "" else cols.getOrNull(idx)?.trim().orEmpty()

        return lines.drop(1).mapNotNull { line ->
            val cols = line.split("\t")
            val nm = get(cols, idxName)
            if (nm.isBlank()) return@mapNotNull null
            mapOf(
                "name" to nm,
                "type" to get(cols, idxType),
                "z" to get(cols, idxZ),
                "ra" to get(cols, idxRa),
                "dec" to get(cols, idxDec)
            )
        }
    }

    fun launchQuery(block: suspend () -> Unit) {
        error = null
        loading = true
        scope.launch {
            try {
                block()
            } catch (t: Throwable) {
                error = t.message ?: ui.commonUnknown
            } finally {
                loading = false
            }
        }
    }

    suspend fun openDetail(snName: String) {
        val raw = OacApiClient.queryByName(snName)
        detailTitle = snName
        detailRaw = raw
        showDetail = true
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.supernovaTitle,
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
                TabRow(selectedTabIndex = tabIndex) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = tabIndex == idx,
                            onClick = { tabIndex = idx },
                            text = { Text(title) }
                        )
                    }
                }

                when (tabIndex) {
                    // By Name
                    0 -> Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(ui.supernovaByNameTitle, style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(ui.supernovaNameLabel) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    launchQuery {
                                        rows = emptyList()
                                        openDetail(name.trim())
                                    }
                                },
                                enabled = !loading
                            ) {
                                Text(if (loading) ui.commonLoading else ui.commonQuery)
                            }
                        }
                    }

                    // Cone Search (with N + distance arcsec)
                    1 -> {
                        Card {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(ui.supernovaConeTitle, style = MaterialTheme.typography.titleMedium)

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = ra,
                                        onValueChange = { ra = it },
                                        label = { Text(ui.supernovaRaLabel) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = dec,
                                        onValueChange = { dec = it },
                                        label = { Text(ui.supernovaDecLabel) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = radiusArcsec,
                                        onValueChange = { radiusArcsec = it },
                                        label = { Text(ui.supernovaRadiusArcsecLabel) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = nLimitCone,
                                        onValueChange = { nLimitCone = it },
                                        label = { Text(ui.supernovaNLabel) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(ui.supernovaConeHint, style = MaterialTheme.typography.bodySmall)

                                Button(
                                    onClick = {
                                        launchQuery {
                                            val n = parseIntSafe(nLimitCone, 30)
                                            val ra0 = parseRaToDeg(ra) ?: throw IllegalArgumentException("Invalid RA")
                                            val dec0 = parseDecToDeg(dec) ?: throw IllegalArgumentException("Invalid Dec")

                                            val tsv = OacApiClient.coneSearchSupernovaeTsv(ra, dec, radiusArcsec)
                                            val maps = parseTsvToMaps(tsv)

                                            rows = maps.mapNotNull { m ->
                                                val nm = m["name"].orEmpty().trim()
                                                if (nm.isBlank()) return@mapNotNull null

                                                val radeg = parseRaToDeg(m["ra"].orEmpty())
                                                val decdeg = parseDecToDeg(m["dec"].orEmpty())
                                                val dist = if (radeg != null && decdeg != null) {
                                                    angSepArcsec(ra0, dec0, radeg, decdeg)
                                                } else null

                                                SnRow(
                                                    name = nm,
                                                    claimedType = m["type"].orEmpty().takeIf { it.isNotBlank() },
                                                    redshift = m["z"].orEmpty().takeIf { it.isNotBlank() },
                                                    raDeg = radeg,
                                                    decDeg = decdeg,
                                                    distArcsec = dist
                                                )
                                            }
                                                .sortedWith(compareBy { it.distArcsec ?: Double.POSITIVE_INFINITY })
                                                .take(n)
                                        }
                                    },
                                    enabled = !loading
                                ) {
                                    Text(if (loading) ui.commonLoading else ui.commonQuery)
                                }
                            }
                        }

                        ResultsList(ui = ui, rows = rows) { r ->
                            launchQuery { openDetail(r.name) }
                        }
                    }

                    // Claimed Type (with N limit; click row -> load detail JSON)
                    2 -> {
                        Card {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(ui.supernovaClaimedTypeTitle, style = MaterialTheme.typography.titleMedium)

                                OutlinedTextField(
                                    value = claimedType,
                                    onValueChange = { claimedType = it },
                                    label = { Text(ui.supernovaClaimedTypeLabel) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = nLimitType,
                                    onValueChange = { nLimitType = it },
                                    label = { Text(ui.supernovaNLabel) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(ui.supernovaClaimedTypeHint, style = MaterialTheme.typography.bodySmall)

                                Button(
                                    onClick = {
                                        launchQuery {
                                            val n = parseIntSafe(nLimitType, 50)
                                            val tsv = OacApiClient.filterByClaimedTypeTsv(claimedType)
                                            val maps = parseTsvToMaps(tsv)

                                            rows = maps.mapNotNull { m ->
                                                val nm = m["name"].orEmpty().trim()
                                                if (nm.isBlank()) null else SnRow(
                                                    name = nm,
                                                    claimedType = m["type"].orEmpty().takeIf { it.isNotBlank() },
                                                    redshift = m["z"].orEmpty().takeIf { it.isNotBlank() },
                                                    raDeg = parseRaToDeg(m["ra"].orEmpty()),
                                                    decDeg = parseDecToDeg(m["dec"].orEmpty())
                                                )
                                            }.take(n)
                                        }
                                    },
                                    enabled = !loading
                                ) {
                                    Text(if (loading) ui.commonLoading else ui.commonQuery)
                                }
                            }
                        }

                        ResultsList(ui = ui, rows = rows) { r ->
                            launchQuery { openDetail(r.name) }
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDetail) {
        AlertDialog(
            onDismissRequest = { showDetail = false },
            title = { Text(detailTitle) },
            text = {
                SelectionContainer {
                    Text(
                        text = detailRaw,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetail = false }) {
                    Text(ui.commonClose)
                }
            }
        )
    }
}

@Composable
private fun ResultsList(
    ui: UiStrings,
    rows: List<SnRow>,
    onClick: (SnRow) -> Unit
) {
    if (rows.isEmpty()) return

    Card {
        Column(Modifier.padding(12.dp)) {
            Text(ui.supernovaResultsTitle, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(rows) { r ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick(r) }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(r.name, style = MaterialTheme.typography.bodyMedium)

                            val meta = buildString {
                                if (!r.claimedType.isNullOrBlank()) append("Type=${r.claimedType}  ")
                                if (!r.redshift.isNullOrBlank()) append("z=${r.redshift}  ")
                                if (r.raDeg != null && r.decDeg != null) {
                                    append("RA=${"%.5f".format(r.raDeg)}  Dec=${"%.5f".format(r.decDeg)}")
                                }
                            }.trim()

                            if (meta.isNotBlank()) {
                                Text(meta, style = MaterialTheme.typography.bodySmall)
                            } else {
                                Text("Tap to load details", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        if (r.distArcsec != null) {
                            Text(
                                text = "${"%.2f".format(r.distArcsec)}\"",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Divider()
                }
            }
        }
    }
}