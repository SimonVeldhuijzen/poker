package poker.players

import poker.*

class MitchAllIn: AIPlayer {
    override var name: String = "MitchellAllIn"

    override fun move(state: Board, player: Player): PlayerAction {
        return AllIn(player, player.wealth)
    }
}
