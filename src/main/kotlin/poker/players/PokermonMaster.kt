package poker.players

import poker.*
import java.lang.Double.max
import kotlin.math.abs
import kotlin.math.max

class PokermonMaster(var safeLevel: Int = 1, var jeMoederLevel: Int = 4): AIPlayer {
    override var name = "PokermonMaster"

    override fun move(state: Board, player: Player): PlayerAction {
        val round = state.communityCards.size
        var maxBet = 100
        val handToRank = player.cards
        handToRank.addAll(state.communityCards)
        val rankedHand = rankHand(handToRank)
        if (rankedHand.handRank > safeLevel) {
            if (rankedHand.handRank > jeMoederLevel) {
                return Call(player)
            } else {
                return Raise(player, player.wealth * 999999999)
            }
        } else if (round < 2) {
            if (player.cards[0].suit == player.cards[1].suit) {
                maxBet = max(1000, player.wealth / 10)
            } else if (player.cards[0].rank.rank > 11 || player.cards[1].rank.rank > 11){
                maxBet = max(1000, player.wealth / 10)
            } else if (player.cards[0].rank.rank == player.cards[1].rank.rank ){
                maxBet = max(2000, player.wealth / 5)
            } else if (abs(player.cards[0].rank.rank - player.cards[1].rank.rank) == 1 ){
                maxBet = max(500, player.wealth / 20)
            }
        }
        if (player.betThisRound <= maxBet) {
            return Call(player)
        } else {
            if (state.currentBet - player.betThisRound <= maxBet) {
                return Call(player)
            } else {
                return Check(player)
            }
        }
    }
}
