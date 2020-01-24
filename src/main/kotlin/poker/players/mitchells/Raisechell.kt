package poker.players.mitchells

import poker.*
import java.util.*

class Raisechell: AIPlayer {

    override var name: String = "Raisechell"

    override fun move(state: Board, player: Player): PlayerAction {
        return Call(player, Random().nextInt(player.wealth) + 1)
    }
}
