package poker

import poker.players.HumanPlayer
import poker.players.RandomPlayer
import poker.players.alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj

fun main(args: Array<String>) {
    val players = listOf(RandomPlayer(), HumanPlayer(), alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj()).mapIndexed { i, ai ->
        Player(i, 10000, 0, 0, null, mutableListOf(), ai)
    }

    val board = Board(players)
    board.playGame()
}
