package com.wakala.fakhr

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object SecureStore {
    private const val PREF_FILE = "secure_prefs"

    fun getEncryptedPrefs(context: Context) = EncryptedSharedPreferences.create(
        PREF_FILE,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeApiKey(context: Context, apiKey: String) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit().putString("api_key", apiKey).apply()
    }

    fun getApiKey(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString("api_key", null)
    }
}
