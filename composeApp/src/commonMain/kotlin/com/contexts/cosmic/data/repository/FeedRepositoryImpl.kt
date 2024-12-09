/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.data.repository

import com.contexts.cosmic.data.network.api.FeedAPI
import com.contexts.cosmic.data.network.httpclient.Response
import com.contexts.cosmic.domain.model.FeedResponse
import com.contexts.cosmic.domain.repository.FeedRepository
import com.contexts.cosmic.exceptions.NetworkError

class FeedRepositoryImpl(private val feedAPI: FeedAPI) : FeedRepository {
    override suspend fun getTimeline(): Response<FeedResponse, NetworkError> {
        return feedAPI.getTimeline()
    }
}
