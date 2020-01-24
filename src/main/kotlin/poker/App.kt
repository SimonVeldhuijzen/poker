package poker

import poker.players.*

fun main(args: Array<String>) {
    val players = listOf(S("A"), S("B"), RandomPlayer("C"), Cheetos("D", 0.5F)).mapIndexed { i, ai ->
        Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
    }

//    try {
//        board.playGame()
//    } catch (_: Exception) {
//        try {
//            board.playGame()
//        } catch (_: Exception) {
//            try {
//                board.playGame()
//            } catch (_: Exception) {
//                try {
//                    board.playGame()
//                } catch (_: Exception) {
//
//                }
//            }
//        }
//    }
    val board = Board(players)
    board.playGame()

    for (player in players) {
        println("Player ${player.name} has wealth ${player.wealth}")
    }
}
