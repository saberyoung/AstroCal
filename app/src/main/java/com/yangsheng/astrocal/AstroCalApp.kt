package com.yangsheng.astrocal

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.strings
import com.yangsheng.astrocal.ui.screens.features.AngleScreen
import com.yangsheng.astrocal.ui.screens.features.FinderScreen
import com.yangsheng.astrocal.ui.screens.features.TimeScreen
import com.yangsheng.astrocal.ui.screens.features.ToolsScreen
import com.yangsheng.astrocal.ui.screens.features.WeatherScreen
import com.yangsheng.astrocal.ui.screens.features.CloudMapScreen
import com.yangsheng.astrocal.ui.screens.features.CelestialScreen
import com.yangsheng.astrocal.ui.screens.home.HomeScreen
import com.yangsheng.astrocal.ui.screens.welcome.SplashScreen
import com.yangsheng.astrocal.ui.screens.welcome.WelcomeScreen
import com.yangsheng.astrocal.ui.theme.AstroTheme

enum class Screen { SPLASH, WELCOME, HOME, TOOLS, TIME, ANGLE, FINDER, WEATHER, CLOUDMAP, CELESTIAL }

@Composable
fun AstroCalApp() {
    val activity = LocalContext.current as Activity

    var screen by remember { mutableStateOf(Screen.SPLASH) }
    var lang by remember { mutableStateOf(Lang.ZH_HANS) }
    val ui = strings(lang)

    AstroTheme {
        when (screen) {
            Screen.SPLASH -> SplashScreen(
                onDone = { screen = Screen.WELCOME }
            )

            Screen.WELCOME -> WelcomeScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onTapToContinue = { screen = Screen.HOME }
            )

            Screen.HOME -> HomeScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.WELCOME },
                onClose = { activity.finish() },
                onGoFinder = { screen = Screen.FINDER },
                onGoTools = { screen = Screen.TOOLS },
                onOpenWeather = { screen = Screen.WEATHER },
                onOpenCelestial = { screen = Screen.CELESTIAL },
            )

            Screen.FINDER -> FinderScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME }
            )

            Screen.TOOLS -> ToolsScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME },
                onGoAngle = { screen = Screen.ANGLE },
                onGoTime = { screen = Screen.TIME }
            )

            Screen.ANGLE -> AngleScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.TOOLS },
                onClose = { screen = Screen.HOME }
            )

            Screen.TIME -> TimeScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.TOOLS },
                onClose = { screen = Screen.HOME }
            )

            Screen.WEATHER -> WeatherScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onClose = { screen = Screen.HOME },
                onBack = { screen = Screen.HOME },
                onOpenCloudMap = { screen = Screen.CLOUDMAP }
            )

            Screen.CLOUDMAP -> CloudMapScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.WEATHER },
                onClose = { screen = Screen.HOME }
            )

            Screen.CELESTIAL -> CelestialScreen(
                ui = ui,
                lang = lang,
                onLangSelected = { lang = it },
                onBack = { screen = Screen.HOME },
                onClose = { screen = Screen.HOME },
            )
        }
    }
}