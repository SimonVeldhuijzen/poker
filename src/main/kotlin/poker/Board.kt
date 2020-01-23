package poker

import poker.players.HumanPlayer
import kotlin.math.max
import kotlin.math.min

class Board(val players: List<Player>, val minBet: Int = 100) {

    val actions = mutableListOf<TurnAction>()
    val deck = mutableListOf<Card>()
    val communityCards = mutableListOf<Card>()
    var dealer = players[0]
    var currentBet = 0
    var smallBlind = nextPlayer(dealer)
    var bigBlind = nextPlayer(smallBlind)
    var currentPlayer = nextPlayer(bigBlind)
    var isFinished = false

    fun playRound() {
        initializeRound()

        while (!isFinished) {
            val player = copyPlayer(currentPlayer, true)
            val board = copyBoard()

            val move = try {
                player.move(board)
            } catch(_: Exception) {
                Fold(currentPlayer)
            }

            handlePlayerInput(move)
        }
    }

    private fun handlePlayerInput(input: PlayerAction) {
        val result = when (input) {
            is SmallBlind -> smallBlind()
            is BigBlind -> bigBlind()
            is Check -> check()
            is Fold -> fold()
            is Call -> call()
            is Raise -> raise(input.amount)
        }

        actions.add(result)
        currentPlayer.lastAction = result::class

        currentPlayer = nextPlayer(currentPlayer)
        checkTransition()
    }

    private fun smallBlind(): PlayerAction {
        val amount = min(minBet / 2, smallBlind.wealth)
        smallBlind.wealth -= amount
        smallBlind.betThisRound += amount
        currentBet = amount
        return SmallBlind(smallBlind, amount)
    }

    private fun bigBlind(): PlayerAction {
        val amount = min(minBet, smallBlind.wealth)
        bigBlind.wealth -= amount
        bigBlind.betThisRound += amount
        currentBet = amount
        return BigBlind(bigBlind, amount)
    }

    private fun check(): PlayerAction {
        return if (currentPlayer.betThisRound == currentBet) {
            Check(currentPlayer)
        } else {
            Fold(currentPlayer)
        }
    }

    private fun fold(): PlayerAction {
        return Fold(currentPlayer)
    }

    private fun call(): PlayerAction {
        val amount = min(currentPlayer.wealth, currentBet - currentPlayer.betThisRound)
        return if (amount == 0) {
            Check(currentPlayer)
        } else {
            currentPlayer.wealth -= amount
            currentPlayer.betThisRound += amount
            Call(currentPlayer)
        }
    }

    private fun raise(amount: Int): PlayerAction {
        val actualAmount = min(currentPlayer.wealth, amount + currentBet - currentPlayer.betThisRound)
        if (actualAmount != currentPlayer.wealth && actualAmount < minBet) {
            return Fold(currentPlayer)
        }

        currentPlayer.wealth -= actualAmount
        currentPlayer.betThisRound += actualAmount
        currentBet += actualAmount

        return Raise(currentPlayer, actualAmount)
    }

    private fun checkTransition() {
        val activePlayers = players.filter { it.isActive }
        if (activePlayers.size == 1) {
            isFinished = true
            activePlayers[0].wealth += players.sumBy { it.betThisRound + it.betTotal }
        } else if (activePlayers.all { it.lastAction == Check::class || it.lastAction == Call::class }) {
            goToNextPhase()
        } else if (activePlayers.count { it.lastAction == Raise::class } == 1 && currentPlayer.lastAction == Raise::class) {
            goToNextPhase()
        }
    }

    private fun goToNextPhase() {
        when {
            actions.filterIsInstance<River>().any() -> handleShowdown()
            actions.filterIsInstance<Turn>().any() -> actions.add(River(deck.removeAt(0)))
            actions.filterIsInstance<Flop>().any() -> actions.add(Turn(deck.removeAt(0)))
            else -> actions.add(Flop(deck.removeAt(0), deck.removeAt(0), deck.removeAt(0)))
        }

        currentPlayer = nextPlayer(bigBlind)
    }

    private fun handleShowdown() {
        actions.add(Showdown())
    }

    private fun initializeRound() {
        actions.clear()

        dealer = nextPlayer(dealer)
        smallBlind = nextPlayer(dealer)
        bigBlind = nextPlayer(smallBlind)
        currentPlayer = nextPlayer(bigBlind)

        communityCards.clear()
        deck.clear()
        deck.addAll(CardSuit.values().flatMap { suit -> (0..12).map { rank -> Card(suit, CardRank(rank)) } })
        deck.shuffle()
        players.forEach {
            it.cards.clear()
            it.cards.add(deck.removeAt(0))
            it.cards.add(deck.removeAt(0))
            it.betTotal = 0
            it.betThisRound = 0
            it.lastAction = null
        }

        handlePlayerInput(SmallBlind(smallBlind, minBet / 2))
        handlePlayerInput(BigBlind(bigBlind, minBet))
        currentBet = max(smallBlind.betThisRound, bigBlind.betThisRound)
    }

    private fun nextPlayer(player: Player): Player {
        val currentIndex = players.indexOf(player)
        for (i in players.indices) {
            val index = (i + currentIndex + 1) % players.size
            if (players[index].isActive) {
                return players[index]
            }
        }

        throw Exception("Should not be reachable")
    }

    private fun copyBoard(): Board {
        return Board(players.map { copyPlayer(it, it == currentPlayer) }, minBet).also {
            it.actions.clear()
            it.actions.addAll(actions.map { a -> a.copy() })
            it.deck.clear()
            it.communityCards.clear()
            it.communityCards.addAll(communityCards.map { c -> c.copy() })
            it.dealer = copyPlayer(dealer, dealer == currentPlayer)
            it.currentBet = currentBet
            it.smallBlind = copyPlayer(smallBlind, smallBlind == currentPlayer)
            it.bigBlind = copyPlayer(bigBlind, bigBlind == currentPlayer)
            it.currentPlayer = copyPlayer(currentPlayer, false)
            it.isFinished = isFinished
        }
    }

    companion object {
        fun copyPlayer(player: Player, showCardsAndAi: Boolean): Player {
            return if (showCardsAndAi) {
                player.copy(
                    cards = player.cards.map { it.copy() }.toMutableList(),
                    ai = player.ai
                )
            } else {
                player.copy(
                    cards = mutableListOf(),
                    ai = HumanPlayer()
                )
            }
        }
    }
}
