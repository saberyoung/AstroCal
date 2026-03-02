package com.yangsheng.astrocal

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.strings
import com.yangsheng.astrocal.ui.screens.components.AiPanelBottomSheet
import com.yangsheng.astrocal.ui.screens.features.*
import com.yangsheng.astrocal.ui.screens.home.HomeScreen
import com.yangsheng.astrocal.ui.screens.welcome.SplashScreen
import com.yangsheng.astrocal.ui.screens.welcome.WelcomeScreen
import com.yangsheng.astrocal.ui.theme.AstroTheme
import com.yangsheng.astrocal.util.store.AppSettings
import com.yangsheng.astrocal.util.store.AppSettingsRepo
import kotlinx.coroutines.launch

enum class Screen { SPLASH, WELCOME, HOME, TOOLS, TIME, ANGLE, FINDER, WEATHER, CLOUDMAP, CELESTIAL, OAC }

@Composable
fun AstroCalApp() {
    val activity = LocalContext.current as Activity

    var screen by remember { mutableStateOf(Screen.SPLASH) }

    val scope = rememberCoroutineScope()
    val repo = remember { AppSettingsRepo(activity) }
    val settings by repo.settingsFlow.collectAsState(initial = AppSettings())

    val lang = settings.lang
    val ui = strings(lang)

    var aiPanelVisible by remember { mutableStateOf(false) }
    val aiMode = settings.aiMode
    val aiApiKey = settings.aiApiKey

    val persistLang: (Lang) -> Unit = { chosen ->
        scope.launch { repo.setLang(chosen) }
    }
    val openAi: () -> Unit = { aiPanelVisible = true }

    AstroTheme {
        when (screen) {
            Screen.SPLASH -> SplashScreen(onDone = { screen = Screen.WELCOME })

            Screen.WELCOME -> WelcomeScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onTapToContinue = { screen = Screen.HOME }
            )

            Screen.HOME -> HomeScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.WELCOME },
                onClose = { activity.finish() },
                onGoFinder = { screen = Screen.FINDER },
                onGoTools = { screen = Screen.TOOLS },
                onOpenWeather = { screen = Screen.WEATHER },
                onOpenCelestial = { screen = Screen.CELESTIAL },
                onOpenAI = openAi,
                onOpenOAC = { screen = Screen.OAC },
            )

            Screen.FINDER -> FinderScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME },
                onAi = openAi
            )

            Screen.TOOLS -> ToolsScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME },
                onGoAngle = { screen = Screen.ANGLE },
                onGoTime = { screen = Screen.TIME },
                onAi = openAi
            )

            Screen.ANGLE -> AngleScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.TOOLS },
                onClose = { screen = Screen.HOME },
                onAi = openAi
            )

            Screen.TIME -> TimeScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.TOOLS },
                onClose = { screen = Screen.HOME },
                onAi = openAi
            )

            Screen.WEATHER -> WeatherScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onClose = { screen = Screen.HOME },
                onBack = { screen = Screen.HOME },
                onOpenCloudMap = { screen = Screen.CLOUDMAP },
                onAi = openAi
            )

            Screen.CLOUDMAP -> CloudMapScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.WEATHER },
                onClose = { screen = Screen.HOME },
                onAi = openAi
            )

            Screen.CELESTIAL -> CelestialScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME },
                onAi = openAi
            )

            Screen.OAC -> SupernovaScreen(
                ui = ui,
                lang = lang,
                onLangSelected = persistLang,
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME },
                onAi = openAi
            )
        }

        AiPanelBottomSheet(
            ui = ui,
            lang = lang,
            visible = aiPanelVisible,
            mode = aiMode,
            onModeChange = { m -> scope.launch { repo.setAiMode(m) } },
            apiKey = aiApiKey,
            onApiKeyChange = { k -> scope.launch { repo.setAiApiKey(k) } },
            onDismiss = { aiPanelVisible = false }
        )
    }
}