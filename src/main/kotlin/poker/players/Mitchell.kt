package poker.players

import poker.*
import poker.TwoPair
import kotlin.Pair

class Mitchell: AIPlayer {

    override var name: String = "Mitchell"

    override fun move(state: Board, player: Player): PlayerAction {
        val listOfPlayers = listOf(MitCall(), MitchAllIn(), Raisechell())
        val listOfCards = player.cards + state.communityCards
        val pokerhand = rankHand(listOfCards)

        val playerAction: PlayerAction
        if (pokerhand is HighCard || pokerhand is poker.Pair || pokerhand is TwoPair) {
            playerAction = Check(player)
        } else {
            playerAction = listOfPlayers.random().move(state, player)
        }
        return playerAction
    }
}
