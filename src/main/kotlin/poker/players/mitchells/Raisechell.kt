package poker.players.mitchells

import poker.*
import java.util.*
import kotlin.random.Random

class Raisechell: AIPlayer {

    override var name: String = "Raisechell"

    override fun move(state: Board, player: Player): PlayerAction {
        return Raise(player, Random.nextInt(state.minBet, player.wealth))
    }
}
