/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowWidthSizeClass
import com.contexts.cosmic.data.local.database.entities.FeedEntity
import com.contexts.cosmic.data.local.database.entities.FeedPostEntity
import com.contexts.cosmic.ui.composables.FeedItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    controlsVisibility: Float,
    onMediaOpen: (String) -> Unit,
) {
    val viewModel: HomeViewModel = koinViewModel()
    val visibleFeeds by viewModel.visibleFeeds.collectAsStateWithLifecycle()
    val allFeeds by viewModel.allFeeds.collectAsStateWithLifecycle()
    val feedStates by viewModel.feedStates.collectAsStateWithLifecycle()
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpandedScreen =
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isExpandedScreen) {
            if (feedStates.isEmpty()) return
            ScrollableTabRow(
                selectedTabIndex = -1,
                modifier = Modifier.fillMaxWidth(),
                indicator = @Composable { _ -> },
                divider = @Composable { },
            ) {
                visibleFeeds.forEachIndexed { index, feedType ->
                    Tab(
                        selected = true,
                        onClick = { },
                        text = { Text(feedType.displayName) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            TabletRow(
                feeds = visibleFeeds,
                feedStates = feedStates,
                onMediaOpen = { onMediaOpen(it) },
                controlsVisibility,
            )
        } else {
            TabPagerFeeds(
                feeds = visibleFeeds,
                feedStates = feedStates,
                onMediaOpen = { onMediaOpen(it) },
                controlsVisibility = controlsVisibility,
            )
        }
//        if (showFeedConfig) {
//            FeedConfigurationDialog(
//                visibleFeeds = visibleFeeds,
//                availableFeeds = availableFeeds,
//                onDismiss = { onDismissFeedConfig() },
//                onSaveConfiguration = { newVisibleFeeds ->
//                    //viewModel.updateVisibleFeeds(newVisibleFeeds)
//                    onDismissFeedConfig()
//                }
//            )
//        }
    }
}

@Composable
fun FeedConfigurationDialog(
    visibleFeeds: List<FeedEntity>,
    availableFeeds: List<FeedEntity>,
    onDismiss: () -> Unit,
    onSaveConfiguration: (List<FeedEntity>) -> Unit,
) {
    var selectedFeeds by remember { mutableStateOf(visibleFeeds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Feeds") },
        text = {
            LazyColumn {
                items(availableFeeds) { feed ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selectedFeeds.contains(feed),
                            onCheckedChange = { checked ->
                                selectedFeeds =
                                    if (checked) {
                                        selectedFeeds + feed
                                    } else {
                                        selectedFeeds - feed
                                    }
                            },
                        )
                        Text(feed.displayName)
                        Icon(
                            Icons.Default.DragIndicator,
                            contentDescription = "Drag to reorder",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSaveConfiguration(selectedFeeds) },
                enabled = selectedFeeds.isNotEmpty(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun TabletRow(
    feeds: List<FeedEntity>,
    feedStates: Map<String, Flow<PagingData<FeedPostEntity>>>,
    onMediaOpen: (String) -> Unit,
    controlsVisibility: Float,
) {
    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        feeds.forEach { feed ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
            ) {
                feedStates[feed.id]?.let { pagingFlow ->
                    FeedContent(
                        pagingFlow = pagingFlow,
                        onMediaOpen = onMediaOpen,
                        controlsVisibility = controlsVisibility,
                    )
                }
            }
        }
    }
}

@Composable
fun TabPagerFeeds(
    feeds: List<FeedEntity>,
    feedStates: Map<String, Flow<PagingData<FeedPostEntity>>>,
    onMediaOpen: (String) -> Unit,
    controlsVisibility: Float,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState { feeds.size }
    if (feeds.isEmpty()) return
    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        edgePadding = 16.dp,
    ) {
        feeds.forEachIndexed { index, feed ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                text = {
                    Text(feed.displayName)
                },
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxSize(),
        ) { page ->
            val feed = feeds[page]
            feedStates[feed.id]?.let { pagingFlow ->
                FeedContent(
                    pagingFlow = pagingFlow,
                    onMediaOpen = { onMediaOpen(it) },
                    controlsVisibility = controlsVisibility,
                )
            }
        }
    }
}

@Composable
private fun FeedContent(
    pagingFlow: Flow<PagingData<FeedPostEntity>>,
    onMediaOpen: (String) -> Unit,
    controlsVisibility: Float,
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpandedScreen =
        adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

    val posts = pagingFlow.collectAsLazyPagingItems()
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier =
                Modifier
                    .padding(horizontal = 4.dp)
                    .fillMaxSize(),
            state = listState,
        ) {
            items(
                count = posts.itemCount,
                key = { index -> posts[index]?.postUri ?: index },
            ) { item ->
                posts[item]?.let {
                    FeedItem(
                        post = it,
                        onReplyClick = {},
                        onRepostClick = {},
                        onLikeClick = {},
                        onMenuClick = {},
                        onMediaOpen = { onMediaOpen(it) },
                    )
                }
            }
        }
        if (showScrollToTop) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = if (isExpandedScreen) 16.dp else 104.dp, start = 16.dp)
                        .graphicsLayer {
                            if (!isExpandedScreen) translationY = 200f * (1f - controlsVisibility)
                        },
            ) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    "Scroll to top",
                )
            }
        }
    }
}
