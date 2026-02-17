package com.hazyk24.hzmessenger.tg

import android.content.Context
import com.hazyk24.hzmessenger.BuildConfig
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * Thin wrapper around TDLib client.
 * It exposes auth state and TDLib updates via flows.
 */
class TdLibManager(private val context: Context) {

    private val clientRef = AtomicReference<Client?>(null)

    private val _updates = MutableSharedFlow<TdApi.Object>(
        extraBufferCapacity = 256,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val updates: SharedFlow<TdApi.Object> = _updates

    @Volatile
    var authState: TdApi.AuthorizationState? = null
        private set

    fun start() {
        if (clientRef.get() != null) return

        Client.execute(TdApi.SetLogVerbosityLevel(1))

        val handler = Client.ResultHandler { obj ->
            if (obj is TdApi.UpdateAuthorizationState) {
                authState = obj.authorizationState
                handleAuth(obj.authorizationState)
            }
            _updates.tryEmit(obj)
        }

        val client = Client.create(handler, null, null)
        clientRef.set(client)
    }

    private fun handleAuth(state: TdApi.AuthorizationState) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> setParams()
            is TdApi.AuthorizationStateWaitEncryptionKey -> {
                clientRef.get()?.send(TdApi.CheckDatabaseEncryptionKey(), null)
            }
        }
    }

    private fun setParams() {
        val filesDir = context.filesDir
        val dbDir = File(filesDir, "tdlib").absolutePath
        val files = File(filesDir, "tdlib_files").absolutePath

        val params = TdApi.TdlibParameters().apply {
            databaseDirectory = dbDir
            filesDirectory = files

            useMessageDatabase = true
            useSecretChats = false

            apiId = BuildConfig.TG_API_ID
            apiHash = BuildConfig.TG_API_HASH

            systemLanguageCode = "ru"
            deviceModel = android.os.Build.MODEL ?: "Android"
            systemVersion = android.os.Build.VERSION.RELEASE ?: "0"
            applicationVersion = "1.0.0"
            enableStorageOptimizer = true
        }

        clientRef.get()?.send(TdApi.SetTdlibParameters(params), null)
        clientRef.get()?.send(TdApi.CheckDatabaseEncryptionKey(), null)
    }

    fun setPhoneNumber(phone: String) {
        clientRef.get()?.send(TdApi.SetAuthenticationPhoneNumber(phone, null), null)
    }

    fun checkCode(code: String) {
        clientRef.get()?.send(TdApi.CheckAuthenticationCode(code), null)
    }

    fun checkPassword(password: String) {
        clientRef.get()?.send(TdApi.CheckAuthenticationPassword(password), null)
    }

    fun getMe(onResult: (TdApi.User?) -> Unit) {
        clientRef.get()?.send(TdApi.GetMe(), Client.ResultHandler { obj ->
            onResult(obj as? TdApi.User)
        })
    }

    fun getChats(limit: Int, onResult: (TdApi.Chats?) -> Unit) {
        clientRef.get()?.send(TdApi.GetChats(null, limit), Client.ResultHandler { obj ->
            onResult(obj as? TdApi.Chats)
        })
    }

    fun getChat(chatId: Long, onResult: (TdApi.Chat?) -> Unit) {
        clientRef.get()?.send(TdApi.GetChat(chatId), Client.ResultHandler { obj ->
            onResult(obj as? TdApi.Chat)
        })
    }

    fun getHistory(chatId: Long, fromMessageId: Long = 0L, limit: Int = 50, onResult: (TdApi.Messages?) -> Unit) {
        clientRef.get()?.send(
            TdApi.GetChatHistory(chatId, fromMessageId, 0, limit, false),
            Client.ResultHandler { obj -> onResult(obj as? TdApi.Messages) }
        )
    }

    fun sendText(chatId: Long, text: String) {
        val content = TdApi.InputMessageText(
            TdApi.FormattedText(text, null),
            false,
            true
        )
        clientRef.get()?.send(TdApi.SendMessage(chatId, 0, null, null, content), null)
    }
}
