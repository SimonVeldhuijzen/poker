package poker

import poker.players.*
import poker.players.mitchells.Mitchell

fun main(args: Array<String>) {
    val players = listOf(
            S(),
            S2(),
            Mitchell(),
            alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj(),
            Cheetos(acceptableRisk = 0.5f),
            BarryDePokerTovenaar(),
            SirRaiseALot("SirRaiseALot", 1.0),
            PokermonMaster()
    ).mapIndexed { i, ai ->
        Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players.toMutableList())
    board.playGame()

    for (player in players) {
        println("Player ${player.name} has wealth ${player.wealth}")
    }
}
