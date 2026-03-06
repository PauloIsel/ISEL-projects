package com.example.chelasmulti_playerpokerdice.lobbyCreation

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.services.Lobby
import com.example.chelasmulti_playerpokerdice.services.LobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LobbyCreationViewModelTests {

    @Test
    fun initially_its_in_data_state_with_default_values() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                latch.open()
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result is LobbyCreationScreenState.Data) {
            "Expected Data state, but got $result"
        }

        val data = result as LobbyCreationScreenState.Data
        assert(data.lobbyName == "") { "Expected empty lobby name" }
        assert(data.description == "") { "Expected empty description" }
        assert(data.players == 2) { "Expected 2 players" }
        assert(data.rounds == 2) { "Expected 2 rounds" }
        assert(!data.isLobbyNameValid) { "Expected lobby name to be invalid" }
        assert(!data.isDescriptionValid) { "Expected description to be invalid" }
    }

    @Test
    fun lobby_name_becomes_valid_when_non_blank() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyCreationScreenState.Data && it.isLobbyNameValid) {
                    latch.open()
                }
            }
        }

        sut.onLobbyNameChange("Test Lobby")

        latch.await()
        collectorJob.cancel()

        val data = result as LobbyCreationScreenState.Data
        assert(data.lobbyName == "Test Lobby") { "Expected lobby name to be 'Test Lobby'" }
        assert(data.isLobbyNameValid) { "Expected lobby name to be valid" }
    }

    @Test
    fun description_becomes_valid_when_non_blank() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyCreationScreenState.Data && it.isDescriptionValid) {
                    latch.open()
                }
            }
        }

        sut.onDescriptionChange("Test Description")

        latch.await()
        collectorJob.cancel()

        val data = result as LobbyCreationScreenState.Data
        assert(data.description == "Test Description") { "Expected description to be 'Test Description'" }
        assert(data.isDescriptionValid) { "Expected description to be valid" }
    }

    @Test
    fun players_change_updates_state() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyCreationScreenState.Data && it.players == 4) {
                    latch.open()
                }
            }
        }

        sut.onPlayersChange(4)

        latch.await()
        collectorJob.cancel()

        val data = result as LobbyCreationScreenState.Data
        assert(data.players == 4) { "Expected 4 players" }
        assert(data.rounds == 4) { "Expected rounds to adjust to 4" }
    }

    @Test
    fun rounds_change_validates_divisibility_by_players() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyCreationScreenState.Data && it.rounds == 6) {
                    latch.open()
                }
            }
        }

        sut.onRoundsChange(6)

        latch.await()
        collectorJob.cancel()

        val data = result as LobbyCreationScreenState.Data
        assert(data.rounds == 6) { "Expected 6 rounds" }
        assert(data.isRoundsValid) { "Expected rounds to be valid (divisible by 2)" }
    }

    @Test
    fun changes_to_success_state_when_lobby_is_created() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyCreationScreenState.Success) {
                    latch.open()
                }
            }
        }

        sut.onLobbyNameChange("Test Lobby")
        sut.onDescriptionChange("Test Description")
        sut.onCreateLobby()

        latch.await()
        collectorJob.cancel()

        assert(result is LobbyCreationScreenState.Success) {
            "Expected Success state, but got $result"
        }
    }

    @Test
    fun changes_to_error_state_when_lobby_creation_fails() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FailingFakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor()
        )
        val latch = SuspendingLatch()
        var result: LobbyCreationScreenState? = null

        val collectorJob = launch {
            sut.currentStateFlow.collect {
                result = it
                if (it is LobbyCreationScreenState.Data && it.errorMessage != null) {
                    latch.open()
                }
            }
        }

        sut.onLobbyNameChange("Test Lobby")
        sut.onDescriptionChange("Test Description")
        sut.onCreateLobby()

        latch.await()
        collectorJob.cancel()

        assert(result is LobbyCreationScreenState.Data) {
            "Expected Data state, but got $result"
        }

        val data = result as LobbyCreationScreenState.Data
        assert(data.errorMessage != null) {
            "Expected error message to be set"
        }
        assert(data.errorMessage == "Failed to create lobby") {
            "Expected error message to be 'Failed to create lobby', but got '${data.errorMessage}'"
        }
        assert(data.lobbyName == "Test Lobby") {
            "Expected lobby name to be preserved"
        }
        assert(data.description == "Test Description") {
            "Expected description to be preserved"
        }
    }
}

private class DefaultTestLobbyService : LobbyService {
    override fun getLobbies(): Flow<List<Lobby>> = TODO()

    override suspend fun getLobby(lobbyId: String): Lobby = TODO()

    override fun observeLobby(lobbyId: String): Flow<Lobby> = TODO()

    override suspend fun removeLobby(lobbyId: String) = TODO()

    override suspend fun changeLobbyHost(lobbyId: String): String = TODO()

    override suspend fun sendMessage(lobbyId: String, player: String, message: String) = TODO()

    override suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby = TODO()

    override suspend fun joinLobby(lobbyId: String, player: String) = TODO()

    override suspend fun abandonLobby(lobbyId: String, player: String) = TODO()

    override suspend fun updateLobby(
        lobbyId: String,
        updates: Map<String, Any>
    ) {
        TODO("Not yet implemented")
    }
}


private class FailingFakeLobbyService(
    delegate: LobbyService = DefaultTestLobbyService()
) : LobbyService by delegate {
    override suspend fun createLobby(name: String, description: String, size: Int, rounds: Int, owner: String): Lobby {
        delay(100)
        throw Exception("Failed to create lobby")
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}
