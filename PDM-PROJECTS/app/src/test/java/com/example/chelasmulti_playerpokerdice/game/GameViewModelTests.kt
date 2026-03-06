package com.example.chelasmulti_playerpokerdice.game

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.DelayedFakeGameService
import com.example.chelasmulti_playerpokerdice.testutils.FakeGameService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import com.example.chelasmulti_playerpokerdice.testutils.GameOverFakeGameService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GameViewModelTests {

    @Test
    fun initially_its_in_loading_state() = runTest {
        val gameService = DelayedFakeGameService()
        val sut = GameViewModel(
            gameService = gameService,
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )

        val result = sut.currentStateFlow.value

        assert(result is GameScreenState.Loading) {
            "Expected Loading state, but got $result"
        }
    }

    @Test
    fun changes_to_playing_state_after_initialization() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )
        val latch = SuspendingLatch()
        var result: GameScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is GameScreenState.Playing) {
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is GameScreenState.Playing) {
            "Expected Playing state, but got $result"
        }

        val playing = result as GameScreenState.Playing
        assert(playing.game.players.size == 2) { "Expected 2 players" }
        assert(playing.game.rounds.numberOfRounds == 4) { "Expected 4 rounds" }
        assert(playing.game.rounds.rollsLeft == 3) { "Expected 3 rolls left" }
    }

    @Test
    fun emits_quit_game_when_onQuitGame_is_called() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )
        val latch = SuspendingLatch()
        var result = false

        val collectorJob = launch {
            sut.quitGameFlow.collect {
                if (it) {
                    result = true
                    latch.open()
                }
            }
        }

        delay(100)

        sut.onQuitGame()

        latch.await()
        collectorJob.cancel()

        assert(result) {
            "Expected quit game to be true"
        }
    }

    @Test
    fun roll_dice_reduces_rolls_left() = runTest {
        val gameService = FakeGameService()
        val sut = GameViewModel(
            gameService = gameService,
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )

        val initialLatch = SuspendingLatch()
        val rollLatch = SuspendingLatch()
        var capturedInitialState = false
        var stateAfterRoll: GameScreenState.Playing? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                if (it is GameScreenState.Playing) {
                    if (!capturedInitialState && it.game.rounds.rollsLeft == 3) {
                        capturedInitialState = true
                        initialLatch.open()
                    } else if (capturedInitialState && it.game.rounds.rollsLeft == 2) {
                        stateAfterRoll = it
                        rollLatch.open()
                    }
                }
            }
        }

        initialLatch.await()

        sut.onRollDice()

        rollLatch.await()
        collectorJob.cancel()

        assert(stateAfterRoll!!.game.rounds.rollsLeft == 2) {
            "Expected 2 rolls left after first roll, but got ${stateAfterRoll.game.rounds.rollsLeft}"
        }
    }

    @Test
    fun toggle_dice_selection_changes_selected_dice() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )
        val initialLatch = SuspendingLatch()
        val toggleLatch = SuspendingLatch()
        var capturedInitialState = false
        var stateAfterToggle: GameScreenState.Playing? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                if (it is GameScreenState.Playing) {
                    if (!capturedInitialState && !it.selectedDice[0]) {
                        capturedInitialState = true
                        initialLatch.open()
                    } else if (capturedInitialState && it.selectedDice[0]) {
                        stateAfterToggle = it
                        toggleLatch.open()
                    }
                }
            }
        }

        initialLatch.await()

        sut.onToggleDiceSelection(0)

        toggleLatch.await()
        collectorJob.cancel()

        assert(stateAfterToggle!!.selectedDice[0]) {
            "Expected first dice to be selected"
        }
    }

    @Test
    fun on_quit_game_clears_quit_state() = runTest {
        val sut = GameViewModel(
            gameService = FakeGameService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )

        delay(500)

        sut.onQuitGame()
        delay(100)

        sut.clearQuitGame()

        assert(!sut.quitGameFlow.value) {
            "Expected quit game to be false after clearing"
        }
    }

    @Test
    fun changes_to_game_over_state_when_game_ends() = runTest {
        val gameService = GameOverFakeGameService()
        val sut = GameViewModel(
            gameService = gameService,
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(),
            lobbyId = "test-lobby-id"
        )

        val playingLatch = SuspendingLatch()
        val gameOverLatch = SuspendingLatch()
        var capturedPlayingState = false
        var result: GameScreenState.GameOver? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                if (it is GameScreenState.Playing && !capturedPlayingState) {
                    capturedPlayingState = true
                    playingLatch.open()
                } else if (it is GameScreenState.GameOver) {
                    result = it
                    gameOverLatch.open()
                }
            }
        }

        playingLatch.await()

        sut.endTurn()

        gameOverLatch.await()
        collectorJob.cancel()

        assert(result != null) {
            "Expected GameOver state, but got null"
        }

        assert(result!!.winners.isNotEmpty()) {
            "Expected at least one winner"
        }

        assert(result.winners[0].name == "player1") {
            "Expected player1 to be the winner, but got ${result.winners[0].name}"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}
