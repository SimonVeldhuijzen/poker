package poker.players

import poker.AIPlayer
import poker.AllIn
import poker.Board
import poker.Call
import poker.Check
import poker.Flush
import poker.FourOfAKind
import poker.FullHouse
import poker.HighCard
import poker.Player
import poker.PlayerAction
import poker.Raise
import poker.Straight
import poker.StraightFlush
import poker.ThreeOfAKind
import poker.TwoPair
import poker.Pair
import poker.rankHand
import kotlin.math.max

class S2(override var name: String = "100") : AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        return Raise(player, 100)
    }
}
