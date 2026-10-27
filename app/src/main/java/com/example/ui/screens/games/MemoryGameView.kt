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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AyushCyanLight
import com.example.ui.theme.AyushGreen
import com.example.ui.theme.AyushVioletLight
import com.example.ui.viewmodel.ArcadeViewModel

private val memoryIcons: List<Pair<ImageVector, Color>> = listOf(
    Icons.Default.Cloud to Color(0xFF38BDF8),
    Icons.Default.Key to Color(0xFFFACC15),
    Icons.Default.Shield to Color(0xFF34D399),
    Icons.Default.RocketLaunch to Color(0xFFF87171),
    Icons.Default.Photo to Color(0xFFA78BFA),
    Icons.Default.Videocam to Color(0xFFFB923C),
    Icons.Default.MusicNote to Color(0xFFEC4899),
    Icons.Default.SportsEsports to Color(0xFF2DD4BF)
)

@Composable
fun MemoryGameView(
    viewModel: ArcadeViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.memoryState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state.cards.isEmpty()) {
            viewModel.startMemoryMatch()
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
            IconButton(onClick = onBack, modifier = Modifier.testTag("memory_back_button")) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Memory Match",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AyushCyanLight
            )

            IconButton(onClick = { viewModel.startMemoryMatch() }, modifier = Modifier.testTag("memory_restart_button")) {
                Icon(Icons.Default.Refresh, contentDescription = "Restart")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Stats Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("PAIRS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("${state.pairsFound} / ${state.totalPairs}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MOVES", style = MaterialTheme.typography.labelSmall, color = AyushVioletLight)
                    Text("${state.moves}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TIME", style = MaterialTheme.typography.labelSmall, color = AyushGreen)
                    Text("${state.elapsedSeconds}s", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4x4 Cards Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(8.dp)
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
                            val index = row * 4 + col
                            val card = state.cards.getOrNull(index)

                            if (card != null) {
                                val isVisible = card.isFaceUp || card.isMatched
                                val cardBg = when {
                                    card.isMatched -> AyushGreen.copy(alpha = 0.2f)
                                    card.isFaceUp -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(cardBg)
                                        .border(
                                            if (card.isMatched) 2.dp else 1.dp,
                                            if (card.isMatched) AyushGreen else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!card.isMatched && !card.isFaceUp) {
                                                viewModel.onMemoryCardClicked(card.id)
                                            }
                                        }
                                        .testTag("memory_card_$index"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isVisible) {
                                        val iconPair = memoryIcons[card.symbolIndex % memoryIcons.size]
                                        Icon(
                                            iconPair.first,
                                            contentDescription = null,
                                            tint = iconPair.second,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.Extension,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                            }
                        }
                    }
                }
            }

            // Win overlay
            if (state.isGameWon) {
                Surface(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                    color = Color.Black.copy(alpha = 0.85f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🏆 PUZZLE SOLVED!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AyushGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Completed in ${state.moves} moves and ${state.elapsedSeconds}s!", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.startMemoryMatch() },
                            modifier = Modifier.testTag("memory_play_again_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Again")
                        }
                    }
                }
            }
        }
    }
}
