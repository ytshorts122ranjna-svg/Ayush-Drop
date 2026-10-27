package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.GameScoreEntity
import com.example.data.repository.ArcadeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- SNAKE MODELS ---
enum class Direction { UP, DOWN, LEFT, RIGHT }
enum class GameStatus { IDLE, RUNNING, PAUSED, GAME_OVER }

data class SnakeState(
    val snake: List<Pair<Int, Int>> = listOf(Pair(8, 8), Pair(8, 9), Pair(8, 10)),
    val food: Pair<Int, Int> = Pair(4, 4),
    val direction: Direction = Direction.UP,
    val score: Int = 0,
    val highScore: Int = 0,
    val status: GameStatus = GameStatus.IDLE,
    val gridSize: Int = 16
)

// --- TIC TAC TOE MODELS ---
data class TicTacToeState(
    val board: List<String> = List(9) { "" },
    val currentTurn: String = "X",
    val vsAi: Boolean = true,
    val winner: String? = null, // "X", "O", "DRAW", or null
    val winningLine: List<Int>? = null,
    val xScore: Int = 0,
    val oScore: Int = 0,
    val ties: Int = 0
)

// --- MEMORY MATCH MODELS ---
data class MemoryCard(
    val id: Int,
    val symbolIndex: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

data class MemoryMatchState(
    val cards: List<MemoryCard> = emptyList(),
    val moves: Int = 0,
    val pairsFound: Int = 0,
    val totalPairs: Int = 8,
    val isGameWon: Boolean = false,
    val elapsedSeconds: Int = 0,
    val isTimerActive: Boolean = false
)

// --- 2048 MODELS ---
data class G2048State(
    val grid: List<Int> = List(16) { 0 },
    val score: Int = 0,
    val bestScore: Int = 0,
    val isGameOver: Boolean = false,
    val hasWon2048: Boolean = false
)

// --- ROCK PAPER SCISSORS MODELS ---
enum class RPSChoice { ROCK, PAPER, SCISSORS }
enum class RPSResult { WIN, LOSE, DRAW }

data class RPSRound(
    val roundNum: Int,
    val playerChoice: RPSChoice,
    val computerChoice: RPSChoice,
    val result: RPSResult
)

data class RPSState(
    val playerWins: Int = 0,
    val computerWins: Int = 0,
    val draws: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastPlayerChoice: RPSChoice? = null,
    val lastComputerChoice: RPSChoice? = null,
    val lastResult: RPSResult? = null,
    val history: List<RPSRound> = emptyList()
)

class ArcadeViewModel(
    private val repository: ArcadeRepository
) : ViewModel() {

    var activeUsername: String = "Player"

    // Active game tab in Arcade: "HOME", "SNAKE", "TICTACTOE", "MEMORY", "2048", "RPS"
    val activeGame = MutableStateFlow("HOME")

    // High score streams
    val snakeHighScore = repository.getBestScore("SNAKE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val g2048HighScore = repository.getBestScore("G2048")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val recentGameScores: StateFlow<List<GameScoreEntity>> = repository.getRecentGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // -------------------------------------------------------------
    // GAME 1: SNAKE
    // -------------------------------------------------------------
    private val _snakeState = MutableStateFlow(SnakeState())
    val snakeState: StateFlow<SnakeState> = _snakeState.asStateFlow()
    private var snakeJob: Job? = null

    fun startSnake() {
        snakeJob?.cancel()
        val initialSnake = listOf(Pair(8, 8), Pair(8, 9), Pair(8, 10))
        val initialFood = spawnFood(initialSnake)
        _snakeState.value = SnakeState(
            snake = initialSnake,
            food = initialFood,
            direction = Direction.UP,
            score = 0,
            highScore = snakeHighScore.value ?: 0,
            status = GameStatus.RUNNING
        )
        runSnakeLoop()
    }

    fun changeSnakeDirection(newDirection: Direction) {
        val current = _snakeState.value.direction
        val isOpposite = when (newDirection) {
            Direction.UP -> current == Direction.DOWN
            Direction.DOWN -> current == Direction.UP
            Direction.LEFT -> current == Direction.RIGHT
            Direction.RIGHT -> current == Direction.LEFT
        }
        if (!isOpposite && _snakeState.value.status == GameStatus.RUNNING) {
            _snakeState.value = _snakeState.value.copy(direction = newDirection)
        }
    }

    private fun runSnakeLoop() {
        snakeJob = viewModelScope.launch {
            while (_snakeState.value.status == GameStatus.RUNNING) {
                val delayTime = (200 - (_snakeState.value.score * 3)).coerceAtLeast(80).toLong()
                delay(delayTime)
                stepSnake()
            }
        }
    }

    private fun stepSnake() {
        val state = _snakeState.value
        if (state.status != GameStatus.RUNNING) return

        val head = state.snake.first()
        val newHead = when (state.direction) {
            Direction.UP -> Pair(head.first, head.second - 1)
            Direction.DOWN -> Pair(head.first, head.second + 1)
            Direction.LEFT -> Pair(head.first - 1, head.second)
            Direction.RIGHT -> Pair(head.first + 1, head.second)
        }

        // Wall collision
        if (newHead.first < 0 || newHead.first >= state.gridSize ||
            newHead.second < 0 || newHead.second >= state.gridSize
        ) {
            endSnakeGame()
            return
        }

        // Self collision
        if (state.snake.contains(newHead)) {
            endSnakeGame()
            return
        }

        val newSnake = ArrayList<Pair<Int, Int>>()
        newSnake.add(newHead)

        val ateFood = newHead == state.food
        if (ateFood) {
            newSnake.addAll(state.snake)
            val newScore = state.score + 10
            val newHighScore = maxOf(newScore, state.highScore)
            val newFood = spawnFood(newSnake)
            _snakeState.value = state.copy(
                snake = newSnake,
                food = newFood,
                score = newScore,
                highScore = newHighScore
            )
        } else {
            for (i in 0 until state.snake.size - 1) {
                newSnake.add(state.snake[i])
            }
            _snakeState.value = state.copy(snake = newSnake)
        }
    }

    private fun spawnFood(snake: List<Pair<Int, Int>>): Pair<Int, Int> {
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (x in 0 until 16) {
            for (y in 0 until 16) {
                val cell = Pair(x, y)
                if (!snake.contains(cell)) emptyCells.add(cell)
            }
        }
        return if (emptyCells.isNotEmpty()) emptyCells[Random.nextInt(emptyCells.size)] else Pair(0, 0)
    }

    private fun endSnakeGame() {
        val finalScore = _snakeState.value.score
        _snakeState.value = _snakeState.value.copy(status = GameStatus.GAME_OVER)
        snakeJob?.cancel()
        if (finalScore > 0) {
            viewModelScope.launch {
                repository.saveGameScore("SNAKE", finalScore, "Length: ${_snakeState.value.snake.size}", activeUsername)
            }
        }
    }

    // -------------------------------------------------------------
    // GAME 2: TIC-TAC-TOE
    // -------------------------------------------------------------
    private val _tttState = MutableStateFlow(TicTacToeState())
    val tttState: StateFlow<TicTacToeState> = _tttState.asStateFlow()

    fun resetTicTacToe(vsAi: Boolean = _tttState.value.vsAi) {
        _tttState.value = _tttState.value.copy(
            board = List(9) { "" },
            currentTurn = "X",
            vsAi = vsAi,
            winner = null,
            winningLine = null
        )
    }

    fun onTicTacToeCellClicked(index: Int) {
        val state = _tttState.value
        if (state.board[index].isNotEmpty() || state.winner != null) return

        val newBoard = state.board.toMutableList()
        newBoard[index] = state.currentTurn

        val (winner, line) = checkTicTacToeWinner(newBoard)
        if (winner != null) {
            handleTicTacToeEnd(newBoard, winner, line)
            return
        }

        if (!newBoard.contains("")) {
            // Draw
            _tttState.value = state.copy(
                board = newBoard,
                winner = "DRAW",
                ties = state.ties + 1
            )
            return
        }

        val nextTurn = if (state.currentTurn == "X") "O" else "X"
        _tttState.value = state.copy(board = newBoard, currentTurn = nextTurn)

        // Computer AI move if playing vs AI and next turn is O
        if (state.vsAi && nextTurn == "O") {
            viewModelScope.launch {
                delay(250)
                makeAiMove()
            }
        }
    }

    private fun makeAiMove() {
        val state = _tttState.value
        if (state.winner != null) return

        val board = state.board
        val available = board.indices.filter { board[it].isEmpty() }
        if (available.isEmpty()) return

        // 1. Check if AI can win immediately
        for (i in available) {
            val test = board.toMutableList()
            test[i] = "O"
            if (checkTicTacToeWinner(test).first == "O") {
                commitAiMove(i)
                return
            }
        }

        // 2. Block player if player can win
        for (i in available) {
            val test = board.toMutableList()
            test[i] = "X"
            if (checkTicTacToeWinner(test).first == "X") {
                commitAiMove(i)
                return
            }
        }

        // 3. Take center if available
        if (available.contains(4)) {
            commitAiMove(4)
            return
        }

        // 4. Random available corner or edge
        val chosen = available[Random.nextInt(available.size)]
        commitAiMove(chosen)
    }

    private fun commitAiMove(index: Int) {
        val state = _tttState.value
        val newBoard = state.board.toMutableList()
        newBoard[index] = "O"

        val (winner, line) = checkTicTacToeWinner(newBoard)
        if (winner != null) {
            handleTicTacToeEnd(newBoard, winner, line)
            return
        }
        if (!newBoard.contains("")) {
            _tttState.value = state.copy(
                board = newBoard,
                winner = "DRAW",
                ties = state.ties + 1
            )
            return
        }

        _tttState.value = state.copy(board = newBoard, currentTurn = "X")
    }

    private fun checkTicTacToeWinner(b: List<String>): Pair<String?, List<Int>?> {
        val winPatterns = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
            listOf(0, 4, 8), listOf(2, 4, 6)             // diagonals
        )
        for (p in winPatterns) {
            if (b[p[0]].isNotEmpty() && b[p[0]] == b[p[1]] && b[p[1]] == b[p[2]]) {
                return Pair(b[p[0]], p)
            }
        }
        return Pair(null, null)
    }

    private fun handleTicTacToeEnd(board: List<String>, winner: String, line: List<Int>?) {
        val state = _tttState.value
        val xScore = if (winner == "X") state.xScore + 1 else state.xScore
        val oScore = if (winner == "O") state.oScore + 1 else state.oScore

        _tttState.value = state.copy(
            board = board,
            winner = winner,
            winningLine = line,
            xScore = xScore,
            oScore = oScore
        )

        viewModelScope.launch {
            repository.saveGameScore("TICTACTOE", if (winner == "X") 100 else 20, "Winner: $winner", activeUsername)
        }
    }

    // -------------------------------------------------------------
    // GAME 3: MEMORY MATCH
    // -------------------------------------------------------------
    private val _memoryState = MutableStateFlow(MemoryMatchState())
    val memoryState: StateFlow<MemoryMatchState> = _memoryState.asStateFlow()
    private var memoryTimerJob: Job? = null
    private var isEvaluatingPair = false

    fun startMemoryMatch() {
        memoryTimerJob?.cancel()
        isEvaluatingPair = false

        // 8 pairs = 16 cards
        val pairs = mutableListOf<MemoryCard>()
        var idCounter = 0
        for (sym in 0 until 8) {
            pairs.add(MemoryCard(id = idCounter++, symbolIndex = sym))
            pairs.add(MemoryCard(id = idCounter++, symbolIndex = sym))
        }
        pairs.shuffle()

        _memoryState.value = MemoryMatchState(
            cards = pairs,
            moves = 0,
            pairsFound = 0,
            totalPairs = 8,
            isGameWon = false,
            elapsedSeconds = 0,
            isTimerActive = true
        )

        memoryTimerJob = viewModelScope.launch {
            while (_memoryState.value.isTimerActive && !_memoryState.value.isGameWon) {
                delay(1000)
                _memoryState.value = _memoryState.value.copy(
                    elapsedSeconds = _memoryState.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun onMemoryCardClicked(cardId: Int) {
        if (isEvaluatingPair) return
        val state = _memoryState.value
        val card = state.cards.find { it.id == cardId } ?: return
        if (card.isFaceUp || card.isMatched) return

        val faceUpUnmatched = state.cards.filter { it.isFaceUp && !it.isMatched }

        if (faceUpUnmatched.isEmpty()) {
            // First card flip of the turn
            _memoryState.value = state.copy(
                cards = state.cards.map { if (it.id == cardId) it.copy(isFaceUp = true) else it }
            )
        } else if (faceUpUnmatched.size == 1) {
            // Second card flip
            val firstCard = faceUpUnmatched.first()
            val newCards = state.cards.map { if (it.id == cardId) it.copy(isFaceUp = true) else it }
            val newMoves = state.moves + 1

            if (firstCard.symbolIndex == card.symbolIndex) {
                // Match!
                val updatedCards = newCards.map {
                    if (it.id == firstCard.id || it.id == cardId) it.copy(isMatched = true) else it
                }
                val newPairs = state.pairsFound + 1
                val won = newPairs >= state.totalPairs

                _memoryState.value = state.copy(
                    cards = updatedCards,
                    moves = newMoves,
                    pairsFound = newPairs,
                    isGameWon = won,
                    isTimerActive = !won
                )

                if (won) {
                    val finalScore = (1000 - (newMoves * 15) - (state.elapsedSeconds * 5)).coerceAtLeast(100)
                    viewModelScope.launch {
                        repository.saveGameScore(
                            "MEMORY",
                            finalScore,
                            "Moves: $newMoves, Time: ${state.elapsedSeconds}s",
                            activeUsername
                        )
                    }
                }
            } else {
                // Not a match: show both for 800ms then flip back down
                _memoryState.value = state.copy(cards = newCards, moves = newMoves)
                isEvaluatingPair = true
                viewModelScope.launch {
                    delay(800)
                    _memoryState.value = _memoryState.value.copy(
                        cards = _memoryState.value.cards.map {
                            if (it.id == firstCard.id || it.id == cardId) it.copy(isFaceUp = false) else it
                        }
                    )
                    isEvaluatingPair = false
                }
            }
        }
    }

    // -------------------------------------------------------------
    // GAME 4: 2048 PUZZLE
    // -------------------------------------------------------------
    private val _g2048State = MutableStateFlow(G2048State())
    val g2048State: StateFlow<G2048State> = _g2048State.asStateFlow()

    fun start2048() {
        val emptyGrid = MutableList(16) { 0 }
        spawnRandomTile(emptyGrid)
        spawnRandomTile(emptyGrid)
        _g2048State.value = G2048State(
            grid = emptyGrid,
            score = 0,
            bestScore = g2048HighScore.value ?: 0,
            isGameOver = false,
            hasWon2048 = false
        )
    }

    fun move2048(direction: Direction) {
        val state = _g2048State.value
        if (state.isGameOver) return

        val currentGrid = state.grid
        val newGrid = currentGrid.toMutableList()
        var scoreAdd = 0
        var moved = false

        when (direction) {
            Direction.LEFT -> {
                for (row in 0 until 4) {
                    val line = listOf(newGrid[row * 4], newGrid[row * 4 + 1], newGrid[row * 4 + 2], newGrid[row * 4 + 3])
                    val (merged, add) = mergeLine(line)
                    scoreAdd += add
                    for (col in 0 until 4) {
                        if (newGrid[row * 4 + col] != merged[col]) moved = true
                        newGrid[row * 4 + col] = merged[col]
                    }
                }
            }
            Direction.RIGHT -> {
                for (row in 0 until 4) {
                    val line = listOf(newGrid[row * 4 + 3], newGrid[row * 4 + 2], newGrid[row * 4 + 1], newGrid[row * 4])
                    val (merged, add) = mergeLine(line)
                    scoreAdd += add
                    for (col in 0 until 4) {
                        if (newGrid[row * 4 + 3 - col] != merged[col]) moved = true
                        newGrid[row * 4 + 3 - col] = merged[col]
                    }
                }
            }
            Direction.UP -> {
                for (col in 0 until 4) {
                    val line = listOf(newGrid[col], newGrid[col + 4], newGrid[col + 8], newGrid[col + 12])
                    val (merged, add) = mergeLine(line)
                    scoreAdd += add
                    for (row in 0 until 4) {
                        if (newGrid[col + row * 4] != merged[row]) moved = true
                        newGrid[col + row * 4] = merged[row]
                    }
                }
            }
            Direction.DOWN -> {
                for (col in 0 until 4) {
                    val line = listOf(newGrid[col + 12], newGrid[col + 8], newGrid[col + 4], newGrid[col])
                    val (merged, add) = mergeLine(line)
                    scoreAdd += add
                    for (row in 0 until 4) {
                        if (newGrid[col + (3 - row) * 4] != merged[row]) moved = true
                        newGrid[col + (3 - row) * 4] = merged[row]
                    }
                }
            }
        }

        if (moved) {
            spawnRandomTile(newGrid)
            val newScore = state.score + scoreAdd
            val newBest = maxOf(newScore, state.bestScore)
            val won = newGrid.contains(2048)
            val over = check2048GameOver(newGrid)

            _g2048State.value = state.copy(
                grid = newGrid,
                score = newScore,
                bestScore = newBest,
                hasWon2048 = won,
                isGameOver = over
            )

            if (over) {
                viewModelScope.launch {
                    repository.saveGameScore("G2048", newScore, "Max: ${newGrid.maxOrNull() ?: 0}", activeUsername)
                }
            }
        }
    }

    private fun mergeLine(line: List<Int>): Pair<List<Int>, Int> {
        val nonZero = line.filter { it != 0 }.toMutableList()
        val result = mutableListOf<Int>()
        var scoreGained = 0
        var i = 0
        while (i < nonZero.size) {
            if (i + 1 < nonZero.size && nonZero[i] == nonZero[i + 1]) {
                val mergedVal = nonZero[i] * 2
                result.add(mergedVal)
                scoreGained += mergedVal
                i += 2
            } else {
                result.add(nonZero[i])
                i += 1
            }
        }
        while (result.size < 4) {
            result.add(0)
        }
        return Pair(result, scoreGained)
    }

    private fun spawnRandomTile(grid: MutableList<Int>) {
        val emptyIndices = grid.indices.filter { grid[it] == 0 }
        if (emptyIndices.isNotEmpty()) {
            val idx = emptyIndices[Random.nextInt(emptyIndices.size)]
            grid[idx] = if (Random.nextFloat() < 0.9f) 2 else 4
        }
    }

    private fun check2048GameOver(grid: List<Int>): Boolean {
        if (grid.contains(0)) return false
        // check horizontal neighbors
        for (r in 0 until 4) {
            for (c in 0 until 3) {
                if (grid[r * 4 + c] == grid[r * 4 + c + 1]) return false
            }
        }
        // check vertical neighbors
        for (c in 0 until 4) {
            for (r in 0 until 3) {
                if (grid[r * 4 + c] == grid[(r + 1) * 4 + c]) return false
            }
        }
        return true
    }

    // -------------------------------------------------------------
    // GAME 5: ROCK PAPER SCISSORS
    // -------------------------------------------------------------
    private val _rpsState = MutableStateFlow(RPSState())
    val rpsState: StateFlow<RPSState> = _rpsState.asStateFlow()

    fun playRPS(choice: RPSChoice) {
        val choices = RPSChoice.values()
        val computerChoice = choices[Random.nextInt(choices.size)]

        val result = when {
            choice == computerChoice -> RPSResult.DRAW
            (choice == RPSChoice.ROCK && computerChoice == RPSChoice.SCISSORS) ||
                    (choice == RPSChoice.PAPER && computerChoice == RPSChoice.ROCK) ||
                    (choice == RPSChoice.SCISSORS && computerChoice == RPSChoice.PAPER) -> RPSResult.WIN
            else -> RPSResult.LOSE
        }

        val state = _rpsState.value
        val newPlayerWins = if (result == RPSResult.WIN) state.playerWins + 1 else state.playerWins
        val newCompWins = if (result == RPSResult.LOSE) state.computerWins + 1 else state.computerWins
        val newDraws = if (result == RPSResult.DRAW) state.draws + 1 else state.draws

        val newStreak = if (result == RPSResult.WIN) state.currentStreak + 1 else 0
        val newBestStreak = maxOf(newStreak, state.bestStreak)

        val newRound = RPSRound(
            roundNum = state.history.size + 1,
            playerChoice = choice,
            computerChoice = computerChoice,
            result = result
        )

        _rpsState.value = state.copy(
            playerWins = newPlayerWins,
            computerWins = newCompWins,
            draws = newDraws,
            currentStreak = newStreak,
            bestStreak = newBestStreak,
            lastPlayerChoice = choice,
            lastComputerChoice = computerChoice,
            lastResult = result,
            history = listOf(newRound) + state.history
        )

        viewModelScope.launch {
            repository.saveGameScore("RPS", newBestStreak * 10, "Wins: $newPlayerWins / ${state.history.size + 1}", activeUsername)
        }
    }

    fun resetRPS() {
        _rpsState.value = RPSState()
    }
}
