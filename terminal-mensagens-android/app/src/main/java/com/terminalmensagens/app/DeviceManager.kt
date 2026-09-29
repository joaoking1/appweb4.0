package com.terminalmensagens.app

import android.content.Context
import android.os.Build
import java.util.UUID

object DeviceManager {
    private const val PREFS = "terminal_mensagens_prefs"
    private const val KEY_TOKEN = "device_token"
    private const val KEY_API_URL = "api_url"

    fun deviceToken(ctx: Context): String {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_TOKEN, null)
        if (existing != null) return existing
        val token = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_TOKEN, token).apply()
        return token
    }

    fun deviceName(): String = "${Build.MANUFACTURER} ${Build.MODEL}"

    fun apiUrl(ctx: Context): String {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_API_URL, "") ?: ""
    }

    fun setApiUrl(ctx: Context, url: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_API_URL, url.trim().trimEnd('/')).apply()
    }
}
