/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.contexts.cosmic.data.local.database.CosmicDatabase
import com.contexts.cosmic.data.local.database.dao.FeedDao
import com.contexts.cosmic.data.local.database.dao.FeedPostDao
import com.contexts.cosmic.data.local.database.dao.RemoteKeysDao
import com.contexts.cosmic.data.local.database.entities.FeedEntity
import com.contexts.cosmic.data.local.database.entities.FeedPostEntity
import com.contexts.cosmic.data.network.client.Response
import com.contexts.cosmic.data.network.client.onSuccess
import com.contexts.cosmic.domain.repository.FeedRepository
import com.contexts.cosmic.domain.repository.ProfileRepository
import com.contexts.cosmic.exceptions.NetworkError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

class FeedManager(
    private val db: CosmicDatabase,
    private val feedDao: FeedDao,
    private val feedPostDao: FeedPostDao,
    private val remoteKeysDao: RemoteKeysDao,
    private val feedRepository: FeedRepository,
    private val profileRepository: ProfileRepository,
) {
    private val feedMediators = ConcurrentHashMap<String, FeedRemoteMediator>()

    private suspend fun registerFeed(feed: FeedEntity) {
        feedDao.insertFeed(feed)
    }

    private suspend fun fetchAndRegisterAvailableFeeds(): Response<List<FeedEntity>, NetworkError> {
        return profileRepository.getSavedFeeds().onSuccess { response ->
            response.forEach { feed ->
                registerFeed(feed)
            }
        }
    }

    val availableFeeds: Flow<List<FeedEntity>> =
        flow {
            emit(feedDao.getAllFeeds())

            fetchAndRegisterAvailableFeeds().onSuccess { feeds ->
                emit(feeds)
            }
        }.distinctUntilChanged()

    private fun getOrCreateMediator(
        feedId: String,
        feedUri: String,
    ): FeedRemoteMediator {
        return feedMediators.getOrPut(feedId) {
            FeedRemoteMediator(
                feedId = feedId,
                feedUri = feedUri,
                feedRepository = feedRepository,
                feedPostDao = feedPostDao,
                remoteKeysDao = remoteKeysDao,
                db = db,
            )
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getFeedPagingFlow(
        feedId: String,
        feedUri: String,
    ): Flow<PagingData<FeedPostEntity>> {
        return Pager(
            config =
                PagingConfig(
                    pageSize = 15,
                    prefetchDistance = 5,
                    enablePlaceholders = false,
                ),
            remoteMediator = getOrCreateMediator(feedId, feedUri),
        ) {
            feedPostDao.getPostsForFeed(feedId)
        }.flow
    }
}