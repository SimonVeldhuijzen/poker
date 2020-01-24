package poker

import poker.players.*

fun main(args: Array<String>) {
    val players = listOf(MitCall(), Mitcheck(), MitchAllIn(), MitchFold(), Raisechell(), Mitchell()).mapIndexed { i, ai ->
        Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players)
    board.playGame()
}
