/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.data.network.api

import com.contexts.cosmic.data.network.httpclient.Response
import com.contexts.cosmic.data.network.httpclient.invalidateBearerTokens
import com.contexts.cosmic.data.network.httpclient.safeRequest
import com.contexts.cosmic.data.network.model.request.CreateSessionRequest
import com.contexts.cosmic.data.network.model.response.CreateSessionResponse
import com.contexts.cosmic.exceptions.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.request.setBody
import io.ktor.http.HttpMethod
import io.ktor.http.path

class AuthenticateAPI(private val client: HttpClient) {
    suspend fun createSession(
        identifier: String,
        password: String,
    ): Response<CreateSessionResponse, NetworkError> {
        val request = CreateSessionRequest(identifier, password)
        return client.safeRequest<CreateSessionResponse> {
            url {
                method = HttpMethod.Post
                path("xrpc/com.atproto.server.createSession")
            }
            setBody(request)
        }
    }

    suspend fun clearToken() {
        client.invalidateBearerTokens()
    }
}
