package poker.players

import poker.*

class alsucfnhaiegsdruifjklgjdkljhjhgfdsdhjkjhgfdryuiyrwqyuikdsjkdmnbvcxvbnmfdgjkjretyutrewhjmnxvbnmnbcdfghjkjgweyuiutrewrj : AIPlayer {
    override fun move(state: Board, player: Player): PlayerAction {
        val myBet = player.betThisRound
        val requiredBet = state.currentBet
        val myMunnie = player.wealth
        val toCall = requiredBet - myBet
        val dikkePot = state.players.sumBy(Player::betTotal)
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
            val coms = lolRandom.drop(2)
            val myHand = rankHand(myCards + coms)
            val otherHand = rankHand(other + coms)
            total += 2
            wins += myHand.compareTo(otherHand) + 1
        }
        val pLose1 = 1.0 - wins / total
        val pLose = Math.pow(pLose1, pCount - 1.0)

        val pWin = 1.0 - pLose

        val rBound = dikkePot*pWin/pLose
        return when {
            rBound - toCall >= state.minBet -> Raise(player, (rBound - toCall).toInt())
            toCall == 0 -> Check(player)
            rBound >= toCall -> Call(player, toCall)
            else -> Fold(player)
        }
    }
}
