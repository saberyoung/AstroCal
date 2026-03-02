package com.yangsheng.astrocal.ui.screens.features

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import com.yangsheng.astrocal.ui.screens.components.AppTopBar
import com.yangsheng.astrocal.ui.screens.components.LangPickerDialog
import com.yangsheng.astrocal.ui.theme.AstroBackground

@Composable
private fun AstroFeatureTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val border = Brush.horizontalGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
        )
    )

    Box(
        modifier = modifier
            .border(1.2.dp, border, shape)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.68f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
            )
        }
    }
}

@Composable
fun ToolsScreen(
    ui: UiStrings,
    lang: Lang,
    onLangSelected: (Lang) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onGoAngle: () -> Unit,
    onGoTime: () -> Unit,
    onAi: (() -> Unit)? = null,
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
                onAi = onAi,
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
                // 2-column grid (Row-based, no LazyVerticalGrid dependency)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AstroFeatureTile(
                        title = ui.timeTitle,
                        subtitle = ui.timeDesc,
                        onClick = onGoTime,
                        modifier = Modifier.weight(1f)
                    )
                    AstroFeatureTile(
                        title = ui.angleTitle,
                        subtitle = ui.angleDesc,
                        onClick = onGoAngle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}
