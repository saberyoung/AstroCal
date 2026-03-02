package com.yangsheng.astrocal.util.store

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yangsheng.astrocal.ai.AiMode
import com.yangsheng.astrocal.ui.i18n.Lang
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AppSettings(
    val lang: Lang = Lang.ZH_HANS,
    val aiMode: AiMode = AiMode.LOCAL_RULES,
    val aiApiKey: String = ""
)

class AppSettingsRepo(private val context: Context) {

    private object Keys {
        val LANG = stringPreferencesKey("lang")
        val AI_MODE = stringPreferencesKey("ai_mode_provider")
        val AI_API_KEY = stringPreferencesKey("ai_api_key")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        val langStr = prefs[Keys.LANG] ?: Lang.ZH_HANS.name
        val lang = runCatching { Lang.valueOf(langStr) }.getOrDefault(Lang.ZH_HANS)

        val aiModeStr = prefs[Keys.AI_MODE] ?: AiMode.LOCAL_RULES.name
        val aiMode = runCatching { AiMode.valueOf(aiModeStr) }.getOrDefault(AiMode.LOCAL_RULES)

        val key = prefs[Keys.AI_API_KEY] ?: ""

        AppSettings(lang = lang, aiMode = aiMode, aiApiKey = key)
    }

    suspend fun setLang(lang: Lang) {
        context.dataStore.edit { it[Keys.LANG] = lang.name }
    }

    suspend fun setAiMode(mode: AiMode) {
        context.dataStore.edit { it[Keys.AI_MODE] = mode.name }
    }

    suspend fun setAiApiKey(key: String) {
        context.dataStore.edit { it[Keys.AI_API_KEY] = key }
    }

    suspend fun clearAiApiKey() {
        context.dataStore.edit { it[Keys.AI_API_KEY] = "" }
    }
}