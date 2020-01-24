package poker.players

import poker.*
import poker.TwoPair

class Mitchell: AIPlayer {

    override var name: String = "Mitchell"

    override fun move(state: Board, player: Player): PlayerAction {


        val listOfPlayers = listOf(MitCall(), MitchAllIn(), Mitcheck(), MitchFold(), Raisechell())
        val listOfCards = player.cards + state.communityCards

        val pokerhand = rankHand(player.cards + state.communityCards)

        println("type $pokerhand.")
        return listOfPlayers.random().move(state, player)
    }
}
