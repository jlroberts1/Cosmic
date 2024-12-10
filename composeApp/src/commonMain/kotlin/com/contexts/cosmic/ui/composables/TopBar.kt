package com.contexts.cosmic.ui.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    TopAppBar(
        scrollBehavior = scrollBehavior,
        title = { Text("Cosmic") },
        navigationIcon = {
            if (navController.previousBackStackEntry != null) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            } else {
                null
            }
        },
    )
}
