package com.contexts.cosmic.data.network.httpclient

import com.contexts.cosmic.data.network.model.Token
import com.contexts.cosmic.data.network.model.response.RefreshSessionResponse
import com.contexts.cosmic.domain.model.Embed
import com.contexts.cosmic.domain.model.EmbedView
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

fun HttpClientConfig<*>.setupContentNegotiation() {
    val module =
        SerializersModule {
            polymorphic(Embed::class) {
                subclass(Embed.Images::class, Embed.Images.serializer())
                subclass(Embed.External::class, Embed.External.serializer())
                subclass(Embed.Record::class, Embed.Record.serializer())
                subclass(Embed.RecordWithMedia::class, Embed.RecordWithMedia.serializer())
            }
            polymorphic(EmbedView::class) {
                subclass(EmbedView.Images::class, EmbedView.Images.serializer())
                subclass(EmbedView.External::class, EmbedView.External.serializer())
                subclass(EmbedView.Record::class, EmbedView.Record.serializer())
                subclass(EmbedView.RecordWithMedia::class, EmbedView.RecordWithMedia.serializer())
            }
        }

    install(ContentNegotiation) {
        json(
            Json {
                serializersModule = module
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            },
        )
    }
}

fun HttpClientConfig<*>.setupLogging() {
    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }
}

fun HttpClientConfig<*>.setupDefaultRequest() {
    install(DefaultRequest) {
        url("https://bsky.social")
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)
    }
}

fun HttpClientConfig<*>.setupAuth(authManager: AuthManager) {
    install(Auth) {
        bearer {
            loadTokens {
                val token = authManager.getTokens()
                println("$token")
                token
            }
            refreshTokens {
                authManager.getTokens()?.let { oldTokens ->
                    when (
                        val response =
                            client.safeRequest<RefreshSessionResponse> {
                                markAsRefreshTokenRequest()
                                url {
                                    method = HttpMethod.Post
                                    path("xrpc/com.atproto.server.refreshSession")
                                }
                                header(HttpHeaders.Authorization, "Bearer ${oldTokens.refreshToken}")
                            }
                    ) {
                        is Response.Success -> {
                            val tokens =
                                Token(
                                    accessJwt = response.data.accessJwt,
                                    refreshJwt = response.data.refreshJwt,
                                )
                            authManager.putTokens(tokens)
                            BearerTokens(
                                tokens.accessJwt,
                                tokens.refreshJwt,
                            )
                        }

                        is Response.Error -> {
                            println("Error refreshing token: ${response.error}")
                            null
                        }
                    }
                }
            }
            sendWithoutRequest { request ->
                when (request.url.pathSegments.last()) {
                    "xrpc/com.atproto.server.createSession",
                    "xrpc/com.atproto.server.refreshSession",
                    -> false

                    else -> true
                }
            }
        }
    }
}
