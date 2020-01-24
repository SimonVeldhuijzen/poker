package poker.players

import poker.*
import kotlin.math.pow
import kotlin.math.sign

class alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj : AIPlayer {
    override var name: String = "<(''<)"
    override fun move(state: Board, player: Player): PlayerAction {
        val myBet = player.betThisRound
        val requiredBet = state.currentBet
        val myMunnie = player.wealth
        val toCall = requiredBet - myBet
        val dikkePot = state.players.sumBy {it.betTotal + it.betThisRound}
        val myCards = player.cards
        val communityCards = state.communityCards
        val deckCards = (CardSuit.values().flatMap { suit -> (2..14).map { rank -> Card(suit, CardRank(rank)) } } - communityCards - myCards).toMutableList()
        val pCount = state.players.size - state.foldedPlayers.size

        val start = System.currentTimeMillis()
        var total = 0
        var wins = 0.0
        while (System.currentTimeMillis() - start < 950 && total < 10000) {
            deckCards.shuffle()
            var lolRandom = deckCards.take(5 - communityCards.size + 2 * pCount)
            val coms = communityCards + lolRandom.take(5 - communityCards.size)
            lolRandom = lolRandom.drop(5 - communityCards.size)
            val otherHands = (1..pCount).map {
                val other = lolRandom.take(2) + coms
                lolRandom = lolRandom.drop(2)
                rankHand(other)
            }
            val myHand = rankHand(myCards + coms)
            total += 2
            wins += otherHands.map{ myHand.compareTo(it).sign + 1 }.min()!!
        }
        val pWin = wins / total

        val pLose = 1.0 - pWin

        val rBound = dikkePot*pWin/pLose
        return when {
            rBound - toCall >= state.minBet -> Raise(player, (rBound - toCall).toInt())
            toCall == 0 -> Check(player)
            rBound >= toCall -> Call(player)
            else -> Fold(player)
        }
    }
}
