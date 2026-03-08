package com.example.chelasmulti_playerpokerdice.lobby

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.FakeGameService
import com.example.chelasmulti_playerpokerdice.testutils.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LobbyNetworkConnectivityTests {

    @Test
    fun changes_to_no_network_state_when_network_is_unavailable() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false),
            lobbyId = "test-lobby-id"
        )

        val latch = SuspendingLatch()
        var result: LobbyScreenState? = null

        val job = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyScreenState.NoNetwork) latch.open()
            }
        }

        latch.await()
        job.cancel()

        assert(result is LobbyScreenState.NoNetwork) { "Expected NoNetwork state" }
    }

    @Test
    fun network_availability_flow_reflects_network_status() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = true),
            lobbyId = "test-lobby-id"
        )

        delay(50)
        assert(sut.isNetworkAvailable.value) { "Expected network to be available" }
    }

    @Test
    fun require_internet_actions_set_error_message_when_offline() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false),
            lobbyId = "test-lobby-id"
        )

        val deadline = System.currentTimeMillis() + 1000
        while (System.currentTimeMillis() < deadline && sut.isNetworkAvailable.value) {
            delay(20)
        }

        sut.onSendMessage("player1", "hello")

        val emitDeadline = System.currentTimeMillis() + 500
        while (System.currentTimeMillis() < emitDeadline && sut.errorMessageFlow.value.isEmpty()) {
            delay(20)
        }

        assert(sut.errorMessageFlow.value == "No internet connection") {
            "Expected error message for no internet connection, but got '${sut.errorMessageFlow.value}'"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}
