package com.example.chelasmulti_playerpokerdice.lobbyCreation

import com.example.chelasmulti_playerpokerdice.services.IProfileService
import com.example.chelasmulti_playerpokerdice.testutils.FakeLobbyService
import com.example.chelasmulti_playerpokerdice.testutils.FakeNetworkMonitor
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LobbyCreationNetworkConnectivityTests {

    @Test
    fun changes_to_no_network_state_when_network_is_unavailable() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false)
        )

        val deadline = System.currentTimeMillis() + 1000
        while (System.currentTimeMillis() < deadline && sut.currentStateFlow.value !is LobbyCreationScreenState.NoNetwork) {
            delay(20)
        }

        val result = sut.currentStateFlow.value
        assert(result is LobbyCreationScreenState.NoNetwork) { "Expected NoNetwork state" }
    }

    @Test
    fun network_availability_flow_reflects_network_status() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = true)
        )

        // allow a short moment for initialization
        val deadline = System.currentTimeMillis() + 500
        while (System.currentTimeMillis() < deadline && !sut.isNetworkAvailable.value) {
            delay(20)
        }

        assert(sut.isNetworkAvailable.value) { "Expected network to be available" }
    }

    @Test
    fun actions_require_network_show_snackbar_when_offline() = runTest {
        val sut = LobbyCreationViewModel(
            lobbyService = FakeLobbyService(),
            profileService = fakeProfileService(),
            networkMonitor = FakeNetworkMonitor(initialNetworkAvailable = false)
        )

        sut.onCreateLobby()

        // Wait briefly for the snackbar message to be emitted from the viewModel coroutine
        val deadline = System.currentTimeMillis() + 500
        while (System.currentTimeMillis() < deadline && sut.snackBarMessageFlow.value.isEmpty()) {
            delay(20)
        }

        assert(sut.snackBarMessageFlow.value == "No internet connection") {
            "Expected snackBarMessage to indicate no internet, but got '${sut.snackBarMessageFlow.value}'"
        }
    }
}

private fun fakeProfileService(): IProfileService {
    return FakeProfileService(initialUsername = "testUser")
}
