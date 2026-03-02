@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.yangsheng.astrocal.ui.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.yangsheng.astrocal.ai.*
import com.yangsheng.astrocal.ai.AiMessage.Role
import com.yangsheng.astrocal.ui.i18n.Lang
import com.yangsheng.astrocal.ui.i18n.UiStrings
import kotlinx.coroutines.launch

@Composable
fun AiPanelBottomSheet(
    ui: UiStrings,
    lang: Lang,
    visible: Boolean,
    mode: AiMode,
    onModeChange: (AiMode) -> Unit,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    var input by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }

    // very small chat memory (in-panel)
    var chat by remember {
        mutableStateOf(
            listOf(
                AiMessage(Role.SYSTEM, ui.aiSystemPrompt),
                AiMessage(Role.ASSISTANT, ui.aiHello)
            )
        )
    }

    // provider menu
    var providerMenu by remember { mutableStateOf(false) }
    val providerLabel = when (mode) {
        AiMode.OFF -> ui.aiModeOff
        AiMode.LOCAL_RULES -> ui.aiModeLocal
        AiMode.OPENAI -> ui.aiModeOpenAI
        AiMode.GEMINI -> ui.aiModeGemini
        AiMode.CLAUDE -> ui.aiModeClaude
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(ui.aiPanelTitle, style = MaterialTheme.typography.titleLarge)

            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    Text(ui.aiModeTitle, style = MaterialTheme.typography.titleMedium)

                    Box {
                        OutlinedButton(onClick = { providerMenu = true }) {
                            Text("${ui.aiProviderLabel}: $providerLabel")
                        }
                        DropdownMenu(
                            expanded = providerMenu,
                            onDismissRequest = { providerMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(ui.aiModeOff) },
                                onClick = { onModeChange(AiMode.OFF); providerMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(ui.aiModeLocal) },
                                onClick = { onModeChange(AiMode.LOCAL_RULES); providerMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(ui.aiModeOpenAI) },
                                onClick = { onModeChange(AiMode.OPENAI); providerMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(ui.aiModeGemini) },
                                onClick = { onModeChange(AiMode.GEMINI); providerMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text(ui.aiModeClaude) },
                                onClick = { onModeChange(AiMode.CLAUDE); providerMenu = false }
                            )
                        }
                    }

                    if (mode == AiMode.OPENAI || mode == AiMode.GEMINI || mode == AiMode.CLAUDE) {
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = onApiKeyChange,
                            label = { Text(ui.aiApiKeyLabel) },
                            placeholder = { Text(ui.aiApiKeyPlaceholder) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (ui.aiMaskApiKey) PasswordVisualTransformation() else VisualTransformation.None
                        )
                        Text(ui.aiApiKeyHint, style = MaterialTheme.typography.bodySmall)
                    } else if (mode == AiMode.LOCAL_RULES) {
                        Text(ui.aiLocalHint, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text(ui.aiOffHint, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(ui.aiChatTitle, style = MaterialTheme.typography.titleMedium)

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // show only user/assistant
                        val shown = chat.filter { it.role != Role.SYSTEM }
                        items(shown) { m ->
                            val prefix = when (m.role) {
                                Role.USER -> ui.aiYouPrefix
                                Role.ASSISTANT -> ui.aiAssistantPrefix
                                Role.SYSTEM -> ""
                            }
                            Text("$prefix ${m.content}")
                        }
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text(ui.aiAskSomething) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        minLines = 2
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val q = input.trim()
                                if (q.isEmpty()) return@Button

                                // OFF -> hint
                                if (mode == AiMode.OFF) {
                                    scope.launch { snackbar.showSnackbar(ui.aiDisabledHint) }
                                    return@Button
                                }

                                // cloud -> require key
                                if ((mode == AiMode.OPENAI || mode == AiMode.GEMINI || mode == AiMode.CLAUDE) && apiKey.isBlank()) {
                                    scope.launch { snackbar.showSnackbar(ui.aiNeedApiKey) }
                                    return@Button
                                }

                                input = ""
                                val newChat = chat + AiMessage(Role.USER, q)
                                chat = newChat
                                sending = true

                                scope.launch {
                                    val res = AiRouter.reply(
                                        mode = mode,
                                        apiKey = apiKey,
                                        messages = newChat
                                    )
                                    sending = false

                                    when (res) {
                                        is AiResult.Ok -> {
                                            chat = chat + AiMessage(Role.ASSISTANT, res.text)
                                        }
                                        is AiResult.Error -> {
                                            // show error inline + snack
                                            chat = chat + AiMessage(Role.ASSISTANT, "${ui.aiErrorPrefix} ${res.message}")
                                            snackbar.showSnackbar("${ui.aiErrorPrefix} ${res.message}")
                                        }
                                    }
                                }
                            },
                            enabled = !sending
                        ) {
                            Text(if (sending) ui.aiSending else ui.aiSend)
                        }

                        OutlinedButton(
                            onClick = {
                                // reset to hello
                                chat = listOf(
                                    AiMessage(Role.SYSTEM, ui.aiSystemPrompt),
                                    AiMessage(Role.ASSISTANT, ui.aiHello)
                                )
                            }
                        ) { Text(ui.aiClear) }
                    }

                    SnackbarHost(hostState = snackbar)
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}