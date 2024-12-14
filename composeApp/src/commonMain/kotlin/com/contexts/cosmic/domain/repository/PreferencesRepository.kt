/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.domain.repository

import com.contexts.cosmic.data.repository.Theme
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    suspend fun updateTheme(theme: Theme)

    fun getTheme(): Flow<Theme>

    suspend fun updateUnreadCount(unreadCount: Long)

    fun getUnreadCount(): Flow<Long>
}
