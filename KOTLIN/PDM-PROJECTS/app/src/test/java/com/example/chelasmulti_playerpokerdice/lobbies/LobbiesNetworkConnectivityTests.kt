package com.example.chelasmulti_playerpokerdice.lobbies

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LobbiesNetworkConnectivityTests {

    @Test
    fun changes_to_no_network_state_when_network_is_unavailable() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false)
        )

        val latch = SuspendingLatch()
        var result: LobbiesScreenState? = null

        val job = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbiesScreenState.NoNetwork) latch.open()
            }
        }

        latch.await()
        job.cancel()

        assert(result is LobbiesScreenState.NoNetwork) { "Expected NoNetwork state" }
    }

    @Test
    fun network_availability_flow_reflects_network_status() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = true)
        )

        // small delay to allow collector to start
        delay(50)
        assert(sut.isNetworkAvailable.value) { "Expected network to be available" }
    }

    @Test
    fun action_requires_network_shows_snackbar_when_offline() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false)
        )

        // Try to join lobby while offline
        sut.onJoinLobby("lobby1")

        delay(50)

        assert(sut.snackBarMessageFlow.value == "No internet connection") {
            "Expected snackBarMessage to indicate no internet, but got '${sut.snackBarMessageFlow.value}'"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}

