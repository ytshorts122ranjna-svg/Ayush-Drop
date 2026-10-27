package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AyushCyanLight
import com.example.ui.theme.AyushGreen
import com.example.ui.theme.AyushRose
import com.example.ui.theme.AyushVioletLight
import com.example.ui.viewmodel.ArcadeViewModel
import com.example.ui.viewmodel.Direction
import kotlin.math.abs

private fun getTileColor(value: Int): Color {
    return when (value) {
        2 -> Color(0xFF1E293B)
        4 -> Color(0xFF334155)
        8 -> Color(0xFF2563EB)
        16 -> Color(0xFF3B82F6)
        32 -> Color(0xFF7C3AED)
        64 -> Color(0xFF8B5CF6)
        128 -> Color(0xFF06B6D4)
        256 -> Color(0xFF0EA5E9)
        512 -> Color(0xFF10B981)
        1024 -> Color(0xFFF59E0B)
        2048 -> Color(0xFFEF4444)
        else -> Color(0xFF64748B)
    }
}

@Composable
fun G2048GameView(
    viewModel: ArcadeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.g2048State.collectAsStateWithLifecycle()
    val bestScore by viewModel.g2048HighScore.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state.grid.all { it == 0 }) {
            viewModel.start2048()
        }
    }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

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
            IconButton(onClick = onBack, modifier = Modifier.testTag("g2048_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "2048 Puzzle",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AyushGreen
            )

            IconButton(onClick = { viewModel.start2048() }, modifier = Modifier.testTag("g2048_restart_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "New Game")
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
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SCORE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("${state.score}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEST", style = MaterialTheme.typography.labelSmall, color = AyushVioletLight)
                    Text("${maxOf(state.score, bestScore ?: 0)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4x4 Grid Board with Swipe gestures
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, AyushGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(10.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {
                            val threshold = 40f
                            if (abs(totalDragX) > abs(totalDragY)) {
                                if (totalDragX > threshold) viewModel.move2048(Direction.RIGHT)
                                else if (totalDragX < -threshold) viewModel.move2048(Direction.LEFT)
                            } else {
                                if (totalDragY > threshold) viewModel.move2048(Direction.DOWN)
                                else if (totalDragY < -threshold) viewModel.move2048(Direction.UP)
                            }
                            totalDragX = 0f
                            totalDragY = 0f
                        },
                        onDrag = { _, dragAmount ->
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y
                        }
                    )
                }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..3) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..3) {
                            val value = state.grid[row * 4 + col]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (value == 0) Color(0xFF1E293B).copy(alpha = 0.5f) else getTileColor(value)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value > 0) {
                                    Text(
                                        text = "$value",
                                        fontWeight = FontWeight.Black,
                                        fontSize = if (value > 512) 18.sp else 22.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Game Over overlay
            if (state.isGameOver) {
                Surface(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                    color = Color.Black.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state.hasWon2048) "🎉 YOU REACHED 2048!" else "NO MORE MOVES",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = if (state.hasWon2048) AyushGreen else AyushRose
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Final Score: ${state.score}", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.start2048() },
                            modifier = Modifier.testTag("g2048_try_again_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Try Again")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // D-Pad / Directional Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FilledIconButton(
                onClick = { viewModel.move2048(Direction.UP) },
                modifier = Modifier.size(46.dp).testTag("g2048_btn_up"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
            }

            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                FilledIconButton(
                    onClick = { viewModel.move2048(Direction.LEFT) },
                    modifier = Modifier.size(46.dp).testTag("g2048_btn_left"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left")
                }

                FilledIconButton(
                    onClick = { viewModel.move2048(Direction.RIGHT) },
                    modifier = Modifier.size(46.dp).testTag("g2048_btn_right"),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right")
                }
            }

            FilledIconButton(
                onClick = { viewModel.move2048(Direction.DOWN) },
                modifier = Modifier.size(46.dp).testTag("g2048_btn_down"),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
            }
        }
    }
}
