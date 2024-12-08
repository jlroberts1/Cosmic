package com.contexts.cosmic.domain.repository

import com.contexts.cosmic.data.network.model.Token
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun getTokens(): Flow<BearerTokens?>

    suspend fun clearTokens()

    suspend fun putTokens(token: Token)
}