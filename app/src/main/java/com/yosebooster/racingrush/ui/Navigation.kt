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
