package com.terminalmensagens.app

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HeartbeatWorker.schedule(applicationContext)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Color(0xFF22C55E),
                background = Color(0xFF0B0F14),
                surface = Color(0xFF12171F),
                onBackground = Color(0xFFE6EDF3),
                onSurface = Color(0xFFE6EDF3),
            )) {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
private fun MainScreen() {
    val ctx = LocalContext.current
    var apiUrl by remember { mutableStateOf(DeviceManager.apiUrl(ctx)) }
    var enabled by remember { mutableStateOf(isListenerEnabled(ctx)) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val obs = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) enabled = isListenerEnabled(ctx)
        }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F14))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "TERMINAL MENSAGENS",
            color = Color(0xFFE6EDF3),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        // Status card
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF12171F))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Status:", color = Color(0xFF8B95A5), fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (enabled) "🟢  Serviço ativo" else "🔴  Serviço desativado",
                        color = if (enabled) Color(0xFF22C55E) else Color(0xFFEF4444),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (enabled) {
                    Text(
                        "🟢  Captura de notificações ativa",
                        color = Color(0xFF22C55E),
                        fontSize = 13.sp
                    )
                    Text("Aparelho conectado", color = Color(0xFFE6EDF3), fontSize = 13.sp)
                }
            }
        }

        Button(
            onClick = {
                ctx.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                contentColor = Color(0xFF0B0F14)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ATIVAR ACESSO ÀS NOTIFICAÇÕES", fontWeight = FontWeight.Bold)
        }

        // API URL config
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF12171F))) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("URL da API", color = Color(0xFF8B95A5), fontSize = 12.sp)
                OutlinedTextField(
                    value = apiUrl,
                    onValueChange = { apiUrl = it },
                    placeholder = { Text("https://seu-terminal.lovable.app", color = Color(0xFF4B5563)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        DeviceManager.setApiUrl(ctx, apiUrl)
                        val token = DeviceManager.deviceToken(ctx)
                        CoroutineScope(Dispatchers.IO).launch {
                            ApiClient.heartbeat(
                                DeviceManager.apiUrl(ctx), token, DeviceManager.deviceName()
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Salvar URL") }

                Text(
                    "Device token: ${DeviceManager.deviceToken(ctx).take(8)}…",
                    color = Color(0xFF4B5563),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun isListenerEnabled(ctx: android.content.Context): Boolean {
    val cn = ComponentName(ctx, NotificationListener::class.java)
    val flat = Settings.Secure.getString(
        ctx.contentResolver, "enabled_notification_listeners"
    ) ?: return false
    return flat.split(":").any {
        try { ComponentName.unflattenFromString(it) == cn } catch (_: Exception) { false }
    }
}
