/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.data.datastore

import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import kotlin.reflect.KClass

class PreferencesDataSourceImpl() : PreferencesDataSource {
    @OptIn(ExperimentalForeignApi::class)
    val producePath = {
        val documentDirectory: NSURL? =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
        requireNotNull(documentDirectory).path + "/$DATA_STORE_FILE_NAME"
    }
    private val dataStore by lazy {
        PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = null,
            migrations = emptyList(),
            produceFile = { producePath().toPath() },
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getValue(
        key: String,
        defaultValue: T,
        type: KClass<T>,
    ): Flow<T> {
        return dataStore.data.catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            when (type) {
                String::class -> preferences[stringPreferencesKey(key)] as? T ?: defaultValue
                Int::class -> preferences[intPreferencesKey(key)] as? T ?: defaultValue
                Boolean::class -> preferences[booleanPreferencesKey(key)] as? T ?: defaultValue
                else -> throw IllegalArgumentException("Unsupported Type")
            }
        }
    }

    override suspend fun <T : Any> editValue(
        key: String,
        value: T,
    ) {
        dataStore.edit { preferences ->
            when (value) {
                is String -> preferences[stringPreferencesKey(key)] = value
                is Int -> preferences[intPreferencesKey(key)] = value
                is Boolean -> preferences[booleanPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Unsupported Type")
            }
        }
    }

    override suspend fun <T : Any> clear(
        key: String,
        type: KClass<T>,
    ) {
        dataStore.edit { preferences ->
            when (type) {
                String::class -> preferences.remove(stringPreferencesKey(key))
                Int::class -> preferences.remove(intPreferencesKey(key))
                Boolean::class -> preferences.remove(booleanPreferencesKey(key))
                else -> throw IllegalArgumentException("Unsupported Type")
            }
            preferences.remove(stringPreferencesKey(key))
        }
    }
}

internal const val DATA_STORE_FILE_NAME = "cosmic.preferences_pb"
