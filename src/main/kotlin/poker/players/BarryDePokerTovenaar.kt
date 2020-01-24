package poker.players

import poker.*

/**
 * Created by daan.tebokkel on 1/24/20
 */
class BarryDePokerTovenaar: AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        val action = checkHandBeforeFlop(player)
        println("actie: $action")
        if (action == "check") {
            return Check(player)
        } else if (action == "allin"){
            return AllIn(player, 10000000)
        } else if (action == "call")  {
            return Call(player, 10)
        }else if (action == "raise")  {
            return Raise(player, state.currentBet * 2)
        }else {
            return Fold(player)
        }
    }


    //check hand
    fun checkHandBeforeFlop(player: Player): String {
        val pokerHand = rankHand(player.cards)
        println("Pokerhand: $pokerHand")
        when(pokerHand) {
            is Pair -> return "raise"
            is HighCard -> return "allin"
            is ThreeOfAKind -> return "raise"
            is Flush -> return "raise"
            is FourOfAKind -> return ""
            is StraightFlush -> return ""
            else -> return "fold"
        }
    }

    //check board state


    //
}