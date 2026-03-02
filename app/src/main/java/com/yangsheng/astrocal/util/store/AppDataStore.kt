package com.yangsheng.astrocal.util.store

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// 文件名随意，这里叫 astrocal_prefs
val Context.dataStore by preferencesDataStore(name = "astrocal_prefs")