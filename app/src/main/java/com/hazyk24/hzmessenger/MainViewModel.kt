package com.hazyk24.hzmessenger

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hazyk24.hzmessenger.tg.TdLibManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.drinkless.tdlib.TdApi

sealed interface UiRoute {
    data object Login : UiRoute
    data object Chats : UiRoute
    data class Chat(val chatId: Long) : UiRoute
}

data class LoginUiState(
    val stage: Stage = Stage.Loading,
    val phone: String = "",
    val code: String = "",
    val password: String = "",
    val error: String? = null
) {
    enum class Stage { Loading, Phone, Code, Password, Ready }
}

data class ChatItem(
    val id: Long,
    val title: String,
    val lastMessage: String
)

data class ChatMessage(
    val id: Long,
    val isOutgoing: Boolean,
    val text: String
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val td = TdLibManager(app.applicationContext)

    private val _route = MutableStateFlow<UiRoute>(UiRoute.Login)
    val route: StateFlow<UiRoute> = _route

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login

    private val _chats = MutableStateFlow<List<ChatItem>>(emptyList())
    val chats: StateFlow<List<ChatItem>> = _chats

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        td.start()

        viewModelScope.launch {
            td.updates.collect { obj ->
                if (obj is TdApi.UpdateAuthorizationState) {
                    onAuthState(obj.authorizationState)
                }
                if (obj is TdApi.UpdateNewMessage) {
                    // simple refresh if chat opened
                    val current = _route.value
                    if (current is UiRoute.Chat && current.chatId == obj.message.chatId) {
                        loadChat(current.chatId)
                    }
                }
            }
        }
    }

    private fun onAuthState(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters,
            is TdApi.AuthorizationStateWaitEncryptionKey -> {
                _login.value = _login.value.copy(stage = LoginUiState.Stage.Loading, error = null)
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> {
                _login.value = _login.value.copy(stage = LoginUiState.Stage.Phone, error = null)
            }
            is TdApi.AuthorizationStateWaitCode -> {
                _login.value = _login.value.copy(stage = LoginUiState.Stage.Code, error = null)
            }
            is TdApi.AuthorizationStateWaitPassword -> {
                _login.value = _login.value.copy(stage = LoginUiState.Stage.Password, error = null)
            }
            is TdApi.AuthorizationStateReady -> {
                _login.value = _login.value.copy(stage = LoginUiState.Stage.Ready, error = null)
                _route.value = UiRoute.Chats
                refreshChats()
            }
            is TdApi.AuthorizationStateClosed -> {
                _route.value = UiRoute.Login
            }
        }
    }

    fun setPhone(phone: String) {
        _login.value = _login.value.copy(phone = phone, error = null)
    }

    fun submitPhone() {
        td.setPhoneNumber(_login.value.phone.trim())
    }

    fun setCode(code: String) {
        _login.value = _login.value.copy(code = code, error = null)
    }

    fun submitCode() {
        td.checkCode(_login.value.code.trim())
    }

    fun setPassword(pwd: String) {
        _login.value = _login.value.copy(password = pwd, error = null)
    }

    fun submitPassword() {
        td.checkPassword(_login.value.password)
    }

    fun refreshChats() {
        td.getChats(200) { chats ->
            val ids = chats?.chatIds?.toList().orEmpty()
            if (ids.isEmpty()) {
                _chats.value = emptyList()
                return@getChats
            }

            // Fetch each chat title + last message
            val list = mutableListOf<ChatItem>()
            ids.take(50).forEach { id ->
                td.getChat(id) { chat ->
                    if (chat != null) {
                        val last = extractMessageText(chat.lastMessage)
                        list.add(ChatItem(id, chat.title ?: "Chat", last))
                        _chats.value = list.sortedByDescending { it.id }
                    }
                }
            }
        }
    }

    fun openChat(chatId: Long) {
        _route.value = UiRoute.Chat(chatId)
        loadChat(chatId)
    }

    fun backToChats() {
        _route.value = UiRoute.Chats
        _messages.value = emptyList()
        refreshChats()
    }

    fun loadChat(chatId: Long) {
        td.getHistory(chatId, 0, 50) { msgs ->
            val list = msgs?.messages?.mapNotNull { m ->
                val text = extractMessageText(m)
                if (text.isBlank()) null else ChatMessage(m.id, m.isOutgoing, text)
            }.orEmpty()
            _messages.value = list.reversed()
        }
    }

    fun send(chatId: Long, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        td.sendText(chatId, trimmed)
    }

    private fun extractMessageText(message: TdApi.Message?): String {
        if (message == null) return ""
        return extractMessageText(message.content)
    }

    private fun extractMessageText(content: TdApi.MessageContent?): String {
        return when (content) {
            is TdApi.MessageText -> content.text?.text ?: ""
            is TdApi.MessageCaption -> content.caption?.text ?: ""
            else -> ""
        }
    }
}
