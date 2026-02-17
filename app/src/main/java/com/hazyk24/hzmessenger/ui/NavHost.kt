package com.hazyk24.hzmessenger.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hazyk24.hzmessenger.MainViewModel
import com.hazyk24.hzmessenger.UiRoute
import com.hazyk24.hzmessenger.ui.screens.ChatScreen
import com.hazyk24.hzmessenger.ui.screens.ChatsScreen
import com.hazyk24.hzmessenger.ui.screens.LoginScreen

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val route by vm.route.collectAsState()

    when (route) {
        is UiRoute.Login -> LoginScreen(vm)
        is UiRoute.Chats -> ChatsScreen(vm)
        is UiRoute.Chat -> {
            val chatId = (route as UiRoute.Chat).chatId
            ChatScreen(vm, chatId)
        }
    }
}
