package com.contexts.cosmic.ui.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.contexts.cosmic.domain.model.EmbedView
import com.contexts.cosmic.domain.model.FeedViewPost
import com.contexts.cosmic.extensions.toRelativeTime
import com.contexts.cosmic.ui.screens.profile.composables.EmbedExternalView
import com.contexts.cosmic.ui.screens.profile.composables.EmbedImageView
import com.contexts.cosmic.ui.screens.profile.composables.EmbedRecordView
import com.contexts.cosmic.ui.screens.profile.composables.EmbedVideoView
import sh.calvin.autolinktext.AutoLinkText

@Composable
fun FeedItem(feedPost: FeedViewPost) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(
            modifier =
                Modifier
                    .fillMaxWidth(),
            thickness = 1.dp,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                model = feedPost.post.author.avatar,
                contentDescription = "Profile avatar",
                modifier =
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.surface,
                            CircleShape,
                        ),
                contentScale = ContentScale.Crop,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = feedPost.post.author.displayName ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = feedPost.post.author.handle ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = feedPost.post.indexedAt.toRelativeTime(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
                AutoLinkText(
                    text = feedPost.post.record.text,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                )
                Spacer(modifier = Modifier.padding(4.dp))
                feedPost.post.embed?.let { embed ->
                    when (embed) {
                        is EmbedView.RecordWithMedia -> {
                        }

                        is EmbedView.Video -> {
                            EmbedVideoView(
                                embed.thumbnail,
                                embed.playlist,
                                embed.aspectRatio,
                            )
                        }

                        is EmbedView.Record -> {
                            EmbedRecordView(embed)
                        }

                        is EmbedView.External -> {
                            EmbedExternalView(
                                embed.external.uri,
                                embed.external.thumb,
                                embed.external.title,
                                embed.external.description,
                            )
                        }

                        is EmbedView.Images -> {
                            EmbedImageView(embed.images)
                        }
                    }
                }
            }
        }
    }
}
