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

class S(override var name: String = "Simon") : AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        if (state.communityCards.size == 0) {
            return preflop(state, player)
        } else if (state.communityCards.size == 3) {
            return Call(player, 0)
        }

        return Call(player, 0)
    }

    fun preflop(state: Board, player: Player): PlayerAction {
        val betDelta = state.currentBet - player.betThisRound
        val betDeltaPercentage = (betDelta * 100.0) / player.wealth
        var handScore = player.cards.sumBy { it.rank.rank }
        if (player.cards.distinctBy { it.rank.rank }.size == 1) {
            handScore += 10
        }

        if (player.cards.distinctBy { it.suit }.size == 1) {
            handScore += 4
        }

        if (handScore > 36) {
            return AllIn(player, 0)
        }

        val risk = betDeltaPercentage / handScore
        if (risk > 100) {
            return Check(player)
        } else if (risk > 50) {
            return Call(player, 0)
        } else {
            return Raise(player, (player.wealth - betDelta) / risk.toInt())
        }
    }

    fun afterFlop(state: Board, player: Player): PlayerAction {
        return when (rankHand(player.cards)) {
            is HighCard -> Check(player)
            is Pair -> Check(player)
            is TwoPair -> if (state.currentBet - player.betThisRound < player.wealth / 15) Call(player, max(state.minBet, 0)) else Check(player)
            is ThreeOfAKind -> if (state.currentBet - player.betThisRound < player.wealth / 10) Call(player, max(state.minBet, 0)) else Check(player)
            is Straight -> Raise(player, max(state.minBet, player.wealth / 6))
            is Flush -> Raise(player, max(state.minBet, player.wealth / 6))
            is FullHouse -> Raise(player, max(state.minBet, player.wealth / 3))
            is FourOfAKind -> AllIn(player, 0)
            is StraightFlush -> AllIn(player, 0)
        }
    }
}