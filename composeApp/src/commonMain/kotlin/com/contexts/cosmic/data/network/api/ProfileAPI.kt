package com.contexts.cosmic.data.network.api

import com.contexts.cosmic.data.network.httpclient.Response
import com.contexts.cosmic.data.network.httpclient.safeRequest
import com.contexts.cosmic.data.network.model.response.ProfileDTO
import com.contexts.cosmic.domain.model.FeedResponse
import com.contexts.cosmic.exceptions.NetworkError
import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.path

class ProfileAPI(private val client: HttpClient) {
    suspend fun getProfile(actor: String): Response<ProfileDTO, NetworkError> {
        return client.safeRequest {
            url {
                method = HttpMethod.Get
                path("xrpc/app.bsky.actor.getProfile")
                parameters.append("actor", actor)
            }
        }
    }

    suspend fun getMyProfile(myDid: String): Response<ProfileDTO, NetworkError> {
        return getProfile(myDid)
    }

    suspend fun getAuthorFeed(
        actor: String,
        limit: Int = 50,
        cursor: String? = null,
    ): Response<FeedResponse, NetworkError> {
        return client.safeRequest {
            url {
                method = HttpMethod.Get
                path("xrpc/app.bsky.feed.getAuthorFeed")
                parameters.append("actor", actor)
                parameters.append("limit", limit.toString())
                cursor?.let { parameters.append("cursor", it) }
            }
        }
    }
}
