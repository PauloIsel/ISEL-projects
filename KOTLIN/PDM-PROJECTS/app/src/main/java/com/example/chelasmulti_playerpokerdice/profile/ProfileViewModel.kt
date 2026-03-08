package com.example.chelasmulti_playerpokerdice.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chelasmulti_playerpokerdice.services.IProfileService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val profileService: IProfileService) : ViewModel() {

    val username = profileService.usernameFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        "Player"
    )

    val gamesPlayed = profileService.gamesPlayedFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )

    val gamesWon = profileService.gamesWonFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        0
    )

    val handFrequency = profileService.handFrequencyFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyMap()
    )

    var editableName = MutableStateFlow(username.value)
    var errorMessage = MutableStateFlow("")

    init {
        viewModelScope.launch {
            username.collect { current ->
                editableName.value = current
            }
        }
    }

    fun updateName(name: String) {
        editableName.value = name
        errorMessage.value = ""
    }

    fun resetName() {
        editableName.value = username.value
    }

    fun save() {
        val trimmed = editableName.value.trim().uppercase()

        if (trimmed.isEmpty()) {
            errorMessage.value = "Nickname can't be empty"
            return
        }

        viewModelScope.launch {
            profileService.saveUserName(trimmed)
        }
    }
}
