/*
 * Copyright (c) 2024. James Roberts
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.contexts.cosmic.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

interface Dimensions {
    val paddingExtraSmall: Dp
    val paddingSmall: Dp
    val paddingNormal: Dp
    val paddingLarge: Dp
    val paddingExtraLarge: Dp
    val normalButtonHeight: Dp
    val minButtonWidth: Dp
}

val normalDimensions: Dimensions =
    object : Dimensions {
        override val paddingExtraSmall: Dp get() = 4.dp
        override val paddingSmall: Dp get() = 8.dp
        override val paddingNormal: Dp get() = 16.dp
        override val paddingLarge: Dp get() = 24.dp
        override val paddingExtraLarge: Dp get() = 32.dp
        override val normalButtonHeight: Dp get() = 48.dp
        override val minButtonWidth: Dp get() = 120.dp
    }
