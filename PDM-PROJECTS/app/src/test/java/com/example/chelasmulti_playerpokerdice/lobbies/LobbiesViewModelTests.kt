package com.example.chelasmulti_playerpokerdice.lobbies

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.EmptyFakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FailingFakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LobbiesViewModelTests {

    @Test
    fun initially_its_in_loading_state() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        var result: LobbiesScreenState? = null
        val latch = SuspendingLatch()

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                latch.open()
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbiesScreenState.Loading) {
            "Expected Loading state, but got $result"
        }
    }

    @Test
    fun changes_to_data_state_when_lobbies_are_received() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbiesScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbiesScreenState.Data) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbiesScreenState.Data) {
            "Expected Data state, but got $result"
        }
    }

    @Test
    fun changes_to_empty_state_when_no_lobbies_exist() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = EmptyFakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbiesScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbiesScreenState.Empty) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbiesScreenState.Empty) {
            "Expected Empty state, but got $result"
        }
    }

    @Test
    fun changes_to_error_state_when_fetch_fails() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FailingFakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbiesScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbiesScreenState.Error) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbiesScreenState.Error) {
            "Expected Error state, but got $result"
        }
    }

    @Test
    fun emits_lobby_session_when_join_succeeds() = runTest {
        val sut = LobbiesViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: String? = null

        val collectorJob = launch {
            sut.lobbySessionFlow.collect {
                result = it
                latch.open()
            }
        }

        // Give time for the collector to start
        delay(100)

        sut.onJoinLobby("lobby1")

        latch.await()
        collectorJob.cancel()

        assert(result == "lobby1") {
            "Expected lobby1, but got $result"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}
