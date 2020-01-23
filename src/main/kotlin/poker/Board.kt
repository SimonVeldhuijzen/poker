package poker

import kotlin.math.min

enum class CardSuit { HEARTS, DIAMONDS, SPADES, CLUBS }
// 2-10 -> 2-10, JQKA -> 11-14
data class CardRank(val rank: Int)
data class Card(val suit: CardSuit, val rank: CardRank)

sealed class TurnAction { abstract fun copy(): TurnAction }
sealed class PlayerAction() : TurnAction() { abstract val playerNumber: Int }
class Check(override val playerNumber: Int): PlayerAction() { override fun copy() = Check(playerNumber) }
class Fold(override val playerNumber: Int): PlayerAction() { override fun copy() = Fold(playerNumber) }
class Call(override val playerNumber: Int): PlayerAction() { override fun copy() = Call(playerNumber) }
class Bet(override val playerNumber: Int, val amount: Int): PlayerAction() { override fun copy() = Bet(playerNumber, amount) }
class Raise(override val playerNumber: Int, val amount: Int): PlayerAction() { override fun copy() = Raise(playerNumber, amount) }
class Flop(val first: Card, val second: Card, val third: Card): TurnAction() { override fun copy() = Flop( first.copy(), second.copy(), third.copy()) }
class Turn(val fourth: Card): TurnAction() { override fun copy() = Turn(fourth.copy()) }
class River(val fifth: Card): TurnAction() { override fun copy() = River(fifth.copy()) }
class Showdown(): TurnAction() { override fun copy() = Showdown() }

class Board(val players: List<AIPlayer>, val minBet: Int = 100, initialWealth: Int = 10000) {
    var bets = players.map { 0 }.toMutableList()
    val wealth = players.map { initialWealth }.toMutableList()
    val actions = mutableListOf<TurnAction>()
    var dealer = 0
    val deck = mutableListOf<Card>()
    val communityCards = mutableListOf<Card>()
    val playerCards = players.map { mutableListOf<Card>() }.toMutableList()
    val foldedPlayers = mutableListOf<Int>()

    val pot: Int get() = bets.sum()

    fun playRound() {
        newRound()
        val smallBlind = currentPlayer()
        actions.add(Bet(smallBlind, min(minBet / 2, wealth[smallBlind])))
        val bigBlind = currentPlayer()
        actions.add(Bet(bigBlind, min(minBet, wealth[bigBlind])))

        while (!finished()) {

        }
    }

    private fun handlePlayerInput(input: PlayerAction) {

    }

    private fun currentPlayer(): Int {
        val lastToDoSomething = actions.filterIsInstance<PlayerAction>().last().playerNumber
        for (i in 1 until players.size) {

        }
    }

    private fun finished(): Boolean {
        return false
    }

    private fun newRound() {
        pot = 0
        actions.clear()
        dealer = (dealer + 1) % players.size
        dealCards()
    }

    private fun dealCards() {
        communityCards.clear()
        deck.clear()
        deck.addAll(CardSuit.values().flatMap { suit -> (0..12).map { rank -> Card(suit, CardRank(rank)) } })
        deck.shuffle()
        playerCards.forEach {
            it.clear()
            it.add(deck.removeAt(0))
        }
    }

    fun copyFor(player: AIPlayer): Board {
        val index = players.indexOf(player)

        return Board(players.map { DummyPlayer() }, minBet).also {
            it.pot = pot
            it.wealth.clear()
            it.wealth.addAll(wealth)
            it.actions.addAll(actions.map { action -> action.copy() })
            it.dealer = dealer
            it.communityCards.addAll(communityCards)
            val cards = playerCards[index].map { c -> c.copy() }.toMutableList()
            it.playerCards.clear()
            it.playerCards.add(cards)
            it.foldedPlayers.addAll(foldedPlayers)
        }
    }
}
