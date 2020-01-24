package poker.players

import poker.AIPlayer
import poker.Board
import poker.Call
import poker.Check
import poker.Fold
import poker.Player
import poker.PlayerAction

class S(override var name: String = "Simon") : AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        return Call(player, 0)
    }
}