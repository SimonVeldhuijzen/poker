package poker

import poker.players.HumanPlayer
import kotlin.math.max
import kotlin.math.min

class Board(val players: List<Player>, val minBet: Int = 100) {

    val actions = mutableListOf<TurnAction>()
    val deck = mutableListOf<Card>()
    val communityCards = mutableListOf<Card>()
    var dealer = players.last()
    var currentBet = 0
    var smallBlind = nextPlayer(dealer)
    var bigBlind = nextPlayer(smallBlind)
    var currentPlayer = nextPlayer(bigBlind)
    var isFinished = false
    var checks = players.size

    override fun toString() = "Community cards: $communityCards; currentBet: $currentBet"

    fun playGame() {
        while((players.filter { it.isActive }).size > 1) {
            playRound()
        }
    }

    fun playRound() {
        initializeRound()

        while (!isFinished) {
            val player = copyPlayer(currentPlayer, true)
            val board = copyBoard()

            println("Player to play: $player")
            println("Board: $board")


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
            is AllIn -> allIn()
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
        checks--
        return if (currentPlayer.betThisRound == currentBet) {
            Check(currentPlayer)
        } else {
            Fold(currentPlayer)
        }
    }

    private fun fold(): PlayerAction {
        checks--
        return Fold(currentPlayer)
    }

    private fun call(): PlayerAction {
        checks--
        val amount = min(currentPlayer.wealth, currentBet - currentPlayer.betThisRound)
        return if (amount == 0) {
            Check(currentPlayer)
        } else {
            currentPlayer.wealth -= amount
            currentPlayer.betThisRound += amount
            Call(currentPlayer, amount)
        }
    }

    private fun raise(amount: Int): PlayerAction {
        call()
        val actualAmount = min(currentPlayer.wealth, amount)
        if (actualAmount == currentPlayer.wealth) {
            return allIn()
        }

        if (actualAmount < minBet) {
            return Fold(currentPlayer)
        }

        currentPlayer.wealth -= actualAmount
        currentPlayer.betThisRound += actualAmount
        currentBet += actualAmount

        checks = players.count { it.isActive && it.wealth > 0 } - 1
        return Raise(currentPlayer, actualAmount)
    }

    private fun allIn(): PlayerAction {
        val call = call()
        val amount = currentPlayer.wealth
        if (amount == 0) {
            return call
        }

        currentPlayer.wealth -= amount
        currentPlayer.betThisRound += amount
        currentBet += amount

        checks = players.count { it.isActive && it.wealth > 0 }
        return AllIn(currentPlayer, amount)
    }

    private fun checkTransition() {
        val activePlayers = players.filter { it.isActive }
        if (activePlayers.size == 1) {
            isFinished = true
            activePlayers[0].wealth += players.sumBy { it.betThisRound + it.betTotal }
        } else if (checks == 0) {
            goToNextPhase()
        }
    }

    private fun goToNextPhase() {
        currentPlayer = nextPlayer(bigBlind)
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
            it.lastAction = if (it.lastAction == Fold::class) Fold::class else null
        }

        currentBet = 0
        checks = players.count { it.isActive && it.wealth > 0 }

        when {
            actions.filterIsInstance<River>().any() -> {
                println("Showdown")
                handleShowdown()
            }
            actions.filterIsInstance<Turn>().any() -> {
                val river = River(deck.removeAt(0))
                actions.add(river)
                communityCards.add(river.fifth)
                println("River")
            }
            actions.filterIsInstance<Flop>().any() -> {
                val turn = Turn(deck.removeAt(0))
                actions.add(turn)
                communityCards.add(turn.fourth)
                println("Turn")
            }
            else -> {
                val flop = Flop(deck.removeAt(0), deck.removeAt(0), deck.removeAt(0))
                actions.add(flop)
                communityCards.add(flop.first)
                communityCards.add(flop.second)
                communityCards.add(flop.third)
                println("Flop")
            }
        }
    }

    private fun handleShowdown() {
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
        }
        val results = applyShowDown(this).toMap()

        val amounts = results.map { it.key.betTotal }.distinct().sorted() - listOf(0)
        var previous = 0
        for (amount in amounts) {
            val actualAmount = amount - previous
            val players = results.filter { it.key.betTotal != 0 }
            val highest = players.filter { it.key.isActive }.maxBy { it.value }
            val winners = players.filter { it.key.isActive && it.value.compareTo(highest!!.value) == 0 }
            val pot = actualAmount * players.size / winners.size
            winners.forEach {
                it.key.wealth += pot
            }

            players.forEach {
                it.key.betTotal -= actualAmount
            }

            previous = amount
        }

        actions.add(Showdown())
        isFinished = true
    }

    private fun initializeRound() {
        actions.clear()

        dealer = nextPlayer(dealer)
        smallBlind = nextPlayer(dealer)
        bigBlind = nextPlayer(smallBlind)

        communityCards.clear()
        deck.clear()
        deck.addAll(CardSuit.values().flatMap { suit -> (2..14).map { rank -> Card(suit, CardRank(rank)) } })
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
        currentPlayer = nextPlayer(bigBlind)
    }

    private fun nextPlayer(player: Player): Player {
        val currentIndex = players.indexOf(player)
        for (i in players.indices) {
            val index = (i + currentIndex + 1) % players.size
            if (players[index].isActive && players[index].wealth > 0) {
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
