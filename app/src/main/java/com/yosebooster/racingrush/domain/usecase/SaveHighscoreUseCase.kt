package com.yosebooster.racingrush.domain.usecase

import com.yosebooster.racingrush.domain.repo.UserPreferencesRepository
import com.yosebooster.racingrush.ui.viewmodel.MainViewModel
import com.yosebooster.racingrush.utils.SoundRepository
import kotlinx.coroutines.flow.first

class SaveHighscoreUseCase(
    private val repository: UserPreferencesRepository,
    private val soundRepository: SoundRepository
) {
    suspend fun execute(score: Int) {
        val prefs = repository.getUserPreferences().first()
        if (score > prefs.highscore) {
            repository.saveHighscore(score)
            soundRepository.playSound(MainViewModel.NEW_HIGHSCORE_SOUND_ID)
        }
    }
}
