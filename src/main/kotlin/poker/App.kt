package poker

import poker.players.HumanPlayer
import poker.players.RandomPlayer

fun main(args: Array<String>) {
    val players = listOf(RandomPlayer(), RandomPlayer(), RandomPlayer(), RandomPlayer()).mapIndexed { i, ai ->
        Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players)
    board.playGame()
}
