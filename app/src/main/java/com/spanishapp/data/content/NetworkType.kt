package com.spanishapp.data.content

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Three states a phone's data connection can be in for our purposes.
 * NONE = airplane mode / no signal — block downloads.
 * MOBILE = cellular only — warn user, let them choose.
 * WIFI = Wi-Fi or Ethernet — green light, just download.
 */
enum class NetworkType { NONE, MOBILE, WIFI }

private fun currentType(cm: ConnectivityManager): NetworkType {
    val active = cm.activeNetwork ?: return NetworkType.NONE
    val caps = cm.getNetworkCapabilities(active) ?: return NetworkType.NONE
    return when {
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.WIFI
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
        else -> NetworkType.NONE
    }
}

/** Cold Flow that emits the current network type and every change. */
fun networkTypeFlow(context: Context): Flow<NetworkType> = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) { trySend(currentType(cm)) }
        override fun onLost(network: android.net.Network)      { trySend(currentType(cm)) }
        override fun onCapabilitiesChanged(
            network: android.net.Network,
            networkCapabilities: NetworkCapabilities,
        ) { trySend(currentType(cm)) }
    }
    trySend(currentType(cm))
    cm.registerDefaultNetworkCallback(callback)
    awaitClose { cm.unregisterNetworkCallback(callback) }
}.distinctUntilChanged()

/** Composable convenience — collects [networkTypeFlow] into a Compose state. */
@Composable
fun rememberNetworkType(): NetworkType {
    val context = LocalContext.current
    val flow = remember { networkTypeFlow(context) }
    val state by flow.collectAsStateWithLifecycle(initialValue = NetworkType.NONE)
    return state
}
