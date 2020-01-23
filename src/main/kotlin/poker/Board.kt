package poker

import kotlin.math.min

enum class CardSuit { HEARTS, DIAMONDS, SPADES, CLUBS }
// 2-10 -> 2-10, JQKA -> 11-14
data class CardRank(val rank: Int)
data class Card(val suit: CardSuit, val rank: CardRank)

sealed class TurnAction { abstract fun copy(): TurnAction }
sealed class PlayerAction : TurnAction() { abstract val playerNumber: Int }
class Check(override val playerNumber: Int): PlayerAction() { override fun copy() = Check(playerNumber) }
class Fold(override val playerNumber: Int): PlayerAction() { override fun copy() = Fold(playerNumber) }
class Call(override val playerNumber: Int): PlayerAction() { override fun copy() = Call(playerNumber) }
class Raise(override val playerNumber: Int, val amount: Int): PlayerAction() { override fun copy() = Raise(playerNumber, amount) }
class Flop(val first: Card, val second: Card, val third: Card): TurnAction() { override fun copy() = Flop( first.copy(), second.copy(), third.copy()) }
class Turn(val fourth: Card): TurnAction() { override fun copy() = Turn(fourth.copy()) }
class River(val fifth: Card): TurnAction() { override fun copy() = River(fifth.copy()) }
class Showdown: TurnAction() { override fun copy() = Showdown() }

class Board(val players: List<AIPlayer>, val minBet: Int = 100, initialWealth: Int = 10000) {

    val wealth = players.mapIndexed { i, _ -> i to initialWealth }.toMap().toMutableMap()
    val bankruptPlayers = mutableListOf<Int>()

    val bets = players.mapIndexed { i, _ -> i to 0 }.toMap().toMutableMap()
    val betsThisRound = players.mapIndexed { i, _ -> i to 0 }.toMap().toMutableMap()
    val actions = mutableListOf<TurnAction>()
    val deck = mutableListOf<Card>()
    val communityCards = mutableListOf<Card>()
    val playerCards = players.mapIndexed { i, _ -> i to mutableListOf<Card>() }.toMap().toMutableMap()
    val activePlayers  = mutableListOf<Int>()
    var dealer = -1
    var checksOrCalls = 0
    var currentBet = 0
    var currentPlayer: Int? = null
    var smallBlind = -1
    var bigBlind = -1
    var firstToPlay = -1

    val pot: Int get() = bets.values.sum()

    fun playRound() {
        newRound()
        handlePlayerInput(Raise(currentPlayer!!, minBet / 2))
        handlePlayerInput(Raise(currentPlayer!!, minBet / 2))
        currentBet = betsThisRound.values.max()!!
        checksOrCalls = 0

        while (!finished()) {
            if (currentPlayer == null) {
                println("transition")
                handleTransition()
            } else {
                println("current player: $currentPlayer; current bet: $currentBet; current bet of player: ${betsThisRound[currentPlayer!!]}; checks and calls: $checksOrCalls")
                val move = try {
                    players[currentPlayer!!].move(copyFor(currentPlayer!!))
                } catch(_: Exception) {
                    Fold(currentPlayer!!)
                }

                handlePlayerInput(move)
            }
        }
    }

    private fun handlePlayerInput(input: PlayerAction) {
        val result = when (input) {
            is Check -> check()
            is Fold -> fold()
            is Call -> call()
            is Raise -> raise(input.amount, actions.size < 2)
        }

        actions.add(result)
        if (result is Fold) {
            activePlayers.remove(currentPlayer!!)
        }
        setCurrentPlayer()
    }

    private fun check(): TurnAction {
        return if (betsThisRound[currentPlayer!!] == currentBet) {
            checksOrCalls++
            Check(currentPlayer!!)
        } else {
            Fold(currentPlayer!!)
        }
    }

    private fun fold(): TurnAction {
        return Fold(currentPlayer!!)
    }

    private fun call(): TurnAction {
        val amount = min(wealth[currentPlayer!!]!!, currentBet - betsThisRound[currentPlayer!!]!!)
        checksOrCalls++
        return if (amount == 0) {
            Check(currentPlayer!!)
        } else {
            wealth[currentPlayer!!] = wealth[currentPlayer!!]!! - amount
            betsThisRound[currentPlayer!!] = betsThisRound[currentPlayer!!]!! + amount
            Call(currentPlayer!!)
        }
    }

    private fun raise(amount: Int, isBlind: Boolean): TurnAction {
        val actualAmount = min(wealth[currentPlayer!!]!!, amount + currentBet - betsThisRound[currentPlayer!!]!!)
        if (!isBlind && actualAmount != wealth[currentPlayer!!]!! && actualAmount < minBet) {
            return Fold(currentPlayer!!)
        }

        wealth[currentPlayer!!] = wealth[currentPlayer!!]!! - actualAmount
        betsThisRound[currentPlayer!!] = betsThisRound[currentPlayer!!]!! + actualAmount
        currentBet += actualAmount
        checksOrCalls = 1

        return Raise(currentPlayer!!, actualAmount)
    }

    private fun handleTransition() {
        currentBet = 0
        checksOrCalls = 0
        for (i in players.indices - bankruptPlayers) {
            bets[i] = bets[i]!! + betsThisRound[i]!!
            betsThisRound[i] = 0
        }
        when {
            actions.filterIsInstance<River>().any() -> handleShowdown()
            actions.filterIsInstance<Turn>().any() -> actions.add(River(deck.removeAt(0)))
            actions.filterIsInstance<Flop>().any() -> actions.add(Turn(deck.removeAt(0)))
            else -> actions.add(Flop(deck.removeAt(0), deck.removeAt(0), deck.removeAt(0)))
        }

        setCurrentPlayer()
    }

    private fun handleShowdown() {
        actions.add(Showdown())
    }

    private fun setCurrentPlayer() {
        if (activePlayers.size < 2 || checksOrCalls == activePlayers.size) {
            currentPlayer = null
        } else if (actions.isEmpty()) {
            currentPlayer = nextPlayer(smallBlind - 1)
        } else if (currentPlayer == null) {
            currentPlayer = nextPlayer(firstToPlay - 1)
        } else {
            currentPlayer = nextPlayer(currentPlayer!!)
        }
    }

    private fun finished(): Boolean {
        return actions.any() && actions.last() is Showdown
    }

    private fun newRound() {
        activePlayers.clear()
        activePlayers.addAll(players.indices - bankruptPlayers)
        bets.clear()
        activePlayers.forEach {
            bets[it] = 0
        }
        actions.clear()
        dealer = nextPlayer(dealer)
        smallBlind = nextPlayer(dealer)
        bigBlind = nextPlayer(smallBlind)
        firstToPlay = nextPlayer(bigBlind)
        currentBet = 0
        checksOrCalls = 0
        dealCards()
        setCurrentPlayer()
    }

    private fun dealCards() {
        communityCards.clear()
        deck.clear()
        deck.addAll(CardSuit.values().flatMap { suit -> (0..12).map { rank -> Card(suit, CardRank(rank)) } })
        deck.shuffle()
        playerCards.clear()
        activePlayers.forEach {
            playerCards[it] = mutableListOf( deck.removeAt(0), deck.removeAt(0))
        }
    }

    private fun nextPlayer(index: Int): Int {
        return activePlayers.firstOrNull { it > index } ?: activePlayers.first()
    }

    fun copyFor(index: Int): Board {
        return Board(players.map { DummyPlayer() }, minBet).also {
            it.wealth.clear()
            for (i in wealth) {
                it.wealth[i.key] = i.value
            }

            it.bankruptPlayers.addAll(bankruptPlayers)

            it.bets.clear()
            for (i in bets) {
                it.bets[i.key] = i.value
            }

            it.betsThisRound.clear()
            for (i in betsThisRound) {
                it.betsThisRound[i.key] = i.value
            }

            it.actions.addAll(actions.map { action -> action.copy() })
            it.communityCards.addAll(communityCards.map { c -> c.copy() })
            val cards = playerCards[index]!!.map { c -> c.copy() }.toMutableList()
            it.playerCards.clear()
            it.playerCards[index] = cards
            it.activePlayers.addAll(activePlayers)
            it.dealer = dealer
            it.checksOrCalls = checksOrCalls
            it.currentBet = currentBet
            it.currentPlayer = currentPlayer
            it.smallBlind = smallBlind
            it.bigBlind = bigBlind
            it.firstToPlay = firstToPlay
        }
    }
}
