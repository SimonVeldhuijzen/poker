package poker.players

import poker.AIPlayer
import poker.Board
import poker.Check
import poker.Player
import poker.PlayerAction

class S(override var name: String) : AIPlayer {

    override var name = "Simon"

    override fun move(state: Board, player: Player): PlayerAction {
        return Check(player)
    }
}