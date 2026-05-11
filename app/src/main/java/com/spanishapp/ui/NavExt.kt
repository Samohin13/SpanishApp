package com.spanishapp.ui

import androidx.navigation.NavController

/**
 * Single-shot navigation helper used by every "open this screen" tap
 * across the app.
 *
 * - launchSingleTop = true → tapping the same destination twice doesn't
 *   pile duplicate entries on the back stack, which was the root cause
 *   of "I tap Settings, return, tap again — nothing happens": the second
 *   navigate() noticed an already-existing top entry and silently
 *   produced no UI change.
 *
 * Call sites use this instead of navController.navigate(route) wherever
 * a button opens a non-tab screen (settings, course detail, lesson, etc.).
 */
fun NavController.navigateSafe(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
