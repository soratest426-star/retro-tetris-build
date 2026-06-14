package com.yosebooster.racingrush.ui

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.yosebooster.racingrush.ui.game.RacingGameScreen
import com.yosebooster.racingrush.ui.screens.MainMenuScreen
import com.yosebooster.racingrush.ui.screens.ResultsScreen
import com.yosebooster.racingrush.ui.screens.ShopScreen
import com.yosebooster.racingrush.ui.settings.SettingsBottomSheet
import com.yosebooster.racingrush.ui.viewmodel.MainViewModel
import com.yosebooster.racingrush.utils.vibrateError
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

sealed class Destinations(val route: String) {
    data object Menu : Destinations("menu")
    data object Game : Destinations("game")
    data object Shop : Destinations("shop")
    data object Results : Destinations("results")
    data object Settings : Destinations("settings")
}


@Composable
@OptIn(ExperimentalMaterialNavigationApi::class)
fun RacingRushGameNavHost() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    ModalBottomSheetLayout(
        bottomSheetNavigator = bottomSheetNavigator,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        val viewModel = hiltViewModel<MainViewModel>()
        NavHost(navController, Destinations.Menu.route) {
            menuScreen(navController, viewModel)
            gameScreen(navController, viewModel)
            shopScreen(navController, viewModel)
            resultsScreen(navController, viewModel)
            settingsScreen(viewModel)
        }
    }
}

private fun NavGraphBuilder.menuScreen(navController: NavHostController, viewModel: MainViewModel) {
    composable(Destinations.Menu.route) {
        val highscore by viewModel.highscore.collectAsState()
        MainMenuScreen(
            onStartGame = {
                viewModel.resetGameScore()
                navController.navigate(Destinations.Game.route)
            },
            onOpenShop = { navController.navigate(Destinations.Shop.route) },
            onOpenSettings = { navController.navigate(Destinations.Settings.route) },
            highscore = highscore
        )
    }
}

private fun NavGraphBuilder.shopScreen(navController: NavHostController, viewModel: MainViewModel) {
    composable(Destinations.Shop.route) {
        val context = LocalContext.current
        val activity = context as Activity
        
        val coins by viewModel.coins.collectAsState()
        val unlockedCars by viewModel.unlockedCars.collectAsState()
        val selectedCarId by viewModel.selectedCarId.collectAsState()
        
        ShopScreen(
            coins = coins,
            unlockedCars = unlockedCars,
            selectedCarId = selectedCarId,
            onBack = { navController.popBackStack() },
            onPurchaseCar = { viewModel.purchaseCar(it) },
            onSelectCar = { viewModel.selectCar(it) }
        )
    }
}

private fun NavGraphBuilder.resultsScreen(navController: NavHostController, viewModel: MainViewModel) {
    composable(Destinations.Results.route) {
        val context = LocalContext.current
        val activity = context as Activity

        LaunchedEffect(Unit) {
            viewModel.showInterstitialIfReady(activity)
        }

        val score by viewModel.gameScore.collectAsState()
        val highscore by viewModel.highscore.collectAsState()
        val coinsEarned by viewModel.sessionCoins.collectAsState()
        
        ResultsScreen(
            score = score,
            highscore = highscore,
            coinsEarned = coinsEarned,
            onRetry = {
                viewModel.resetGameScore()
                navController.navigate(Destinations.Game.route) {
                    popUpTo(Destinations.Menu.route)
                }
            },
            onDoubleRewards = { viewModel.doubleSessionRewards(activity) },
            onMainMenu = {
                navController.navigate(Destinations.Menu.route) {
                    popUpTo(Destinations.Menu.route) { inclusive = true }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
private fun NavGraphBuilder.settingsScreen(viewModel: MainViewModel) {
    bottomSheet(Destinations.Settings.route) {
        val movementInput by viewModel.movementInput.collectAsState()
        SettingsBottomSheet(
            movementInput = movementInput,
            onMovementInputChange = { viewModel.setMovementInput(it) }
        )
    }
}

private fun NavGraphBuilder.gameScreen(navController: NavHostController, viewModel: MainViewModel) {
    composable(Destinations.Game.route) {

        val context = LocalContext.current
        val activity = context as Activity

        LaunchedEffect(context) {
            viewModel.loadAds()
            viewModel.vibrateSharedFlow.collect {
                context.vibrateError()
            }
        }
        
        LaunchedEffect(Unit) {
            viewModel.gameOverEvent.collect {
                navController.navigate(Destinations.Results.route) {
                    popUpTo(Destinations.Game.route) { inclusive = true }
                }
            }
        }

        val gameScore by viewModel.gameScore.collectAsState()
        val highscore by viewModel.highscore.collectAsState()
        val isGameOver by viewModel.isGameOver.collectAsState()
        val acceleration by viewModel.acceleration.collectAsState()
        val movementInput by viewModel.movementInput.collectAsState()
        val resourcePack by viewModel.resourcePack.collectAsState()
        val carImageRes by viewModel.selectedCarImage.collectAsState()

        RacingGameScreen(
            isDevMode = { true },
            onSettingsClick = {
                navController.navigate(Destinations.Settings.route)
            },
            gameScore = { gameScore },
            highscore = { highscore },
            isGameOver = { isGameOver },
            resourcePack = { resourcePack },
            carImageRes = carImageRes,
            acceleration = { acceleration },
            movementInput = { movementInput },
            onGameScoreIncrease = viewModel::increaseGameScore,
            onResetGameScore = viewModel::resetGameScore,
            onContinueGame = { viewModel.continueGame(activity) },
            onExitGame = { viewModel.onGameOverExit() },
            onBlockerRectsDraw = viewModel::updateBlockerRects,
            onCarRectDraw = viewModel::updateCarRect,
            modifier = Modifier.fillMaxSize()
        )
    }
}

            onBlockerRectsDraw = viewModel::updateBlockerRects,
            onCarRectDraw = viewModel::updateCarRect,
            modifier = Modifier.fillMaxSize()
        )
    }
}
