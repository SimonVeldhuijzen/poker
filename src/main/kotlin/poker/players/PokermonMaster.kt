package poker.players

import poker.*

class PokermonMaster(): AIPlayer {
    override var name = "PokermonMaster"

    override fun move(state: Board, player: Player): PlayerAction {
        val handToRank = player.cards
        handToRank.addAll(state.communityCards)
        val rankedHand = rankHand(handToRank)
        if (rankedHand.handRank > 2) {
            return Call(player)
        } else {
            if (player.betThisRound <= 100) {
                return Call(player)
            } else {
                return Check(player)
            }
        }
    }
}
