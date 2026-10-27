package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

/**
 * Deterministic vector QR-pattern generator based on token hashing.
 * Renders authentic corner position markers and data modules.
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    qrColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = Color.White
) {
    val matrixSize = 25
    val grid = remember(data) {
        val hash = MessageDigest.getInstance("SHA-256").digest(data.toByteArray(Charsets.UTF_8))
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }

        // Finder patterns in three corners (top-left, top-right, bottom-left)
        fun setFinder(rowStart: Int, colStart: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isInner = r in 2..4 && c in 2..4
                    matrix[rowStart + r][colStart + c] = isBorder || isInner
                }
            }
        }

        setFinder(0, 0)
        setFinder(0, matrixSize - 7)
        setFinder(matrixSize - 7, 0)

        // Fill remaining with deterministic data modules
        var byteIdx = 0
        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                val inCorner = (r < 8 && c < 8) || (r < 8 && c >= matrixSize - 8) || (r >= matrixSize - 8 && c < 8)
                if (!inCorner) {
                    val bit = (hash[byteIdx % hash.size].toInt() ushr ((r + c) % 8)) and 1 == 1
                    matrix[r][c] = bit
                    byteIdx++
                }
            }
        }
        matrix
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 24.dp)) {
            val cellWidth = this.size.width / matrixSize
            val cellHeight = this.size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (grid[r][c]) {
                        drawRect(
                            color = qrColor,
                            topLeft = Offset(c * cellWidth, r * cellHeight),
                            size = Size(cellWidth * 1.02f, cellHeight * 1.02f)
                        )
                    }
                }
            }
        }
    }
}
