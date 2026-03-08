package com.example.chelasmulti_playerpokerdice.profile

import com.example.chelasmulti_playerpokerdice.SuspendingLatch
import com.example.chelasmulti_playerpokerdice.services.HandRank
import com.example.chelasmulti_playerpokerdice.testutils.FakeProfileService
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ProfileViewModelTests {

    @Test
    fun initially_displays_current_username() = runTest {
        val profileService = FakeProfileService(initialUsername = "TESTUSER")
        val sut = ProfileViewModel(profileService = profileService)

        val latch = SuspendingLatch()
        var result: String? = null

        val collectorJob = launch {
            sut.username.collect { username ->
                result = username
                latch.open()
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result == "TESTUSER") {
            "Expected 'TESTUSER', but got $result"
        }
    }

    @Test
    fun saveUsername_trims_whitespace() = runTest {
        val profileService = FakeProfileService(initialUsername = "TESTUSER")
        val sut = ProfileViewModel(profileService = profileService)

        val latch = SuspendingLatch()
        var savedUsername: String? = null

        val collectorJob = launch {
            sut.username.collect { username ->
                if (username == "NEWUSER") {
                    savedUsername = username
                    latch.open()
                }
            }
        }

        sut.updateName("  newUser  ")
        sut.save()

        latch.await()
        collectorJob.cancel()

        assert(savedUsername == "NEWUSER") {
            "Expected 'NEWUSER', but got $savedUsername"
        }
    }

    @Test
    fun saveUsername_shows_error_for_empty_username() = runTest {
        val profileService = FakeProfileService(initialUsername = "TESTUSER")
        val sut = ProfileViewModel(profileService = profileService)

        val latch = SuspendingLatch()
        var errorMessage: String? = null

        val collectorJob = launch {
            sut.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    errorMessage = error
                    latch.open()
                }
            }
        }

        sut.updateName("")
        sut.save()

        latch.await()
        collectorJob.cancel()

        assert(errorMessage?.isNotEmpty() == true) {
            "Expected error message for empty username"
        }
    }

    @Test
    fun saveUsername_shows_error_for_whitespace_only() = runTest {
        val profileService = FakeProfileService(initialUsername = "TESTUSER")
        val sut = ProfileViewModel(profileService = profileService)

        val latch = SuspendingLatch()
        var errorMessage: String? = null

        val collectorJob = launch {
            sut.errorMessage.collect { error ->
                if (error.isNotEmpty()) {
                    errorMessage = error
                    latch.open()
                }
            }
        }

        sut.updateName("   ")
        sut.save()

        latch.await()
        collectorJob.cancel()

        assert(errorMessage?.isNotEmpty() == true) {
            "Expected error message for whitespace-only username"
        }
    }

    @Test
    fun resetUsername_restores_original_username() = runTest {
        val profileService = FakeProfileService(initialUsername = "ORIGINALUSER")
        val sut = ProfileViewModel(profileService = profileService)

        val initialLatch = SuspendingLatch()
        val collectorJob = launch {
            sut.editableName.collect { name ->
                if (name == "ORIGINALUSER") {
                    initialLatch.open()
                }
            }
        }
        initialLatch.await()


        sut.updateName("TEMPUSER")

        assert(sut.editableName.value == "TEMPUSER") {
            "Expected editableName to be 'TEMPUSER', but got ${sut.editableName.value}"
        }

        sut.resetName()

        assert(sut.editableName.value == "ORIGINALUSER") {
            "Expected editableName to be restored to 'ORIGINALUSER', but got ${sut.editableName.value}"
        }
        collectorJob.cancel()
    }

    @Test
    fun editingUsername_clears_error_message() = runTest {
        val profileService = FakeProfileService(initialUsername = "TESTUSER")
        val sut = ProfileViewModel(profileService = profileService)

        val errorLatch = SuspendingLatch()
        val clearLatch = SuspendingLatch()
        var errorSet = false
        var errorCleared = false

        val collectorJob = launch {
            sut.errorMessage.collect { error ->
                if (!errorSet && error.isNotEmpty()) {
                    errorSet = true
                    errorLatch.open()
                } else if (errorSet && error.isEmpty()) {
                    errorCleared = true
                    clearLatch.open()
                }
            }
        }

        sut.updateName("")
        sut.save()

        errorLatch.await()

        sut.updateName("newUser")

        clearLatch.await()
        collectorJob.cancel()

        assert(errorCleared) {
            "Expected error message to be cleared"
        }
    }

    @Test
    fun loads_hand_frequencies_on_initialization() = runTest {
        val profileService = FakeProfileService(
            initialUsername = "TESTUSER",
            initialHandFrequency = mapOf(
                HandRank.PAIR to 5,
                HandRank.FULL_HOUSE to 3
            )
        )
        val sut = ProfileViewModel(profileService = profileService)

        val latch = SuspendingLatch()
        var result: Map<HandRank, Int>? = null

        val collectorJob = launch {
            sut.handFrequency.collect { frequencies ->
                if (frequencies.isNotEmpty()) {
                    result = frequencies
                    latch.open()
                }
            }
        }

        latch.await()
        collectorJob.cancel()

        assert(result?.get(HandRank.PAIR) == 5) {
            "Expected PAIR frequency to be 5, but got ${result?.get(HandRank.PAIR)}"
        }
        assert(result?.get(HandRank.FULL_HOUSE) == 3) {
            "Expected FULL_HOUSE frequency to be 3, but got ${result?.get(HandRank.FULL_HOUSE)}"
        }
    }
}
