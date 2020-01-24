package poker.players

import poker.*

class PokermonMaster(var safeLevel: Int = 1, var jeMoederLevel: Int = 4): AIPlayer {
    override var name = "PokermonMaster"

    override fun move(state: Board, player: Player): PlayerAction {
        val handToRank = player.cards
        handToRank.addAll(state.communityCards)
        val rankedHand = rankHand(handToRank)
        if (rankedHand.handRank > safeLevel) {
            if (rankedHand.handRank > jeMoederLevel) {
                return Call(player)
            } else {
                return Raise(player, player.wealth * 999999999)
            }
        } else {
            if (player.betThisRound <= 100) {
                return Call(player)
            } else {
                if (state.currentBet - player.betThisRound == state.minBet) {
                    return Call(player)
                } else {
                    return Check(player)
                }
            }
        }
    }
}
