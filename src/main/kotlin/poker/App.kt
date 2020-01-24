package poker

import poker.players.*
import poker.players.mitchells.Mitchell

fun main(args: Array<String>) {
    val players = listOf(
        S(),
        Mitchell(),
        alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj(),
        Cheetos("Joeri", 0.5f),
        BarryDePokerTovenaar(),
        SirRaiseALot("SirRaiseALot")
    ).mapIndexed { i, ai ->
        Player(i, ai.name, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players)
    board.playGame()

    for (player in players) {
        println("Player ${player.name} has wealth ${player.wealth}")
    }
}
