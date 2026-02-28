package com.yangsheng.astrocal.ui.screens.features

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.FeatureCard
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog

@Composable
fun ToolsScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onGoAngle: () -> Unit,
    onGoTime: () -> Unit
) {
    var showLang by remember { mutableStateOf(false) }

    LangPickerDialog(
        visible = showLang,
        title = ui.chooseLang,
        current = lang,
        onDismiss = { showLang = false },
        onSelect = { showLang = false; onLangSelected(it) }
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = ui.ToolsTitle,
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
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FeatureCard(
                title = ui.timeTitle,
                subtitle = ui.timeDesc,
                onClick = onGoTime
            )
            FeatureCard(
                title = ui.angleTitle,
                subtitle = ui.angleDesc,
                onClick = onGoAngle
            )
        }
    }
}