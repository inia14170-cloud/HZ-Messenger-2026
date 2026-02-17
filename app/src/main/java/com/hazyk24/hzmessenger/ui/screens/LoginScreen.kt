package com.hazyk24.hzmessenger.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hazyk24.hzmessenger.MainViewModel
import com.hazyk24.hzmessenger.LoginUiState

@Composable
fun LoginScreen(vm: MainViewModel) {
    val state by vm.login.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("HZ Messenger")
        Spacer(Modifier.height(16.dp))

        when (state.stage) {
            LoginUiState.Stage.Loading -> {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("Подключаем Telegram…")
            }
            LoginUiState.Stage.Phone -> {
                Text("Введите номер телефона")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = vm::setPhone,
                    label = { Text("+7…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxSize(fraction = 0.0f)
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = vm::submitPhone) { Text("Далее") }
            }
            LoginUiState.Stage.Code -> {
                Text("Введите код из Telegram/SMS")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.code,
                    onValueChange = vm::setCode,
                    label = { Text("Код") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = vm::submitCode) { Text("Подтвердить") }
            }
            LoginUiState.Stage.Password -> {
                Text("Введите пароль 2FA")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.password,
                    onValueChange = vm::setPassword,
                    label = { Text("Пароль") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = vm::submitPassword) { Text("Войти") }
            }
            LoginUiState.Stage.Ready -> {
                Text("Готово ✅")
            }
        }

        state.error?.let {
            Spacer(Modifier.height(12.dp))
            Text("Ошибка: $it")
        }
    }
}
