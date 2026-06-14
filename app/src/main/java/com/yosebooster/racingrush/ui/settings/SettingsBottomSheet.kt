package com.yosebooster.racingrush.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yosebooster.racingrush.ui.models.MovementInput

@Composable
fun SettingsBottomSheet(
    movementInput: MovementInput,
    onMovementInputChange: (MovementInput) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .heightIn(min = 100.dp)
    ) {
        MovementInputRow(
            movementInput = movementInput,
            onMovementInputChange = onMovementInputChange
        )
    }
}


