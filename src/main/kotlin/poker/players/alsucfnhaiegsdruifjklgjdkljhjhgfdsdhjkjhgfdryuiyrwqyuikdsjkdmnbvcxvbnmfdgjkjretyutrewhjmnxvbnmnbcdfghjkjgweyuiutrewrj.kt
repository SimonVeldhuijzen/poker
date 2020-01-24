package poker.players

import poker.*
import kotlin.math.pow
import kotlin.math.sign

class alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj : AIPlayer {
    override fun move(state: Board, player: Player): PlayerAction {
        val myBet = player.betThisRound
        val requiredBet = state.currentBet
        val myMunnie = player.wealth
        val toCall = requiredBet - myBet
        val dikkePot = state.players.sumBy {it.betTotal + it.betThisRound}
        val myCards = player.cards
        val communityCards = state.communityCards
        val deckCards = (CardSuit.values().flatMap { suit -> (2..14).map { rank -> Card(suit, CardRank(rank)) } } - communityCards - myCards).toMutableList()
        val pCount = state.players.count(Player::isActive)

        val start = System.currentTimeMillis()
        var total = 0
        var wins = 0.0
        while (System.currentTimeMillis() - start < 950) {
            deckCards.shuffle()
            val lolRandom = deckCards.take(5 - communityCards.size + 2)
            val other = communityCards + lolRandom.take(2)
            val coms = lolRandom.drop(2) + communityCards
            val myHand = rankHand(myCards + coms)
            val otherHand = rankHand(other + coms)
            total += 2
            wins += myHand.compareTo(otherHand).sign + 1
        }
        val pWin1 = wins / total
        val pWin = pWin1.pow(pCount - 1.0)

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
