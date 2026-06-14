package com.yosebooster.racingrush.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yosebooster.racingrush.ui.game.state.BackgroundState
import com.yosebooster.racingrush.ui.game.state.BlockersState
import com.yosebooster.racingrush.ui.game.state.CarState
import com.yosebooster.racingrush.ui.game.state.GameState
import com.yosebooster.racingrush.ui.models.AccelerationData
import com.yosebooster.racingrush.ui.models.MovementInput
import com.yosebooster.racingrush.ui.models.MovementInput.Accelerometer
import com.yosebooster.racingrush.ui.models.MovementInput.SwipeGestures
import com.yosebooster.racingrush.ui.models.MovementInput.TapGestures
import com.yosebooster.racingrush.ui.models.RacingResourcePack
import com.yosebooster.racingrush.utils.Constants
import com.yosebooster.racingrush.utils.Constants.CAR_MOVEMENT_SPRING_ANIMATION_STIFFNESS
import com.yosebooster.racingrush.utils.Constants.TICKER_ANIMATION_DURATION

@Composable
fun RacingGameScreen(
    gameScore: () -> Int,
    highscore: () -> Int,
    isGameOver: () -> Boolean,
    acceleration: () -> AccelerationData,
    movementInput: () -> MovementInput,
    resourcePack: () -> RacingResourcePack,
    carImageRes: Int,
    isDevMode: () -> Boolean,
    onSettingsClick: () -> Unit,
    onGameScoreIncrease: () -> Unit,
    onResetGameScore: () -> Unit,
    onContinueGame: () -> Unit,
    onExitGame: () -> Unit,
    onBlockerRectsDraw: (List<Rect>) -> Unit,
    onCarRectDraw: (Rect) -> Unit,
    modifier: Modifier = Modifier,
) {
    // resources
    val carImageDrawableBitmap = ImageBitmap.imageResource(carImageRes)
    val backgroundImageBitmap = ImageBitmap.imageResource(resourcePack().backgroundImageDrawable)
    val blockerImageBitmap = ImageBitmap.imageResource(resourcePack().blockerImageDrawable)

    // states
    val gameState by remember {
        mutableStateOf(GameState())
    }
    val carState by remember {
        mutableStateOf(
            CarState(image = carImageDrawableBitmap)
        )
    }

    val blockersState by remember {
        mutableStateOf(
            BlockersState(image = blockerImageBitmap)
        )
    }
    val backgroundState by remember {
        mutableStateOf(
            BackgroundState(
                image = backgroundImageBitmap,
                onGameScoreIncrease = {
                    if (gameState.isRunning() && !isGameOver())
                        onGameScoreIncrease()
                }
            )
        )
    }

    val backgroundSpeed by remember {
        derivedStateOf {
            (gameScore() / Constants.GAME_SCORE_TO_VELOCITY_RATIO) + Constants.INITIAL_VELOCITY
        }
    }

    // ticker
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")

    val ticker by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = TICKER_ANIMATION_DURATION, easing = LinearEasing)
        ),
        label = "ticker"
    )

    BoxWithConstraints(modifier = modifier) {
        ticker //todo find a better way to put it in here!

        LaunchedEffect(movementInput()) {
            if (movementInput() == Accelerometer)
                carState.moveWithAcceleration(acceleration())
        }

        val carOffsetIndex by animateFloatAsState(
            targetValue = carState.getPosition().fromLeftOffsetIndex(),
            label = "car offset index",
            animationSpec = spring(stiffness = CAR_MOVEMENT_SPRING_ANIMATION_STIFFNESS)
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (gameState.isRunning() && !isGameOver()) {
                        when (movementInput()) {
                            TapGestures ->
                                Modifier.detectCarPositionByPointerInput(maxWidth = maxWidth.value.toInt()) { position ->
                                    carState.moveWithTapGesture(position)
                                }

                            SwipeGestures -> Modifier.detectSwipeDirection(maxWidth.value.toInt()) { swipeDirection ->
                                carState.moveWithSwipeGesture(swipeDirection)
                            }

                            Accelerometer -> Modifier
                        }
                    } else
                        Modifier
                )
        ) {
            GameCanvas(
                gameState = gameState,
                backgroundState = backgroundState,
                backgroundSpeed = backgroundSpeed,
                blockersState = blockersState,
                carState = carState,
                carOffsetIndex = carOffsetIndex,
                onBlockerRectsDraw = onBlockerRectsDraw,
                onCarRectDraw = onCarRectDraw,
                modifier = Modifier.fillMaxSize(),
            )

            AnimatedVisibility(
                visible = (gameState.isStopped() || gameState.isPaused()) && !isGameOver(),
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                GameStateIndicator(
                    gameState = gameState,
                    onStartClicked = { gameState.run() }
                )
            }

            if (isGameOver()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "CRASHED!",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.Red,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = onContinueGame,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONTINUE (WATCH AD)", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onExitGame,
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("QUIT", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }

        }
        Column(modifier = Modifier.fillMaxWidth()) {
            TopInfoTexts(
                gameScore = gameScore,
                highscore = highscore,
                modifier = Modifier.fillMaxWidth()
            )
            TopActionButtons(
                onSettingsClick = onSettingsClick,
                onPauseGameState = { if (!isGameOver()) gameState.pause() },
                onResetGameScore = onResetGameScore,
                isDevMode = isDevMode(),
                modifier = Modifier.fillMaxWidth()
            )
        }

    }
}
