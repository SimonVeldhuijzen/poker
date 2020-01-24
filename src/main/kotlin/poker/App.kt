package poker

import poker.players.*

fun main(args: Array<String>) {
    val players = listOf(RandomPlayer("A"), RandomPlayer("B"), RandomPlayer("C"), RandomPlayer("D")).mapIndexed { i, ai ->
        Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players)
    board.playGame()

    for (player in players) {
        println("Player ${player.name} has wealth ${player.wealth}")
    }
}
