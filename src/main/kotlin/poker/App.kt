package poker

import poker.players.*

fun main(args: Array<String>) {

    var counter = 0

    for (i in 0..200) {
        val players = listOf(S("A"), RandomPlayer("B"), RandomPlayer("C"), RandomPlayer("D")).mapIndexed { i, ai ->
            Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
        }

        val board = Board(players)
        try {
            board.playGame()
        } catch (_: Exception) {
            try {
                board.playGame()
            } catch (_: Exception) {
                try {
                    board.playGame()
                } catch (_: Exception) {
                    try {
                        board.playGame()
                    } catch (_: Exception) {

                    }
                }
            }
        }

        for (player in players) {
            println("Player ${player.name} has wealth ${player.wealth}")
        }

        if (players[0].wealth > 0) {
            counter++
        }
    }

    println("win chance: ${counter / 200.0}")
}
