package com.yosebooster.racingrush.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SecurityBlockScreen(
    detectedViolations: List<String>,
    isDebuggable: Boolean,
    onBypass: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E) // Dark security-themed background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⚠️ SEGURIDAD",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = Color(0xFFFF5555),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SISTEMA ANTI-HACK & ANTI-FRAUDE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 1.sp
                ),
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Se han detectado las siguientes anomalías:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                for (violation in detectedViolations) {
                    Text(
                        text = "• $violation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFFF8888),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Para proteger la integridad de las puntuaciones, compras y asegurar un juego justo, RacingRush bloquea entornos modificados, depuradores o emuladores en producción.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { activity?.finish() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "SALIR DEL JUEGO",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            if (isDebuggable) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBypass,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        "OMITIR ADVERTENCIA (SÓLO DEBUG)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Yellow
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nota: Como desarrollador, puedes omitir esta alerta para probar el APK en tu emulador o dispositivo ruteado.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Yellow,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
