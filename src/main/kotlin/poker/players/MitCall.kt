package poker.players

import poker.*
import java.util.*

class MitCall: AIPlayer {
    override var name: String = "MitCall"

    override fun move(state: Board, player: Player): PlayerAction {
        return Call(player)
    }
}
