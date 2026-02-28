@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yangsheng.astrocal.ui.screens.features

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog

// 你可以按需扩展更多 survey
private enum class Survey(val label: String, val surveyId: String) {
    DSS_COLOR("DSS (Color)", "P/DSS2/color"),
    DSS_RED("DSS (Red)", "P/DSS2/red"),
    PS1_COLOR("Pan-STARRS (Color)", "P/PanSTARRS/DR1/color-z-zg-g"),
    PS1_STACK("Pan-STARRS (Stack)", "P/PanSTARRS/DR1/stack"),
    TWOMASS("2MASS", "P/2MASS/color"),
    GAIA("Gaia (EDR3 density)", "P/DM/gaiaedr3") // 若加载慢可先删掉
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CelestialScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    var showLang by remember { mutableStateOf(false) }

    // Goto 输入
    var raInput by remember { mutableStateOf("150.0") }
    var decInput by remember { mutableStateOf("2.0") }

    // Survey 选择
    var survey by remember { mutableStateOf(Survey.DSS_COLOR) }
    var surveyMenu by remember { mutableStateOf(false) }

    // WebView 引用
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    LangPickerDialog(
        visible = showLang,
        title = ui.selectLanguageTitle,
        current = lang,
        onSelect = {
            onLangSelected(it)
            showLang = false
        },
        onDismiss = { showLang = false }
    )

    fun jsEscape(s: String): String =
        s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")

    fun jsGoto(ra: String, dec: String) {
        val target = "${ra.trim()} ${dec.trim()}".trim()
        val js = "window.gotoTarget && window.gotoTarget('${jsEscape(target)}');"
        webViewRef?.evaluateJavascript(js, null)
    }

    fun jsSetSurvey(s: Survey) {
        val js = "window.setSurvey && window.setSurvey('${jsEscape(s.surveyId)}');"
        webViewRef?.evaluateJavascript(js, null)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.celestialTitle,
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
            // 控制面板
            Card {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(ui.celestialControlTitle, style = MaterialTheme.typography.titleMedium)

                    // Survey 下拉
                    Box {
                        OutlinedButton(onClick = { surveyMenu = true }) {
                            Text("${ui.celestialSurvey}: ${survey.label}")
                        }
                        DropdownMenu(
                            expanded = surveyMenu,
                            onDismissRequest = { surveyMenu = false }
                        ) {
                            Survey.entries.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.label) },
                                    onClick = {
                                        survey = s
                                        surveyMenu = false
                                        jsSetSurvey(s)
                                    }
                                )
                            }
                        }
                    }

                    // Goto 输入
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = raInput,
                            onValueChange = { raInput = it },
                            label = { Text(ui.celestialRaLabel) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = decInput,
                            onValueChange = { decInput = it },
                            label = { Text(ui.celestialDecLabel) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { jsGoto(raInput, decInput) }) {
                            Text(ui.celestialGoto)
                        }
                        OutlinedButton(onClick = { jsGoto("0", "0") }) {
                            Text(ui.celestialGotoZero)
                        }
                    }

                    Text(ui.celestialHint, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Aladin WebView
            Card(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        // 开启 WebView 调试（可用 Chrome inspect）
                        WebView.setWebContentsDebuggingEnabled(true)

                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadsImagesAutomatically = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true

                            // 让 console/JS 报错可见
                            webChromeClient = android.webkit.WebChromeClient()

                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    error: android.webkit.WebResourceError?
                                ) {
                                    Log.e("AstroCalWebView", "onReceivedError: $error")
                                    super.onReceivedError(view, request, error)
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    errorResponse: android.webkit.WebResourceResponse?
                                ) {
                                    Log.e("AstroCalWebView", "onReceivedHttpError: $errorResponse")
                                    super.onReceivedHttpError(view, request, errorResponse)
                                }
                            }

                            val html = buildAladinHtml(
                                initialSurveyId = survey.surveyId,
                                initialTarget = "${raInput.trim()} ${decInput.trim()}",
                                initialFovDeg = 2.0
                            )

                            loadDataWithBaseURL(
                                "https://aladin.cds.unistra.fr/",
                                html,
                                "text/html",
                                "UTF-8",
                                null
                            )

                            webViewRef = this
                        }
                    }
                )
            }
        }
    }
}

private fun buildAladinHtml(
    initialSurveyId: String,
    initialTarget: String,
    initialFovDeg: Double
): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
          <script src="https://aladin.cds.unistra.fr/AladinLite/api/v3/latest/aladin.js"></script>
          <link rel="stylesheet" href="https://aladin.cds.unistra.fr/AladinLite/api/v3/latest/aladin.css"/>
          <style>
            html, body { margin:0; padding:0; height:100%; background:#000; }
            #a { width:100%; height:100%; }
            #loading {
              position:absolute; left:12px; top:12px;
              color:#fff; font-family:sans-serif; font-size:14px;
              background:rgba(0,0,0,0.45); padding:8px 10px; border-radius:10px;
              z-index:9999;
            }
          </style>
        </head>
        <body>
          <div id="loading">Loading Aladin Lite…</div>
          <div id="a"></div>

          <script>
            function setText(t) {
              var el = document.getElementById('loading');
              if (el) el.innerText = t;
            }
            function hideLoading() {
              var el = document.getElementById('loading');
              if (el) el.style.display = 'none';
            }

            // 如果 aladin.js 没加载到，这里会直接提示
            if (typeof A === 'undefined') {
              setText('Failed to load aladin.js (network/SSL/WebView).');
            } else {
              let aladin = A.aladin('#a', {
                survey: '${escapeJs(initialSurveyId)}',
                target: '${escapeJs(initialTarget)}',
                fov: ${initialFovDeg}
              });

              // 暴露给 Android evaluateJavascript 调用
              window.setSurvey = function(surveyId) {
                try { aladin.setImageSurvey(surveyId); } catch(e) {}
              };
              window.gotoTarget = function(target) {
                try { aladin.gotoObject(target); } catch(e) {}
              };

              hideLoading();
            }
          </script>
        </body>
        </html>
    """.trimIndent()
}

private fun escapeJs(s: String): String =
    s.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")