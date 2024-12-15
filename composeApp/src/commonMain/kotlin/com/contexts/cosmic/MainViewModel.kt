/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contexts.cosmic.data.network.httpclient.AuthManager
import com.contexts.cosmic.data.network.httpclient.Response
import com.contexts.cosmic.data.repository.Theme
import com.contexts.cosmic.domain.repository.NotificationsRepository
import com.contexts.cosmic.domain.repository.PreferencesRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthenticationState {
    data object Authenticated : AuthenticationState

    data object Unauthenticated : AuthenticationState
}

class MainViewModel(
    private val authManager: AuthManager,
    private val notificationsRepository: NotificationsRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthenticationState?>(null)
    val authState = _authState.asStateFlow()

    private val _scaffoldViewState = MutableStateFlow(ScaffoldViewState())
    val scaffoldViewState = _scaffoldViewState.asStateFlow()

    private val _controlsVisibility = MutableStateFlow(1f)
    val controlsVisibility = _controlsVisibility.asStateFlow()

    val unreadCount =
        preferencesRepository.getUnreadCount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0L)

    val currentTheme =
        preferencesRepository.getTheme()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), Theme.SYSTEM)

    fun updateControlsVisibility(scrollDelta: Float) {
        _controlsVisibility.update { currentVisibility ->
            if (scrollDelta < 0) {
                (currentVisibility - (-scrollDelta / 100f)).coerceIn(0f, 1f)
            } else {
                (currentVisibility + (scrollDelta / 100f)).coerceIn(0f, 1f)
            }
        }
    }

    init {
        viewModelScope.launch {
            authManager.getAuthStateFlow().collect { authState ->
                _authState.value =
                    when (authState) {
                        null -> AuthenticationState.Unauthenticated
                        else -> AuthenticationState.Authenticated
                    }
            }
        }
        getUnreadCount()
    }

    private fun getUnreadCount() {
        viewModelScope.launch {
            when (val response = notificationsRepository.getUnreadCount()) {
                is Response.Success -> {
                    preferencesRepository.updateUnreadCount(response.data.count)
                }
                is Response.Error -> {
                    Napier.e("Error getting unread count ${response.error}")
                }
            }
        }
    }

    fun updateScaffoldViewState(newState: ScaffoldViewState) {
        _scaffoldViewState.value = newState
    }
}
