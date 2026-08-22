package com.nexus.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.nexus.assistant.command.CommandProcessor
import com.nexus.assistant.ui.theme.*
import com.nexus.assistant.voice.VoiceManager
import java.text.SimpleDateFormat
import java.util.*

data class LogEntry(val time: String, val command: String, val reply: String, val success: Boolean)

class MainActivity : ComponentActivity() {

    private var voiceManager: VoiceManager? = null
    private var hasMicPermission by mutableStateOf(false)

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasMicPermission = granted }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasMicPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        setContent {
            NexusTheme {
                val logs = remember { mutableStateListOf<LogEntry>() }
                var isListening by remember { mutableStateOf(false) }
                var statusText by remember { mutableStateOf("جاهز") }
                var inputText by remember { mutableStateOf("") }

                if (voiceManager == null) {
                    voiceManager = VoiceManager(
                        context = this,
                        onResult = { text ->
                            inputText = text
                            handleCommand(text, logs) { statusText = it }
                        },
                        onListeningChange = { listening ->
                            isListening = listening
                            statusText = if (listening) "يسمع..." else "جاهز"
                        },
                        onError = { err -> statusText = err }
                    )
                }

                NexusScreen(
                    statusText = statusText,
                    isListening = isListening,
                    hasMicPermission = hasMicPermission,
                    inputText = inputText,
                    onInputChange = { inputText = it },
                    onMicClick = {
                        if (!hasMicPermission) {
                            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            voiceManager?.startListening()
                        }
                    },
                    onSendClick = {
                        if (inputText.isNotBlank()) {
                            val cmd = inputText
                            handleCommand(cmd, logs) { statusText = it }
                            inputText = ""
                        }
                    },
                    logs = logs
                )
            }
        }
    }

    private fun handleCommand(text: String, logs: MutableList<LogEntry>, setStatus: (String) -> Unit) {
        setStatus("يفكر...")
        val result = CommandProcessor.process(this, text)
        setStatus("جاهز")
        voiceManager?.speak(result.spokenReply)
        val time = SimpleDateFormat("HH:mm", Locale("ar")).format(Date())
        logs.add(0, LogEntry(time, text, result.spokenReply, result.success))
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager?.release()
    }
}

@Composable
fun NexusScreen(
    statusText: String,
    isListening: Boolean,
    hasMicPermission: Boolean,
    inputText: String,
    onInputChange: (String) -> Unit,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit,
    logs: List<LogEntry>
) {
    Scaffold(
        containerColor = NexusBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(NexusViolet, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("N", color = NexusBackground, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("NEXUS", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NexusInk)
                            Text(statusText, fontSize = 11.sp, color = NexusInkDim)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NexusBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Text(
                text = "وش تحب أساعدك فيه؟",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = NexusInk,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                FloatingActionButton(
                    onClick = onMicClick,
                    containerColor = if (isListening) NexusAmber else NexusViolet,
                    contentColor = NexusBackground,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "تحدث", modifier = Modifier.size(30.dp))
                }
            }

            if (!hasMicPermission) {
                Text(
                    "بدون صلاحية الميكروفون تقدر تكتب الأوامر بس",
                    fontSize = 11.sp,
                    color = NexusInkDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("اكتب أمر مثل \"افتح واتساب\"", color = NexusInkDim, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NexusPanel,
                        unfocusedContainerColor = NexusPanel,
                        focusedTextColor = NexusInk,
                        unfocusedTextColor = NexusInk,
                        focusedBorderColor = NexusViolet,
                        unfocusedBorderColor = NexusLine
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(NexusViolet, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "إرسال", tint = NexusBackground)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("سجل النشاط", fontWeight = FontWeight.Bold, color = NexusInk, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            if (logs.isEmpty()) {
                Text(
                    "ما فيه أوامر بعد. جرب تقول أو تكتب \"افتح الكاميرا\"",
                    fontSize = 12.sp,
                    color = NexusInkDim,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(logs) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(NexusPanel, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(entry.command, fontWeight = FontWeight.SemiBold, color = NexusInk, fontSize = 13.sp)
                                Text(entry.time, color = NexusInkDim, fontSize = 10.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                entry.reply,
                                color = if (entry.success) NexusGreen else NexusDanger,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
