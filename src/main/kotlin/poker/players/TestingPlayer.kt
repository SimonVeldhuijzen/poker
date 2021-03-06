package poker.players

import poker.AIPlayer
import poker.Board
import poker.Call
import poker.Check
import poker.Fold
import poker.Player
import poker.PlayerAction
import poker.Raise

class TestingPlayer(commands: String): AIPlayer {
    override var name = "TestingPlayer"
    private val commands = commands.split(" ").toMutableList()

    override fun move(state: Board, player: Player): PlayerAction {
        return when (val command = commands.removeAt(0)) {
            "p" -> Check(player)
            "f" -> Fold(player)
            "c" -> Call(player)
            else -> Raise(player, command.toInt())
        }
    }
}
