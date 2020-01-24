package poker.players

import poker.*

class PokermonMaster(commands: String): AIPlayer {
    override var name = "PokermonMaster"
    private val commands = commands.split(" ").toMutableList()

    override fun move(state: Board, player: Player): PlayerAction {
        val handToRank = player.cards
        handToRank.addAll(state.communityCards)
        val rankedHand = rankHand(handToRank)
        if (rankedHand.handRank > 2) {
            Call(player)
        } else {
            if (player.betThisRound <= 100) {
                Call(player)
            } else {
                Check(player)
            }
        }
    }
}
