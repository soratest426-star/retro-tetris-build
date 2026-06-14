package com.yosebooster.racingrush.ui.viewmodel

import android.app.Activity
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosebooster.racingrush.R
import com.yosebooster.racingrush.domain.models.Achievement
import com.yosebooster.racingrush.domain.models.Car
import com.yosebooster.racingrush.domain.models.Mission
import com.yosebooster.racingrush.domain.models.PowerUpState
import com.yosebooster.racingrush.domain.models.PowerUpType
import com.yosebooster.racingrush.domain.models.availableCars
import com.yosebooster.racingrush.domain.usecase.AddCoinsUseCase
import com.yosebooster.racingrush.domain.usecase.GetUserPreferencesUseCase
import com.yosebooster.racingrush.domain.usecase.PurchaseCarUseCase
import com.yosebooster.racingrush.domain.usecase.SaveHighscoreUseCase
import com.yosebooster.racingrush.domain.usecase.SelectCarUseCase
import com.yosebooster.racingrush.domain.usecase.UpdateSettingsUseCase
import com.yosebooster.racingrush.ui.models.AccelerationData
import com.yosebooster.racingrush.ui.models.MovementInput
import com.yosebooster.racingrush.ui.models.NightRacingResourcePack
import com.yosebooster.racingrush.ui.models.RacingResourcePack
import com.yosebooster.racingrush.utils.AdManager
import com.yosebooster.racingrush.utils.Constants.ACHIEVEMENT_HIGH_SCORE_TARGET
import com.yosebooster.racingrush.utils.Constants.COLLISION_SCORE_PENALTY
import com.yosebooster.racingrush.utils.Constants.DEFAULT_ACCELEROMETER_SENSITIVITY
import com.yosebooster.racingrush.utils.Constants.INITIAL_GAME_SCORE
import com.yosebooster.racingrush.utils.Constants.MISSION_COINS_TARGET
import com.yosebooster.racingrush.utils.Constants.MISSION_SCORE_TARGET
import com.yosebooster.racingrush.utils.Constants.POWER_UP_DURATION_MS
import com.yosebooster.racingrush.utils.SecurityUtils
import com.yosebooster.racingrush.utils.SoundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val saveHighscoreUseCase: SaveHighscoreUseCase,
    private val addCoinsUseCase: AddCoinsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val purchaseCarUseCase: PurchaseCarUseCase,
    private val selectCarUseCase: SelectCarUseCase,
    private val soundRepository: SoundRepository,
    private val adManager: AdManager,
) : ViewModel() {

    private val _acceleration = MutableStateFlow(AccelerationData(0f, 0f, 0f))
    val acceleration = _acceleration.asStateFlow()

    private val _movementInput = MutableStateFlow(MovementInput.SwipeGestures)
    val movementInput = _movementInput.asStateFlow()

    private val _gameScore = MutableStateFlow(INITIAL_GAME_SCORE)
    val gameScore = _gameScore.asStateFlow()

    private val _highscore = MutableStateFlow(0)
    val highscore = _highscore.asStateFlow()

    private val _coins = MutableStateFlow(0)
    val coins = _coins.asStateFlow()

    private val _sessionCoins = MutableStateFlow(0)
    val sessionCoins = _sessionCoins.asStateFlow()

    private val _unlockedCars = MutableStateFlow<Set<String>>(setOf("car_1"))
    val unlockedCars = _unlockedCars.asStateFlow()

    private val _selectedCarId = MutableStateFlow("car_1")
    val selectedCarId = _selectedCarId.asStateFlow()

    val selectedCarImage = _selectedCarId.map { id ->
        availableCars.find { it.id == id }?.imageRes ?: R.drawable.ic_car
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), R.drawable.ic_car)

    // Power-ups State
    private val _activePowerUps = MutableStateFlow<Map<PowerUpType, PowerUpState>>(emptyMap())
    val activePowerUps = _activePowerUps.asStateFlow()

    // Missions & Achievements State
    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    val missions = _missions.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements = _achievements.asStateFlow()

    private val powerUpJobs = mutableMapOf<PowerUpType, Job>()

    private val _resourcePack = MutableStateFlow<RacingResourcePack>(NightRacingResourcePack())
    val resourcePack = _resourcePack.asStateFlow()

    val vibrateSharedFlow = MutableSharedFlow<Unit>(replay = 1)
    val gameOverEvent = MutableSharedFlow<Unit>()
    val securityViolationEvent = MutableSharedFlow<String>(replay = 1)

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver.asStateFlow()

    private var lastScoreUpdateTimestamp: Long = 0L
    private var lastScoreValue: Int = 0

    private val carRectStateFlow = MutableStateFlow<Rect?>(null)
    private val blockerRectsStateFlow = MutableStateFlow<List<Rect>>(emptyList())

    private val carAndBlockerCollisionStateFlow =
        combine(carRectStateFlow.filterNotNull(), blockerRectsStateFlow) { carRect, blockerRects ->
            checkBlockerAndCarCollision(blockerRects, carRect)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        lastScoreUpdateTimestamp = System.currentTimeMillis()
        observeCollision()
        observeUserPreferences()
        initMissionsAndAchievements()

        soundRepository.loadSound(NEW_HIGHSCORE_SOUND_ID, R.raw.new_highscore)
        soundRepository.loadSound(BLOCKER_HIT_SOUND_ID, R.raw.blocker_hit)
        soundRepository.loadSound(MILESTONE_REACH_SOUND_ID, R.raw.milestone_reach)
    }

    private fun initMissionsAndAchievements() {
        _missions.value = listOf(
            Mission("m1", "Reach $MISSION_SCORE_TARGET points", MISSION_SCORE_TARGET, rewardCoins = 50),
            Mission("m2", "Collect $MISSION_COINS_TARGET coins in one session", MISSION_COINS_TARGET, rewardCoins = 30)
        )
        _achievements.value = listOf(
            Achievement("a1", "Speed Demon", "Reach a highscore of $ACHIEVEMENT_HIGH_SCORE_TARGET", ACHIEVEMENT_HIGH_SCORE_TARGET)
        )
    }

    private fun observeUserPreferences() {
        getUserPreferencesUseCase.execute().onEach { prefs ->
            _highscore.value = prefs.highscore
            _coins.value = prefs.coins
            _unlockedCars.value = prefs.unlockedCars
            _selectedCarId.value = prefs.selectedCarId
            _movementInput.value = when (prefs.movementInput) {
                "Accelerometer" -> MovementInput.Accelerometer
                "TapGestures" -> MovementInput.TapGestures
                else -> MovementInput.SwipeGestures
            }
            checkAchievementProgress(prefs)
        }.launchIn(viewModelScope)
    }

    private fun observeCollision() {
        carAndBlockerCollisionStateFlow.onEach { hasCollision ->
            if (hasCollision && !_isGameOver.value) {
                val isTurboActive = _activePowerUps.value[PowerUpType.TURBO]?.isActive == true
                val isShieldActive = _activePowerUps.value[PowerUpType.SHIELD]?.isActive == true

                if (isTurboActive) return@onEach 

                if (isShieldActive) {
                    deactivatePowerUp(PowerUpType.SHIELD)
                    return@onEach
                }

                _isGameOver.value = true
                playBlockerHitSound()
                vibrateSharedFlow.tryEmit(Unit)
            }
        }.launchIn(viewModelScope)
    }

    fun activatePowerUp(type: PowerUpType) {
        powerUpJobs[type]?.cancel()
        _activePowerUps.update { it + (type to PowerUpState(type, true, POWER_UP_DURATION_MS)) }
        
        powerUpJobs[type] = viewModelScope.launch {
            var remaining = POWER_UP_DURATION_MS
            while (remaining > 0) {
                delay(100)
                remaining -= 100
                _activePowerUps.update { it + (type to PowerUpState(type, true, remaining)) }
            }
            deactivatePowerUp(type)
        }
    }

    private fun deactivatePowerUp(type: PowerUpType) {
        powerUpJobs[type]?.cancel()
        _activePowerUps.update { it - type }
    }

    fun setAcceleration(
        accelerationX: Float,
        accelerationY: Float,
        accelerationZ: Float,
        sensitivity: Int = DEFAULT_ACCELEROMETER_SENSITIVITY
    ) {
        _acceleration.update {
            it.copy(
                x = accelerationX * sensitivity,
                y = accelerationY * sensitivity,
                z = accelerationZ * sensitivity
            )
        }
    }

    fun setMovementInput(movementInput: MovementInput) {
        viewModelScope.launch {
            updateSettingsUseCase.updateMovementInput(movementInput.name)
        }
    }

    fun increaseGameScore() {
        if (_isGameOver.value) return

        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastScoreUpdateTimestamp

        // Speed-hack detection: Score shouldn't increase faster than once every 100ms
        if (lastScoreValue != 0 && SecurityUtils.detectTimeAnomaly(100L, timeDelta)) {
            securityViolationEvent.tryEmit("Speed-hack detected! (Anomalous score increase rate)")
            _isGameOver.value = true
            return
        }

        val isMultiplierActive = _activePowerUps.value[PowerUpType.MULTIPLIER]?.isActive == true
        val increment = if (isMultiplierActive) 2 else 1

        _gameScore.update { currentScore ->
            val newScore = currentScore + increment
            lastScoreUpdateTimestamp = currentTime
            lastScoreValue = newScore

            newScore.also {
                saveNewHighscore(it)
                checkMissionProgress(it)

                if (it % 10 == 0) {
                    awardCoins(if (isMultiplierActive) 2 else 1)
                    playMilestoneReachSound()
                }
            }
        }
    }

    private fun checkMissionProgress(score: Int) {
        _missions.update { currentMissions ->
            currentMissions.map { mission ->
                if (mission.id == "m1" && !mission.isCompleted) {
                    val isCompleted = score >= mission.targetValue
                    if (isCompleted) awardCoins(mission.rewardCoins)
                    mission.copy(currentValue = score, isCompleted = isCompleted)
                } else {
                    mission
                }
            }
        }
    }

    private fun checkAchievementProgress(prefs: com.yosebooster.racingrush.domain.models.UserPreferences) {
        _achievements.update { currentAchievements ->
            currentAchievements.map { achievement ->
                if (achievement.id == "a1" && !achievement.isUnlocked) {
                    val isUnlocked = prefs.highscore >= achievement.targetValue
                    achievement.copy(currentValue = prefs.highscore, isUnlocked = isUnlocked)
                } else {
                    achievement
                }
            }
        }
    }

    private fun saveNewHighscore(newScore: Int) {
        viewModelScope.launch {
            saveHighscoreUseCase.execute(newScore)
        }
    }

    fun awardCoins(amount: Int) {
        viewModelScope.launch {
            addCoinsUseCase.execute(amount)
            _sessionCoins.update { it + amount }
            _missions.update { currentMissions ->
                currentMissions.map { mission ->
                    if (mission.id == "m2" && !mission.isCompleted) {
                        val newValue = mission.currentValue + amount
                        val isCompleted = newValue >= mission.targetValue
                        if (isCompleted) addCoinsUseCase.execute(mission.rewardCoins)
                        mission.copy(currentValue = newValue, isCompleted = isCompleted)
                    } else {
                        mission
                    }
                }
            }
        }
    }

    fun resetGameScore() {
        _gameScore.update { INITIAL_GAME_SCORE }
        _sessionCoins.update { 0 }
        _isGameOver.value = false
        lastScoreUpdateTimestamp = System.currentTimeMillis()
        lastScoreValue = 0
    }

    fun onGameOverExit() {
        gamesCompleted++
        viewModelScope.launch {
            gameOverEvent.emit(Unit)
        }
    }

    fun showInterstitialIfReady(activity: Activity, onFinished: () -> Unit = {}) {
        if (gamesCompleted >= adThreshold) {
            gamesCompleted = 0
            adThreshold = (3..5).random()
            adManager.showInterstitialAd(activity, onFinished)
        } else {
            onFinished()
        }
    }

    fun continueGame(activity: Activity) {
        adManager.showContinueGameAd(activity) {
            _isGameOver.value = false
            activatePowerUp(PowerUpType.SHIELD) 
        }
    }

    fun purchaseCar(car: Car) {
        viewModelScope.launch {
            if (purchaseCarUseCase.execute(car)) {
                // Success
            }
        }
    }

    fun selectCar(carId: String) {
        viewModelScope.launch {
            selectCarUseCase.execute(carId)
        }
    }

    fun doubleSessionRewards(activity: Activity) {
        adManager.showRewardDoublerAd(activity) {
            val sessionCoinsValue = _sessionCoins.value
            if (sessionCoinsValue > 0) {
                awardCoins(sessionCoinsValue)
            }
        }
    }

    fun loadAds() {
        adManager.loadInterstitialAd()
        adManager.loadRewardedAd()
    }

    private fun playBlockerHitSound() {
        soundRepository.playSound(BLOCKER_HIT_SOUND_ID)
    }

    fun playMilestoneReachSound() {
        soundRepository.playSound(MILESTONE_REACH_SOUND_ID)
    }

    fun playBackgroundMusic() {
        soundRepository.playBackgroundMusic()
    }

    fun stopBackgroundMusic() {
        soundRepository.stopBackgroundMusic()
    }

    fun releaseSounds() {
        soundRepository.release()
    }

    fun updateCarRect(carRect: Rect) {
        carRectStateFlow.value = carRect
    }

    fun updateBlockerRects(blockerRects: List<Rect>) {
        blockerRectsStateFlow.value = blockerRects
    }

    companion object {
        const val NEW_HIGHSCORE_SOUND_ID = 1
        const val BLOCKER_HIT_SOUND_ID = 2
        const val MILESTONE_REACH_SOUND_ID = 3
    }

    private fun checkBlockerAndCarCollision(blockerRects: List<Rect>, carRect: Rect): Boolean {
        return blockerRects.any { blockerRect ->
            blockerRect.overlaps(carRect)
        }
    }
}
