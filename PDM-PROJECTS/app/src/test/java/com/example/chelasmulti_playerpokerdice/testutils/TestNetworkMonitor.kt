package com.example.chelasmulti_playerpokerdice.testutils

import com.example.chelasmulti_playerpokerdice.ui.components.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Fake NetworkMonitor implementation for testing.
 * Allows controlling network availability state for test scenarios.
 */
class FakeNetworkMonitor(
    initialNetworkAvailable: Boolean = true
) : NetworkMonitor {

    private val _networkAvailability = MutableStateFlow(initialNetworkAvailable)

    override fun isNetworkAvailable(): Boolean = _networkAvailability.value

    override fun observeNetworkConnectivity(): Flow<Boolean> = _networkAvailability
}

