package poker.players

import poker.AIPlayer
import poker.Board
import poker.Call
import poker.Check
import poker.Fold
import poker.Player
import poker.PlayerAction
import poker.Raise

class HumanPlayer: AIPlayer {
    override var name: String = "HumanPlayer"

    override fun move(state: Board, player: Player): PlayerAction {
        return when (val action = readLine()) {
            "p" -> Check(player)
            "f" -> Fold(player)
            "c" -> Call(player, 0)
            else -> Raise(player, action!!.toInt())
        }
    }
}
