
package com.manish.springaiassistant

import com.manish.springaiassistant.data.model.WeatherSummary
import com.manish.springaiassistant.ui.components.WeatherCard
import com.manish.springaiassistant.utils.WeatherRequestDetector
import java.util.UUID

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff

import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime

import com.manish.springaiassistant.data.local.ChatDatabase
import com.manish.springaiassistant.data.local.ChatRepository
import com.manish.springaiassistant.data.local.ChatSessionEntity
import com.manish.springaiassistant.data.model.ChatRequest
import com.manish.springaiassistant.data.remote.RetrofitClient

import kotlinx.coroutines.launch

private val AppBackground = Color(0xFFF5F7FC)
private val PrimaryBlue = Color(0xFF3659E3)
private val DarkBlue = Color(0xFF233DAA)

private val HeaderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4776F5),
        Color(0xFF304CCB)
    )
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val weather: WeatherSummary? = null
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.getInsetsController(
            window,
            window.decorView
        ).isAppearanceLightStatusBars = true

        setContent {
            MaterialTheme {
                ManishAIApp()
            }
        }
    }
}

@Composable
fun ManishAIApp() {

    var message by remember { mutableStateOf("") }

    var conversationId by remember {
        mutableStateOf<String?>(null)
    }

    var isLoading by remember {
        mutableStateOf(false)
    }

    val context = LocalContext.current

    // -----------------------------------------
    // Local Chat Database
    // -----------------------------------------

    val chatDatabase = remember(context) {
        ChatDatabase.getDatabase(context)
    }

    val chatRepository = remember(chatDatabase) {
        ChatRepository(chatDatabase.chatDao())
    }

    var showHistorySheet by remember {
        mutableStateOf(false)
    }

    val chatSessions by chatRepository
        .getAllSessions()
        .collectAsState(initial = emptyList())

    var localSessionId by remember {
        mutableStateOf<String?>(null)
    }

    var isRestoringChat by remember {
        mutableStateOf(true)
    }

    // -----------------------------------------
    // Text-to-Speech State
    // Disabled by default
    // -----------------------------------------

    var speechEnabled by remember {
        mutableStateOf(false)
    }

    var isTtsReady by remember {
        mutableStateOf(false)
    }

    var pendingSpeech by remember {
        mutableStateOf<String?>(null)
    }

    val textToSpeechManager = remember(context) {
        TextToSpeechManager(context) { initialized ->
            isTtsReady = initialized
        }
    }

    // -----------------------------------------
    // Voice Input State
    // -----------------------------------------

    var isListening by remember {
        mutableStateOf(false)
    }

    var autoSendVoice by remember {
        mutableStateOf(false)
    }

    var voiceError by remember {
        mutableStateOf<String?>(null)
    }

    val messages = remember {
        mutableStateListOf<ChatMessage>()
    }

    // -----------------------------------------
    // Restore Previous Conversation
    // -----------------------------------------

    LaunchedEffect(chatRepository) {

        try {

            val latestSession = chatRepository.getLatestSession()

            if (latestSession != null) {

                localSessionId = latestSession.id

                conversationId = latestSession.backendConversationId

                val savedMessages = chatRepository.getMessages(
                    latestSession.id
                )

                messages.clear()

                messages.addAll(
                    savedMessages.map { savedMessage ->
                        ChatMessage(
                            text = savedMessage.text,
                            isUser = savedMessage.isUser
                        )
                    }
                )

                Log.d(
                    "ManishAI",
                    "Restored conversation: ${latestSession.id}"
                )
            }

        } catch (exception: Exception) {

            Log.e(
                "ManishAI",
                "Failed to restore chat history",
                exception
            )

        } finally {

            isRestoringChat = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // -----------------------------------------
    // New Chat
    // -----------------------------------------

    fun startNewChat() {

        if (isLoading) return

        messages.clear()

        localSessionId = null
        conversationId = null

        message = ""
        voiceError = null

        pendingSpeech = null
        textToSpeechManager.stop()

        Log.d("ManishAI", "New conversation started")
    }

    // -----------------------------------------
    // Send Message
    // Supports normal text and voice input
    // -----------------------------------------

    fun sendMessage(textOverride: String? = null) {

        val userMessage = (textOverride ?: message).trim()

        if (
            userMessage.isEmpty() ||
            isLoading ||
            isRestoringChat
        ) return

        messages.add(
            ChatMessage(userMessage, true)
        )

        message = ""
        isLoading = true

        coroutineScope.launch {

            try {

                // Create local session if required
                val sessionId = localSessionId
                    ?: chatRepository.createSession().id.also {
                        localSessionId = it
                    }

                // Save user message
                chatRepository.saveMessage(
                    sessionId = sessionId,
                    text = userMessage,
                    isUser = true
                )

                // Detect weather request
                val city = WeatherRequestDetector.extractCity(
                    userMessage
                )

                if (city != null) {

                    // Generate backend conversation ID if required
                    val activeConversationId = conversationId
                        ?: UUID.randomUUID().toString().also {
                            conversationId = it
                        }

                    // Persist backend conversation ID
                    chatRepository.updateBackendConversationId(
                        sessionId = sessionId,
                        backendId = activeConversationId
                    )

                    // Call structured weather endpoint
                    val weather = RetrofitClient.apiService
                        .getStructuredWeather(
                            city = city,
                            conversationId = activeConversationId
                        )

                    // Add structured weather response
                    messages.add(
                        ChatMessage(
                            text = "",
                            isUser = false,
                            weather = weather
                        )
                    )

                    // Save readable weather information in Room
                    val weatherText = """
                    Weather in ${weather.city}

                    Temperature: ${weather.temperature} °C
                    Humidity: ${weather.humidity}%
                    Condition: ${weather.condition}

                    Recommendation:
                    ${weather.recommendation}
                """.trimIndent()

                    chatRepository.saveMessage(
                        sessionId = sessionId,
                        text = weatherText,
                        isUser = false
                    )

                } else {

                    // Existing normal Spring AI chat flow
                    val response = RetrofitClient.apiService.sendMessage(
                        ChatRequest(
                            conversationId = conversationId,
                            message = userMessage
                        )
                    )

                    conversationId = response.conversationId

                    chatRepository.updateBackendConversationId(
                        sessionId = sessionId,
                        backendId = response.conversationId
                    )

                    messages.add(
                        ChatMessage(response.response, false)
                    )

                    chatRepository.saveMessage(
                        sessionId = sessionId,
                        text = response.response,
                        isUser = false
                    )

                    if (speechEnabled) {
                        pendingSpeech = response.response
                    }
                }

            } catch (exception: Exception) {

                Log.e(
                    "ManishAI",
                    "API request failed",
                    exception
                )

                messages.add(
                    ChatMessage(
                        text = "Sorry, something went wrong: ${
                            exception.localizedMessage
                                ?: "Please try again."
                        }",
                        isUser = false
                    )
                )

            } finally {

                isLoading = false
            }
        }
    }

    // -----------------------------------------
    // Speak AI responses when TTS is ready
    // -----------------------------------------

    LaunchedEffect(isTtsReady, speechEnabled, pendingSpeech) {

        val textToSpeak = pendingSpeech

        if (!speechEnabled) {

            pendingSpeech = null

        } else if (isTtsReady && !textToSpeak.isNullOrBlank()) {

            textToSpeechManager.speak(textToSpeak)

            pendingSpeech = null
        }
    }

    // -----------------------------------------
    // Release Text-to-Speech Resources
    // -----------------------------------------

    DisposableEffect(textToSpeechManager) {

        onDispose {
            textToSpeechManager.destroy()
        }
    }

    // -----------------------------------------
    // Voice Recognition Manager
    // -----------------------------------------

    val voiceInputManager = remember(context) {

        VoiceInputManager(

            context = context,

            onResult = { recognizedText ->

                message = recognizedText
                voiceError = null

                if (autoSendVoice) {
                    sendMessage(recognizedText)
                }
            },

            onVoiceError = { error ->
                voiceError = error
                Log.e("ManishAI", "Voice input error: $error")
            },

            onListeningChanged = { listening ->
                isListening = listening
            }
        )
    }

    // -----------------------------------------
    // Microphone Permission
    // -----------------------------------------

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                voiceError = null
                voiceInputManager.startListening()

            } else {

                isListening = false
                voiceError = "Microphone permission is required."
            }
        }

    // -----------------------------------------
    // Start Voice Input
    // -----------------------------------------

    fun startVoiceInput() {

        if (isLoading) return

        voiceError = null

        val permissionGranted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {

            voiceInputManager.startListening()

        } else {

            microphonePermissionLauncher.launch(
                Manifest.permission.RECORD_AUDIO
            )
        }
    }

    // -----------------------------------------
    // Release Speech Recognition Resources
    // -----------------------------------------

    DisposableEffect(voiceInputManager) {

        onDispose {
            voiceInputManager.destroy()
        }
    }

    // -----------------------------------------
    // Automatic Chat Scrolling
    // -----------------------------------------

    val density = LocalDensity.current

    val imeBottom = WindowInsets.ime.getBottom(density)

    LaunchedEffect(
        messages.size,
        isLoading,
        imeBottom
    ) {

        val lastIndex =
            listState.layoutInfo.totalItemsCount - 1

        if (lastIndex >= 0) {
            listState.animateScrollToItem(lastIndex)
        }
    }

    // -----------------------------------------
    // Main Scaffold
    // -----------------------------------------

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),

        containerColor = AppBackground,

        // -------------------------------------
        // Header
        // -------------------------------------

        topBar = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderGradient)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 20.dp,
                            vertical = 14.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Image(
                        painter = painterResource(
                            R.drawable.manish_ai_robot
                        ),
                        contentDescription = "Manish AI Robot",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.width(12.dp))

                    Column {

                        Text(
                            "Manish AI",
                            color = Color.White,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Your Personal AI Assistant",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Chat History Button
                    IconButton(
                        onClick = {
                            showHistorySheet = true
                        },
                        enabled = !isLoading
                    ) {

                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = Color.White
                        )
                    }

                    // New Chat Button
                    IconButton(
                        onClick = {

                            if (isListening) {
                                voiceInputManager.cancelListening()
                            }

                            startNewChat()
                        },
                        enabled = !isLoading
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = Color.White
                        )
                    }
                }
            }
        },

        // -------------------------------------
        // Bottom Input Area
        // -------------------------------------

        bottomBar = {

            Surface(
                color = Color.White,
                shadowElevation = 12.dp
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = 14.dp,
                            vertical = 10.dp
                        )
                ) {

                    // Voice Mode Selector and TTS Toggle

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        FilterChip(
                            selected = !autoSendVoice,
                            onClick = {
                                autoSendVoice = false
                                voiceError = null
                            },
                            enabled = !isListening,
                            label = {
                                Text("Voice to Text")
                            }
                        )

                        FilterChip(
                            selected = autoSendVoice,
                            onClick = {
                                autoSendVoice = true
                                voiceError = null
                            },
                            enabled = !isListening,
                            label = {
                                Text("Auto Send")
                            }
                        )

                        Spacer(Modifier.weight(1f))

                        // Enable / Disable AI Spoken Responses

                        IconButton(
                            onClick = {

                                speechEnabled = !speechEnabled

                                if (!speechEnabled) {
                                    textToSpeechManager.stop()
                                    pendingSpeech = null
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {

                            Icon(
                                imageVector = if (speechEnabled) {
                                    Icons.Default.VolumeUp
                                } else {
                                    Icons.Default.VolumeOff
                                },
                                contentDescription = if (speechEnabled) {
                                    "Disable spoken responses"
                                } else {
                                    "Enable spoken responses"
                                },
                                tint = if (speechEnabled) {
                                    PrimaryBlue
                                } else {
                                    Color.Gray
                                }
                            )
                        }
                    }

                    // Listening Status

                    if (isListening) {

                        Text(
                            "Listening... Speak now",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 8.dp,
                                    bottom = 8.dp
                                ),
                            color = PrimaryBlue,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Voice Error

                    if (voiceError != null) {

                        Text(
                            text = voiceError ?: "",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 8.dp,
                                    bottom = 8.dp
                                ),
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp
                        )
                    }

                    // Message Input Row

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Microphone Button

                        IconButton(
                            onClick = {

                                if (isListening) {
                                    voiceInputManager.stopListening()
                                } else {
                                    startVoiceInput()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    if (isListening)
                                        Color(0xFFE53935)
                                    else
                                        PrimaryBlue,
                                    CircleShape
                                )
                        ) {

                            Icon(
                                imageVector = if (isListening)
                                    Icons.Default.MicOff
                                else
                                    Icons.Default.Mic,

                                contentDescription = if (isListening)
                                    "Stop voice input"
                                else
                                    "Start voice input",

                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        // Message TextField

                        OutlinedTextField(
                            value = message,
                            onValueChange = {
                                message = it
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    "Ask Manish AI ...",
                                    color = Color.Gray
                                )
                            },
                            shape = RoundedCornerShape(26.dp),
                            maxLines = 4,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Send
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    sendMessage()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE0E4EF),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        // Send Button

                        IconButton(
                            onClick = {
                                sendMessage()
                            },
                            enabled = message.isNotBlank() && !isLoading,
                            modifier = Modifier
                                .size(50.dp)
                                .background(
                                    if (
                                        message.isNotBlank() &&
                                        !isLoading
                                    )
                                        PrimaryBlue
                                    else
                                        Color(0xFFBFC8E5),
                                    CircleShape
                                )
                        ) {

                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send message",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

    ) { padding ->

        // -------------------------------------
        // Welcome Screen / Chat Messages
        // -------------------------------------

        if (messages.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Spacer(Modifier.height(24.dp))

                Image(
                    painter = painterResource(
                        R.drawable.manish_ai_robot
                    ),
                    contentDescription = "Manish AI Assistant",
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Hello! 👋",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "I'm Manish AI",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF252B40)
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    "Your personal AI assistant. Ask me anything, explore ideas, or get help with everyday tasks.",
                    fontSize = 14.sp,
                    color = Color(0xFF737B91),
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(30.dp))

                Text(
                    "TRY ASKING",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF929BB0)
                )

                SuggestionCard(
                    title = "Explain something to me",
                    description = "Break down a complex topic simply",
                    onClick = {
                        message = "Explain something interesting to me"
                    }
                )

                Spacer(Modifier.height(10.dp))

                SuggestionCard(
                    title = "Help me write",
                    description = "Create emails, messages or documents",
                    onClick = {
                        message = "Help me write a professional email"
                    }
                )

                Spacer(Modifier.height(10.dp))

                SuggestionCard(
                    title = "Explore an idea",
                    description = "Brainstorm and discover new possibilities",
                    onClick = {
                        message = "Help me brainstorm a new idea"
                    }
                )

                Spacer(Modifier.height(24.dp))
            }

        } else {

            // ---------------------------------
            // Chat Messages
            // ---------------------------------

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(messages) { chatMessage ->

                    ChatBubble(chatMessage)
                }

                if (isLoading) {

                    item {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Image(
                                painter = painterResource(
                                    R.drawable.manish_ai_robot
                                ),
                                contentDescription = "AI is thinking",
                                modifier = Modifier.size(30.dp),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.width(10.dp))

                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )

                            Spacer(Modifier.width(10.dp))

                            Text(
                                "Manish AI is thinking...",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // -----------------------------------------
    // Chat History Bottom Sheet
    // IMPORTANT: This is outside Scaffold
    // -----------------------------------------

    if (showHistorySheet) {

        ChatHistorySheet(
            sessions = chatSessions,
            currentSessionId = localSessionId,

            onDismiss = {
                showHistorySheet = false
            },

            onSelectSession = { session ->

                coroutineScope.launch {

                    try {

                        val savedMessages =
                            chatRepository.getMessages(session.id)

                        localSessionId = session.id
                        conversationId = session.backendConversationId

                        messages.clear()

                        messages.addAll(
                            savedMessages.map { savedMessage ->
                                ChatMessage(
                                    text = savedMessage.text,
                                    isUser = savedMessage.isUser
                                )
                            }
                        )

                        message = ""
                        voiceError = null
                        pendingSpeech = null
                        textToSpeechManager.stop()

                        showHistorySheet = false


                    } catch (exception: Exception) {

                        Log.e(
                            "ManishAI",
                            "Failed to open conversation",
                            exception
                        )
                    }
                }
            },

            onDeleteSession = { session ->

                coroutineScope.launch {

                    try {

                        chatRepository.deleteSession(session.id)

                        // If the deleted conversation is currently open,
                        // return the app to a fresh chat screen.

                        if (localSessionId == session.id) {

                            messages.clear()
                            localSessionId = null
                            conversationId = null
                            message = ""

                            pendingSpeech = null
                            textToSpeechManager.stop()
                        }


                    } catch (exception: Exception) {

                        Log.e(
                            "ManishAI",
                            "Failed to delete conversation",
                            exception
                        )
                    }
                }
            }
        )
    }
}

// ---------------------------------------------
// Suggestion Card
// ---------------------------------------------

@Composable
fun SuggestionCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onClick()
            },
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF29365F)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                description,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

// ---------------------------------------------
// Chat Bubble
// ---------------------------------------------

@Composable
fun ChatBubble(message: ChatMessage) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser)
            Arrangement.End
        else
            Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {

        if (!message.isUser) {

            Image(
                painter = painterResource(
                    R.drawable.manish_ai_robot
                ),
                contentDescription = "AI Assistant",
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(8.dp))
        }

        Surface(
            color = if (message.isUser)
                PrimaryBlue
            else
                Color.White,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isUser) 18.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 18.dp
            ),
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {

            if (message.isUser) {

                Text(
                    text = message.text,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 13.dp
                    ),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 23.sp
                )

            } else {

                Column(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 13.dp
                    )
                ) {
                    when {
                        message.weather != null -> {
                            WeatherCard(weather = message.weather)
                        }

                        message.text.isNotBlank() -> {
                            MarkdownContent(message.text)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------
// Chat History Bottom Sheet
// ---------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistorySheet(
    sessions: List<ChatSessionEntity>,
    currentSessionId: String?,
    onDismiss: () -> Unit,
    onSelectSession: (ChatSessionEntity) -> Unit,
    onDeleteSession: (ChatSessionEntity) -> Unit
) {

    var sessionToDelete by remember {
        mutableStateOf<ChatSessionEntity?>(null)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 20.dp)
        ) {

            Text(
                text = "Chat History",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBlue
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Continue one of your previous conversations",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(18.dp))

            if (sessions.isEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.LightGray
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "No chat history yet",
                            color = Color.Gray,
                            fontSize = 15.sp
                        )
                    }
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {

                    items(
                        items = sessions,
                        key = { it.id }
                    ) { session ->

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSession(session)
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (
                                session.id == currentSessionId
                            ) {
                                Color(0xFFEDF1FF)
                            } else {
                                AppBackground
                            },
                            border = if (
                                session.id == currentSessionId
                            ) {
                                androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    PrimaryBlue
                                )
                            } else {
                                null
                            }
                        ) {

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 14.dp,
                                        vertical = 12.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        text = session.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF29365F),
                                        maxLines = 2
                                    )

                                    Spacer(Modifier.height(4.dp))

                                    Text(
                                        text = java.text.SimpleDateFormat(
                                            "dd MMM yyyy, hh:mm a",
                                            java.util.Locale.getDefault()
                                        ).format(
                                            java.util.Date(session.updatedAt)
                                        ),
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        sessionToDelete = session
                                    }
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete conversation",
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog

    sessionToDelete?.let { session ->

        AlertDialog(
            onDismissRequest = {
                sessionToDelete = null
            },

            title = {
                Text("Delete conversation?")
            },

            text = {
                Text(
                    "Are you sure you want to permanently delete " +
                            "\"${session.title}\"?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        onDeleteSession(session)
                        sessionToDelete = null
                    }
                ) {

                    Text(
                        "Delete",
                        color = Color(0xFFD32F2F)
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {
                        sessionToDelete = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }
}
