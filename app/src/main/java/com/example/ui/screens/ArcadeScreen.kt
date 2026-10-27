package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.ui.screens.games.G2048GameView
import com.example.ui.screens.games.MemoryGameView
import com.example.ui.screens.games.RpsGameView
import com.example.ui.screens.games.SnakeGameView
import com.example.ui.screens.games.TicTacToeGameView
import com.example.ui.theme.AyushAmber
import com.example.ui.theme.AyushCyanLight
import com.example.ui.theme.AyushGreen
import com.example.ui.theme.AyushRose
import com.example.ui.theme.AyushVioletLight
import com.example.ui.viewmodel.ArcadeViewModel

data class GameCardItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val accentColor: Color,
    val badge: String
)

private val gamesList = listOf(
    GameCardItem(
        id = "SNAKE",
        title = "Cyber Snake",
        subtitle = "Steer the snake, grab quantum data tokens, and beat the high score.",
        icon = Icons.Default.Speed,
        accentColor = AyushCyanLight,
        badge = "Classic Arcade"
    ),
    GameCardItem(
        id = "TICTACTOE",
        title = "Tic-Tac-Toe",
        subtitle = "Play against a smart AI bot or challenge a friend in pass & play mode.",
        icon = Icons.Default.Grid3x3,
        accentColor = AyushVioletLight,
        badge = "AI & 2-Player"
    ),
    GameCardItem(
        id = "MEMORY",
        title = "Memory Match",
        subtitle = "Flip cards to find pairs with minimal moves and clock the best time.",
        icon = Icons.Default.Extension,
        accentColor = AyushGreen,
        badge = "Puzzle"
    ),
    GameCardItem(
        id = "2048",
        title = "2048 Matrix",
        subtitle = "Slide and merge matching numbered tiles to conquer the 2048 grid.",
        icon = Icons.Default.ViewModule,
        accentColor = AyushAmber,
        badge = "Strategy"
    ),
    GameCardItem(
        id = "RPS",
        title = "Rock Paper Scissors",
        subtitle = "Battle the computer, track round histories, and climb winning streaks.",
        icon = Icons.Default.SportsEsports,
        accentColor = AyushRose,
        badge = "Instant Play"
    )
)

@Composable
fun ArcadeScreen(
    viewModel: ArcadeViewModel,
    currentUser: String
) {
    val activeGame by viewModel.activeGame.collectAsStateWithLifecycle()
    viewModel.activeUsername = currentUser

    // BackHandler: If inside a game, go back to Arcade HOME
    BackHandler(enabled = activeGame != "HOME") {
        viewModel.activeGame.value = "HOME"
    }

    when (activeGame) {
        "SNAKE" -> SnakeGameView(viewModel = viewModel, onBack = { viewModel.activeGame.value = "HOME" })
        "TICTACTOE" -> TicTacToeGameView(viewModel = viewModel, onBack = { viewModel.activeGame.value = "HOME" })
        "MEMORY" -> MemoryGameView(viewModel = viewModel, onBack = { viewModel.activeGame.value = "HOME" })
        "2048" -> G2048GameView(viewModel = viewModel, onBack = { viewModel.activeGame.value = "HOME" })
        "RPS" -> RpsGameView(viewModel = viewModel, onBack = { viewModel.activeGame.value = "HOME" })
        else -> ArcadeHubHome(viewModel = viewModel)
    }
}

@Composable
private fun ArcadeHubHome(
    viewModel: ArcadeViewModel
) {
    val snakeBest by viewModel.snakeHighScore.collectAsStateWithLifecycle()
    val g2048Best by viewModel.g2048HighScore.collectAsStateWithLifecycle()
    val recentScores by viewModel.recentGameScores.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AyushVioletLight.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(AyushVioletLight.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Gamepad, contentDescription = null, tint = AyushVioletLight, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AyushDrop Arcade Hub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "100% Free • No Ads • Offline Playable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // High Scores Ticker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AyushCyanLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Snake Best", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${snakeBest ?: 0}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AyushAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("2048 Best", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("${g2048Best ?: 0}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Choose a Game to Play",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // 5 Interactive Game Cards
        items(gamesList) { game ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { viewModel.activeGame.value = game.id }
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .testTag("launch_game_${game.id.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(game.accentColor.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(game.icon, contentDescription = null, tint = game.accentColor, modifier = Modifier.size(26.dp))
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = game.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = game.accentColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = game.badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = game.accentColor,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = game.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Recent Arcade Logs
        if (recentScores.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Matches",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(recentScores.take(5)) { match ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(match.gameId, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(match.details.ifBlank { "Score: ${match.score}" }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("+${match.score} pts", style = MaterialTheme.typography.labelMedium, color = AyushGreen)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
