package poker.players

import poker.*

class RaymondPlayer : AIPlayer {
    override fun move(state: Board, player: Player): PlayerAction {
        return Check(player)
    }
}
