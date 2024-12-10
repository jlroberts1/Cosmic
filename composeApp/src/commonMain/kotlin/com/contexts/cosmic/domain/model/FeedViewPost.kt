package com.contexts.cosmic.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class FeedViewPost(
    val post: Post,
    val reply: ReplyReference? = null,
    // val reason: FeedReason? = null,
)
