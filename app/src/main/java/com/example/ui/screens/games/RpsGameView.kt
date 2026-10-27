package com.example.ui.screens.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.theme.AyushAmber
import com.example.ui.theme.AyushCyanLight
import com.example.ui.theme.AyushGreen
import com.example.ui.theme.AyushRose
import com.example.ui.theme.AyushVioletLight
import com.example.ui.viewmodel.ArcadeViewModel
import com.example.ui.viewmodel.RPSChoice
import com.example.ui.viewmodel.RPSResult

private fun getRpsEmoji(choice: RPSChoice?): String {
    return when (choice) {
        RPSChoice.ROCK -> "🪨 Rock"
        RPSChoice.PAPER -> "📄 Paper"
        RPSChoice.SCISSORS -> "✂️ Scissors"
        null -> "❓"
    }
}

@Composable
fun RpsGameView(
    viewModel: ArcadeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.rpsState.collectAsStateWithLifecycle()

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
            IconButton(onClick = onBack, modifier = Modifier.testTag("rps_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Rock Paper Scissors",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AyushAmber
            )

            IconButton(onClick = { viewModel.resetRPS() }, modifier = Modifier.testTag("rps_reset_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset RPS")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Streak & Score Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WINS", style = MaterialTheme.typography.labelSmall, color = AyushGreen)
                    Text("${state.playerWins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOSSES", style = MaterialTheme.typography.labelSmall, color = AyushRose)
                    Text("${state.computerWins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("STREAK", style = MaterialTheme.typography.labelSmall, color = AyushAmber)
                    Text("🔥 ${state.currentStreak}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // VS Showcase Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOU", style = MaterialTheme.typography.labelMedium, color = AyushCyanLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getRpsEmoji(state.lastPlayerChoice),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text("VS", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.Gray)

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AI BOT", style = MaterialTheme.typography.labelMedium, color = AyushVioletLight)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getRpsEmoji(state.lastComputerChoice),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (state.lastResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val (resText, resColor) = when (state.lastResult) {
                        RPSResult.WIN -> "🎉 YOU WIN!" to AyushGreen
                        RPSResult.LOSE -> "💀 COMPUTER WINS!" to AyushRose
                        RPSResult.DRAW -> "🤝 IT'S A TIE!" to AyushAmber
                        null -> "" to Color.White
                    }
                    Text(text = resText, fontSize = 18.sp, fontWeight = FontWeight.Black, color = resColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Choose Your Hand:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RpsChoiceCard(
                label = "ROCK",
                emoji = "🪨",
                modifier = Modifier.weight(1f).testTag("rps_btn_rock"),
                onClick = { viewModel.playRPS(RPSChoice.ROCK) }
            )
            RpsChoiceCard(
                label = "PAPER",
                emoji = "📄",
                modifier = Modifier.weight(1f).testTag("rps_btn_paper"),
                onClick = { viewModel.playRPS(RPSChoice.PAPER) }
            )
            RpsChoiceCard(
                label = "SCISSORS",
                emoji = "✂️",
                modifier = Modifier.weight(1f).testTag("rps_btn_scissors"),
                onClick = { viewModel.playRPS(RPSChoice.SCISSORS) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battle History
        Text(
            text = "Match History",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(state.history) { round ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Round #${round.roundNum}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("${getRpsEmoji(round.playerChoice)} vs ${getRpsEmoji(round.computerChoice)}", fontSize = 12.sp)
                        val badgeColor = when (round.result) {
                            RPSResult.WIN -> AyushGreen
                            RPSResult.LOSE -> AyushRose
                            RPSResult.DRAW -> AyushAmber
                        }
                        Text(
                            text = round.result.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RpsChoiceCard(
    label: String,
    emoji: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}
