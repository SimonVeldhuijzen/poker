package poker.players

import poker.*

/**
 * Created by daan.tebokkel on 1/24/20
 */
class BarryDePokerTovenaar(override var name: String = "Barry") : AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        var action: String = ""
        when(state.communityCards.size){
            0 -> action = checkHandBeforeFlop(player)
            3 -> action = checkHandAfterFlop(player)
            4 -> action = checkHandAfterTurn(player)
            else -> action = checkHandAfterRiver(player)
        }
        println("actie: $action")
        if (action == "check") {
            return Check(player)
        } else if (action == "allin"){
            return AllIn(player, 10000000)
        } else if (action == "call")  {
            return Call(player)
        } else if (action == "raise")  {
            return Raise(player, state.currentBet * 2)
        } else if (action == "fold")  {
            return Fold(player)
        } else{
            return Fold(player)
        }
    }


    //check hand
    fun checkHandBeforeFlop(player: Player): String {
        val pokerHand = rankHand(player.cards)
        println("Pokerhand: ${pokerHand::class.simpleName}")
        when(pokerHand) {
            is Pair -> return "raise"
            is HighCard -> return "call"
            is ThreeOfAKind -> return "raise"
            is Flush -> return "raise"
            is FourOfAKind -> return "allin"
            is StraightFlush -> return "allin"
            else -> return "fold"
        }
    }

    fun checkHandAfterFlop(player: Player): String {
        val pokerHand = rankHand(player.cards)
        println("Pokerhand: ${pokerHand::class.simpleName}")
        when(pokerHand) {
            is Pair -> return "raise"
            is HighCard -> return "fold"
            is ThreeOfAKind -> return "raise"
            is Flush -> return "raise"
            is FourOfAKind -> return "allin"
            is StraightFlush -> return "allin"
            else -> return "fold"
        }
    }

    fun checkHandAfterTurn(player: Player): String {
        val pokerHand = rankHand(player.cards)
        println("Pokerhand: ${pokerHand::class.simpleName}")
        when(pokerHand) {
            is Pair -> return "raise"
            is HighCard -> return "fold"
            is ThreeOfAKind -> return "raise"
            is Flush -> return "raise"
            is FourOfAKind -> return "allin"
            is StraightFlush -> return "allin"
            else -> return "fold"
        }
    }

    fun checkHandAfterRiver(player: Player): String {
        val pokerHand = rankHand(player.cards)
        println("Pokerhand: ${pokerHand::class.simpleName}")
        when(pokerHand) {
            is Pair -> return "fold"
            is HighCard -> return "fold"
            is ThreeOfAKind -> return "raise"
            is Flush -> return "allin"
            is FourOfAKind -> return "allin"
            is StraightFlush -> return "allin"
            else -> return "fold"
        }
    }

    //check board state
    //check own bet
    //check other bets
}