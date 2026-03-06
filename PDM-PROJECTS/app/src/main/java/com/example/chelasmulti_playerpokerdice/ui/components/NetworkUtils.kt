package com.example.chelasmulti_playerpokerdice.ui.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

data class NetworkState(
    val hasNetwork: Boolean,
    val hasInternet: Boolean
)

private suspend fun checkInternetBySocket(timeoutMs: Int = 1500): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), timeoutMs)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}

fun Context.getNetworkState(): NetworkState {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return NetworkState(false, false)
    val caps = cm.getNetworkCapabilities(network) ?: return NetworkState(false, false)

    val hasNetwork = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

    val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

    return NetworkState(hasNetwork, hasInternet)
}

fun Context.observeNetworkState(): Flow<NetworkState> = callbackFlow {
    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var lastKnownState: NetworkState? = null

    val updateState: suspend (Network?) -> Unit = { network ->
        val caps = network?.let { cm.getNetworkCapabilities(it) }
        val hasNetwork = caps?.let {
            it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false

        val hasInternet = if (hasNetwork) {
            checkInternetBySocket(800)
        } else {
            false
        }

        val newState = NetworkState(hasNetwork, hasInternet)
        if (newState != lastKnownState) {
            lastKnownState = newState
            trySend(newState)
        }
    }

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            launch { updateState(network) }
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            launch { updateState(network) }
        }

        override fun onLost(network: Network) {
            launch { updateState(null) }
        }
    }

    val request = NetworkRequest.Builder()
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
        .build()

    launch { updateState(cm.activeNetwork) }

    cm.registerNetworkCallback(request, callback)

    launch {
        while (currentCoroutineContext().isActive) {
            delay(1000)
            val network = cm.activeNetwork
            updateState(network)
        }
    }

    awaitClose { cm.unregisterNetworkCallback(callback) }
}
