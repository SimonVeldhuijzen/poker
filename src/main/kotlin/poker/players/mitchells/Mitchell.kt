package poker.players.mitchells

import poker.*
import poker.TwoPair

class Mitchell: AIPlayer {

    override var name: String = "Mitchell"

    override fun move(state: Board, player: Player): PlayerAction {
        val listOfCards = player.cards + state.communityCards
        val pokerhand = rankHand(listOfCards)

        val playerAction: PlayerAction
        when (pokerhand) {
            is HighCard -> playerAction = Check(player)
            is Pair -> playerAction = Check(player)
            is TwoPair -> playerAction = MitCall().move(state, player)
            is ThreeOfAKind -> playerAction = Raisechell().move(state, player)
            is Straight -> playerAction = Raisechell().move(state, player)
            is Flush -> playerAction = Raisechell().move(state, player)
            is FullHouse -> playerAction = Raisechell().move(state, player)
            is FourOfAKind -> playerAction = MitchAllIn().move(state, player)
            is StraightFlush -> playerAction = MitchAllIn().move(state, player)
        }
        return playerAction
    }
}
