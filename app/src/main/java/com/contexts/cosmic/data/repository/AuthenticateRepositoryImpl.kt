/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.data.repository

import com.atproto.server.CreateSessionRequest
import com.atproto.server.CreateSessionResponse
import com.contexts.cosmic.data.local.database.entities.ProfileEntity
import com.contexts.cosmic.data.network.api.AuthenticateAPI
import com.contexts.cosmic.data.network.client.Response
import com.contexts.cosmic.data.network.client.onSuccess
import com.contexts.cosmic.domain.model.toUser
import com.contexts.cosmic.domain.repository.AuthenticateRepository
import com.contexts.cosmic.domain.repository.PreferencesRepository
import com.contexts.cosmic.domain.repository.ProfileRepository
import com.contexts.cosmic.domain.repository.UserRepository
import com.contexts.cosmic.exceptions.NetworkError

class AuthenticateRepositoryImpl(
    private val api: AuthenticateAPI,
    private val userRepository: UserRepository,
    private val preferencesRepository: PreferencesRepository,
    private val profileRepository: ProfileRepository,
) : AuthenticateRepository {
    override suspend fun createSession(createSessionRequest: CreateSessionRequest): Response<CreateSessionResponse, NetworkError> {
        return api.createSession(createSessionRequest)
            .onSuccess {
                preferencesRepository.updateCurrentUser(it.did.did)
                userRepository.insertUser(it.toUser())
                profileRepository.getProfile(it.did.did).onSuccess { response ->
                    profileRepository.insertProfile(
                        ProfileEntity(
                            userDid = it.did.did,
                            handle = response.handle.handle,
                            displayName = response.displayName,
                            banner = response.banner?.uri,
                            avatar = response.avatar?.uri,
                            description = response.description,
                            followersCount = response.followersCount,
                            followsCount = response.followsCount,
                            postsCount = response.postsCount,
                        ),
                    )
                }
            }
    }
}
