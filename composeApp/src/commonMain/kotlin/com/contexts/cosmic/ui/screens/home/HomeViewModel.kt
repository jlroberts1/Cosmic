/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bsky.feed.FeedViewPost
import com.contexts.cosmic.data.network.httpclient.Response
import com.contexts.cosmic.domain.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val feed: List<FeedViewPost> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)

class HomeViewModel(
    private val feedRepository: FeedRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val response = feedRepository.getDefaultFeed()) {
                is Response.Success -> {
                    _uiState.update {
                        it.copy(
                            feed = response.data.feed,
                            loading = false,
                            error = null,
                        )
                    }
                }

                is Response.Error -> {
                    _uiState.update { it.copy(loading = false, error = response.error.message) }
                }
            }
        }
    }
}
