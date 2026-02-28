@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yangsheng.astrocal.ui.screens.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.util.GeoPlace
import com.yangsheng.astrocal.util.OpenMeteoService
import com.yangsheng.astrocal.util.WeatherForecast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WeatherScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onClose: () -> Unit,
    onBack: () -> Unit,
    onOpenCloudMap: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var showLang by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("Rome") }

    var searching by remember { mutableStateOf(false) }
    var places by remember { mutableStateOf<List<GeoPlace>>(emptyList()) }
    var selected by remember { mutableStateOf<GeoPlace?>(null) }

    var loadingForecast by remember { mutableStateOf(false) }
    var forecast by remember { mutableStateOf<WeatherForecast?>(null) }

    var error by remember { mutableStateOf<String?>(null) }

    // ✅ 用项目自带的语言选择弹窗（和 Finder 一致）
    LangPickerDialog(
        visible = showLang,
        title = ui.selectLanguageTitle,
        // 如果你的 LangPickerDialog 不需要 current/onDismiss，会提示“no parameter…”，删掉对应行即可
        current = lang,
        onSelect = {
            onLangSelected(it)
            showLang = false
        },
        onDismiss = { showLang = false }
    )

    fun runSearch() {
        val q = query.trim()
        if (q.isEmpty()) return
        searching = true
        error = null
        places = emptyList()
        selected = null
        forecast = null

        val langCode = when (lang) {
            Lang.ZH_HANS -> "zh"
            Lang.ZH_HANT -> "zh"
            Lang.EN -> "en"
            Lang.IT -> "it"
            Lang.JA -> "ja"
            Lang.KO -> "ko"
        }

        scope.launch {
            try {
                val res = withContext(Dispatchers.IO) {
                    OpenMeteoService.searchCity(q, count = 10, language = langCode)
                }
                places = res
                if (res.isEmpty()) error = ui.weatherNoResults
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            } finally {
                searching = false
            }
        }
    }

    fun loadForecast(p: GeoPlace) {
        selected = p
        loadingForecast = true
        error = null
        forecast = null

        scope.launch {
            try {
                val fc = withContext(Dispatchers.IO) {
                    OpenMeteoService.fetchHourlyForecast(
                        latitude = p.latitude,
                        longitude = p.longitude,
                        timezone = p.timezone ?: "auto"
                    )
                }
                forecast = fc
            } catch (e: Exception) {
                error = e.message ?: e.toString()
            } finally {
                loadingForecast = false
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.weatherTitle,
                ui = ui,
                onBack = onBack,
                onLang = { showLang = true },
                onClose = onClose
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(ui.weatherCitySearchTitle, style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text(ui.weatherCityPlaceLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { runSearch() },
                            enabled = !searching
                        ) {
                            Text(if (searching) ui.weatherSearching else ui.weatherSearchButton)
                        }

                        if (selected != null) {
                            OutlinedButton(
                                onClick = { loadForecast(selected!!) },
                                enabled = !loadingForecast
                            ) {
                                Text(if (loadingForecast) ui.weatherLoading else ui.weatherRefreshButton)
                            }
                        }
                    }

                    if (error != null) {
                        Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Button(onClick = onOpenCloudMap) {
                Text(ui.cloudMapButton)
            }

            if (places.isNotEmpty() && selected == null) {
                Text(ui.weatherSelectPlace, style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(places) { p ->
                        Card(onClick = { loadForecast(p) }) {
                            Column(Modifier.padding(12.dp)) {
                                Text(p.label, style = MaterialTheme.typography.titleSmall)
                                Spacer(Modifier.height(4.dp))
                                Text("lat=${p.latitude}, lon=${p.longitude}  tz=${p.timezone ?: "auto"}")
                            }
                        }
                    }
                }
            }

            val sel = selected
            val fc = forecast
            if (sel != null) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(sel.label, style = MaterialTheme.typography.titleMedium)
                        Text("lat=${sel.latitude}, lon=${sel.longitude}, tz=${fc?.timezone ?: sel.timezone ?: "auto"}")
                    }
                }
            }

            if (loadingForecast) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (fc != null) {
                Text(ui.weatherNext24h, style = MaterialTheme.typography.titleSmall)
                val next24 = fc.hours.take(24)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(next24) { h ->
                        Card {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(h.timeIso, style = MaterialTheme.typography.labelLarge)

                                val t = h.temperature2m?.let { String.format("%.1f°C", it) } ?: "-"
                                val w = h.windSpeed10m?.let { String.format("%.1f m/s", it) } ?: "-"
                                val p = h.precipProb?.let { "$it%" } ?: "-"
                                val c = h.cloudCover?.let { "$it%" } ?: "-"

                                Text("${ui.weatherTemp}: $t   ${ui.weatherWind}: $w")
                                Text("${ui.weatherPrecip}: $p   ${ui.weatherCloud}: $c")

                                val low = h.cloudLow?.let { "$it%" } ?: "-"
                                val mid = h.cloudMid?.let { "$it%" } ?: "-"
                                val high = h.cloudHigh?.let { "$it%" } ?: "-"
                                Text("${ui.weatherCloudLMH}: $low / $mid / $high")
                            }
                        }
                    }
                }
            }
        }
    }
}