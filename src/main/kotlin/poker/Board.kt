package poker

enum class CardSuit { HEARTS, DIAMONDS, SPADES, CLUBS }
data class CardRank(val rank: Int)
data class Card(val suit: CardSuit, val rank: CardRank)

sealed class TurnAction { abstract fun copy(): TurnAction }
data class Check(val playerNumber: Int): TurnAction() { override fun copy() = Check(playerNumber) }
class Fold(val playerNumber: Int): TurnAction() { override fun copy() = Fold(playerNumber) }
class Call(val playerNumber: Int): TurnAction() { override fun copy() = Call(playerNumber) }
class Bet(val playerNumber: Int, val amount: Int): TurnAction() { override fun copy() = Bet(playerNumber) }
class Raise(val playerNumber: Int, val amount: Int): TurnAction() { override fun copy() = Raise(playerNumber) }
class Flop(val first: Card, val second: Card, val third: Card)
class Turn(val fourth: Card)
class River(val fifth: Card)
class Showdown()

class Board(val players: List<AIPlayer>, val minBet: Int = 100, initialWealth: Int = 10000) {
    var pot = 0
    val wealth = players.associateWith { initialWealth }.toMutableMap()
    val actions = mutableListOf<TurnAction>()
    var dealerIndex = 0
    var dealer = players[dealerIndex]
    val deck = mutableListOf<Card>()
    val communityCards = mutableListOf<Card>()
    val playerCards = players.associateWith { mutableSetOf<Card>() }.toMutableMap()

    private fun newRound() {
        pot = 0
        actions.clear()
        dealerIndex = (dealerIndex + 1) % players.size
        dealer = players[dealerIndex]
        dealCards()
    }

    private fun dealCards() {
        communityCards.clear()
        deck.clear()
        deck.addAll(CardSuit.values().flatMap { suit -> (0..12).map { rank -> Card(suit, CardRank(rank)) } })
        deck.shuffle()
        for (player in players) {
            playerCards[player]!!.clear()
            playerCards[player]!!.add(deck.removeAt(0))
            playerCards[player]!!.add(deck.removeAt(0))
        }
    }



    fun copyFor(player: AIPlayer) {
        val board = Board(players, minBet).also {
            it.pot = pot
            it.actions.addAll(actions)
            it.dealerIndex = dealerIndex
            it.dealer = dealer
            it.communityCards.addAll(communityCards)


            for (p in players) {
                it.wealth[p] = wealth[p]!!
            }

        }
    }
}