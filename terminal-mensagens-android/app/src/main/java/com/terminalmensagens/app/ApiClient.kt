package com.terminalmensagens.app

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val TAG = "TerminalMensagensApi"

    fun sendMessage(
        baseUrl: String,
        deviceToken: String,
        deviceName: String,
        app: String,
        sender: String?,
        message: String?,
        receivedAtIso: String
    ): Boolean {
        if (baseUrl.isBlank()) return false
        val payload = JSONObject().apply {
            put("device_token", deviceToken)
            put("device_name", deviceName)
            put("app", app)
            put("sender", sender ?: JSONObject.NULL)
            put("message", message ?: JSONObject.NULL)
            put("received_at", receivedAtIso)
        }
        return post("$baseUrl/api/public/messages", payload)
    }

    fun heartbeat(baseUrl: String, deviceToken: String, deviceName: String): Boolean {
        if (baseUrl.isBlank()) return false
        val payload = JSONObject().apply {
            put("device_token", deviceToken)
            put("device_name", deviceName)
            put("status", "online")
        }
        return post("$baseUrl/api/public/heartbeat", payload)
    }

    private fun post(urlStr: String, body: JSONObject): Boolean {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            Log.d(TAG, "POST $urlStr -> $code")
            code in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "post failed: ${e.message}")
            false
        } finally {
            conn?.disconnect()
        }
    }
}
