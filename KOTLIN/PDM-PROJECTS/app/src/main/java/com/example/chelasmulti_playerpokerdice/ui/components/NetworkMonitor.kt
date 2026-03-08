package com.example.chelasmulti_playerpokerdice.ui.components

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface NetworkMonitor {
    fun isNetworkAvailable(): Boolean
    fun observeNetworkConnectivity(): Flow<Boolean>
}

class AndroidNetworkMonitor(private val context: Context) : NetworkMonitor {
    override fun isNetworkAvailable(): Boolean {
        val state = context.getNetworkState()
        return state.hasNetwork && state.hasInternet
    }

    override fun observeNetworkConnectivity(): Flow<Boolean> =
        context.observeNetworkState().map { it.hasNetwork && it.hasInternet }
}

