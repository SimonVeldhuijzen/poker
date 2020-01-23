package poker

import poker.players.HumanPlayer

fun main(args: Array<String>) {
    val players = listOf(HumanPlayer(), HumanPlayer(), HumanPlayer(), HumanPlayer()).mapIndexed { i, ai ->
        Player(i, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players)
    board.playRound()
}
