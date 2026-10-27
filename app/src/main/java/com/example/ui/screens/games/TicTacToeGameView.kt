package com.example.ui.screens.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AyushBlueLight
import com.example.ui.theme.AyushCyanLight
import com.example.ui.theme.AyushGreen
import com.example.ui.theme.AyushVioletLight
import com.example.ui.viewmodel.ArcadeViewModel

@Composable
fun TicTacToeGameView(
    viewModel: ArcadeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.tttState.collectAsStateWithLifecycle()

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
            IconButton(onClick = onBack, modifier = Modifier.testTag("ttt_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Tic-Tac-Toe",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AyushVioletLight
            )

            IconButton(onClick = { viewModel.resetTicTacToe() }, modifier = Modifier.testTag("ttt_reset_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Board")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mode Toggles
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            FilterChip(
                selected = state.vsAi,
                onClick = { viewModel.resetTicTacToe(vsAi = true) },
                label = { Text("vs Computer AI") },
                leadingIcon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("ttt_mode_ai")
            )
            FilterChip(
                selected = !state.vsAi,
                onClick = { viewModel.resetTicTacToe(vsAi = false) },
                label = { Text("2 Players") },
                leadingIcon = { Icon(Icons.Default.SupervisorAccount, contentDescription = null, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.testTag("ttt_mode_2p")
            )
        }

        // Scoreboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ScorePill(
                title = "X (YOU)",
                score = state.xScore,
                color = AyushCyanLight,
                isActive = state.currentTurn == "X" && state.winner == null,
                modifier = Modifier.weight(1f)
            )
            ScorePill(
                title = "DRAWS",
                score = state.ties,
                color = Color.Gray,
                isActive = false,
                modifier = Modifier.weight(1f)
            )
            ScorePill(
                title = if (state.vsAi) "O (AI)" else "O (P2)",
                score = state.oScore,
                color = AyushVioletLight,
                isActive = state.currentTurn == "O" && state.winner == null,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Banner
        val statusText = when (state.winner) {
            "X" -> if (state.vsAi) "🎉 You Won!" else "🎉 Player X Won!"
            "O" -> if (state.vsAi) "🤖 AI Won!" else "🎉 Player O Won!"
            "DRAW" -> "🤝 It's a Draw!"
            else -> if (state.currentTurn == "X") "Turn: X" else (if (state.vsAi) "AI thinking..." else "Turn: O")
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (state.winner == "X") AyushGreen else if (state.winner == "O") AyushVioletLight else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3x3 Board
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0..2) {
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0..2) {
                            val index = row * 3 + col
                            val value = state.board[index]
                            val isWinCell = state.winningLine?.contains(index) == true

                            val cellBg = when {
                                isWinCell -> AyushGreen.copy(alpha = 0.3f)
                                value.isNotEmpty() -> MaterialTheme.colorScheme.surface
                                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cellBg)
                                    .border(
                                        if (isWinCell) 2.dp else 1.dp,
                                        if (isWinCell) AyushGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.onTicTacToeCellClicked(index) }
                                    .testTag("ttt_cell_$index"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value.isNotEmpty()) {
                                    Text(
                                        text = value,
                                        fontSize = 38.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (value == "X") AyushCyanLight else AyushVioletLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.winner != null) {
            Button(
                onClick = { viewModel.resetTicTacToe() },
                modifier = Modifier.fillMaxWidth(0.8f).testTag("ttt_play_again_button")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Next Round", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScorePill(
    title: String,
    score: Int,
    color: Color,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                if (isActive) 2.dp else 0.dp,
                if (isActive) color else Color.Transparent,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
            Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
