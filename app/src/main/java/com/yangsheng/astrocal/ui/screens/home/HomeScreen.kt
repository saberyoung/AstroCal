package com.yangsheng.astrocal.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.FeatureCard
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.ui.theme.AstroBackground

@Composable
fun HomeScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onGoFinder: () -> Unit,
    onGoTools: () -> Unit,
    onOpenWeather: () -> Unit,
    onOpenCelestial: () -> Unit,
    onOpenAI: () -> Unit,
    onOpenOAC: () -> Unit,
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

    Scaffold(
        containerColor = Color.Transparent, // IMPORTANT: avoid white "veil" over background
        topBar = {
            AppTopBar(
                title = ui.homeTitle,
                ui = ui,
                onBack = onBack,
                onLang = { showLang = true },
                onAi = onOpenAI,
                onClose = onClose
            )
        }
    ) { pad ->
        AstroBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pad)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureCard(
                    title = ui.finderTitle,
                    subtitle = ui.finderDesc,
                    onClick = onGoFinder
                )

                FeatureCard(
                    title = ui.homeCelestialTitle,
                    subtitle = ui.homeCelestialSubtitle,
                    onClick = onOpenCelestial
                )

                FeatureCard(
                    title = ui.OACTitle,
                    subtitle = ui.OACSubTitle,
                    onClick = onOpenOAC
                )

                FeatureCard(
                    title = ui.homeWeatherTitle,
                    subtitle = ui.homeWeatherSubtitle,
                    onClick = onOpenWeather
                )

                FeatureCard(
                    title = ui.ToolsTitle,
                    subtitle = ui.ToolsDesc,
                    onClick = onGoTools
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    text = ui.appSubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
