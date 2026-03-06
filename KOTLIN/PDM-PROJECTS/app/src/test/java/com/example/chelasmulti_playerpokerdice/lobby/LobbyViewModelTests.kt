package com.example.chelasmulti_playerpokerdice.lobby

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.FakeGameService
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.FailingFakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import com.example.chelasmulti_playerpokerdice.testutils.GameStartingFakeLobbyService
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LobbyViewModelTests {

    @Test
    fun changes_to_data_state_when_lobby_is_loaded() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id",
        )
        val latch = SuspendingLatch()
        var result: LobbyScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyScreenState.Data) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbyScreenState.Data) {
            "Expected Data state, but got $result"
        }

        val data = result as LobbyScreenState.Data
        assert(data.lobby.id == "test-lobby-id") {
            "Expected lobby id to be 'test-lobby-id'"
        }
    }

    @Test
    fun changes_to_error_state_when_lobby_load_fails() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FailingFakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id",
        )
        val latch = SuspendingLatch()
        var result: LobbyScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyScreenState.Error) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbyScreenState.Error) {
            "Expected Error state, but got $result"
        }
    }

    @Test
    fun emits_game_started_when_game_starts() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = GameStartingFakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id",
        )
        val latch = SuspendingLatch()
        var result = false

        val collectorJob = launch {
            sut.gameStartedFlow.collect {
                if (it) {
                    result = true
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result) {
            "Expected game started to be true"
        }
    }


    @Test
    fun get_lobby_id_returns_correct_id() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FailingFakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id",
        )

        val lobbyId = sut.getLobbyId()

        assert(lobbyId == "test-lobby-id") {
            "Expected lobby id to be 'test-lobby-id', but got $lobbyId"
        }
    }

    @Test
    fun get_player_name_returns_correct_name() = runTest {
        val sut = LobbyViewModel(
            gameService = FakeGameService(),
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id",
        )

        val playerName = sut.getPlayerName()

        assert(playerName == "testUser") {
            "Expected player name to be 'testUser', but got $playerName"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}
