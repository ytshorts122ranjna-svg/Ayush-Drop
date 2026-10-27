package com.example.ui.screens.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AyushBlueLight
import com.example.ui.theme.AyushCyanLight
import com.example.ui.theme.AyushGreen
import com.example.ui.theme.AyushRose
import com.example.ui.theme.AyushVioletLight
import com.example.ui.viewmodel.ArcadeViewModel
import com.example.ui.viewmodel.Direction
import com.example.ui.viewmodel.GameStatus

@Composable
fun SnakeGameView(
    viewModel: ArcadeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.snakeState.collectAsStateWithLifecycle()
    val highScore by viewModel.snakeHighScore.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state.status == GameStatus.IDLE) {
            viewModel.startSnake()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("snake_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to Arcade")
            }

            Text(
                text = "Cyber Snake",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AyushCyanLight
            )

            IconButton(onClick = { viewModel.startSnake() }, modifier = Modifier.testTag("snake_restart_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart Snake")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Score Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "${state.score}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("BEST", style = MaterialTheme.typography.labelSmall, color = AyushVioletLight)
                    Text(
                        "${maxOf(state.score, highScore ?: 0)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Game Canvas Board
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, AyushBlueLight.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val gridSize = state.gridSize
                val cellW = size.width / gridSize
                val cellH = size.height / gridSize

                // Draw Food
                val food = state.food
                drawCircle(
                    color = AyushRose,
                    radius = cellW * 0.4f,
                    center = Offset(food.first * cellW + cellW / 2, food.second * cellH + cellH / 2)
                )

                // Draw Snake Body
                state.snake.forEachIndexed { index, part ->
                    val color = if (index == 0) AyushCyanLight else AyushBlueLight
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(part.first * cellW + 1f, part.second * cellH + 1f),
                        size = Size(cellW - 2f, cellH - 2f),
                        cornerRadius = CornerRadius(cellW * 0.25f, cellH * 0.25f)
                    )
                }
            }

            // Game Over overlay
            if (state.status == GameStatus.GAME_OVER) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.Black.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME OVER",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = AyushRose
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Final Score: ${state.score}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startSnake() },
                            modifier = Modifier.testTag("snake_play_again_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Again")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Virtual D-Pad
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            FilledIconButton(
                onClick = { viewModel.changeSnakeDirection(Direction.UP) },
                modifier = Modifier.size(52.dp).testTag("dpad_up"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", modifier = Modifier.size(32.dp))
            }

            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                FilledIconButton(
                    onClick = { viewModel.changeSnakeDirection(Direction.LEFT) },
                    modifier = Modifier.size(52.dp).testTag("dpad_left"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", modifier = Modifier.size(32.dp))
                }

                FilledIconButton(
                    onClick = { viewModel.changeSnakeDirection(Direction.RIGHT) },
                    modifier = Modifier.size(52.dp).testTag("dpad_right"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", modifier = Modifier.size(32.dp))
                }
            }

            FilledIconButton(
                onClick = { viewModel.changeSnakeDirection(Direction.DOWN) },
                modifier = Modifier.size(52.dp).testTag("dpad_down"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", modifier = Modifier.size(32.dp))
            }
        }
    }
}
