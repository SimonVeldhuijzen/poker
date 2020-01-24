package poker.players

import poker.*

class MitchFold: AIPlayer {

    override var name: String = "MitchFold"

    override fun move(state: Board, player: Player): PlayerAction {
        return Fold(player)
    }
}
