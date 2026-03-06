package com.example.chelasmulti_playerpokerdice.game

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.FakeGameService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GameNetworkConnectivityTests {

    @Test
    fun changes_to_no_network_state_when_network_is_unavailable() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false),
            lobbyId = "test-lobby-id"
        )
        val latch = SuspendingLatch()
        var result: GameScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is GameScreenState.NoNetwork) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is GameScreenState.NoNetwork) {
            "Expected NoNetwork state when network is unavailable, but got $result"
        }
    }

    @Test
    fun network_availability_flow_reflects_network_status() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = true),
            lobbyId = "test-lobby-id"
        )

        delay(100)

        assert(sut.isNetworkAvailable.value) {
            "Expected network to be available"
        }
    }

    @Test
    fun error_message_is_set_when_action_without_network() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false),
            lobbyId = "test-lobby-id"
        )

        delay(100)

        sut.onLeaveGame()

        delay(100)

        assert(sut.errorMessageFlow.value == "No internet connection. Please check your network.") {
            "Expected error message for no internet connection, but got '${sut.errorMessageFlow.value}'"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}