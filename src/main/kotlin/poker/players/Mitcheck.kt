package poker.players

import poker.*

class Mitcheck: AIPlayer {

    override var name: String = "MitCheck"

    override fun move(state: Board, player: Player): PlayerAction {
        return Check(player)
    }
}
