package poker.players

import poker.AIPlayer
import poker.Board
import poker.Call
import poker.Check
import poker.Fold
import poker.Player
import poker.PlayerAction
import poker.Raise
import kotlin.random.Random

class RandomPlayer: AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        val options = listOf(Call(player, 0), Check(player), Fold(player), Raise(player, Random.nextInt(100, 10000)))
        return options[Random.nextInt(0, 5)]
    }
}