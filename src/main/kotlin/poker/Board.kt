package poker

import org.jetbrains.kotlin.psi.valueArgumentListVisitor
import poker.players.HumanPlayer
import kotlin.contracts.CallsInPlace
import kotlin.math.max
import kotlin.math.min

class Board(val players: List<Player>, val minBet: Int = 100) {

    val actions = mutableListOf<TurnAction>()
    val deck = mutableListOf<Card>()
    val communityCards = mutableListOf<Card>()

    val activePlayers = players.map { it }.toMutableList()
    val foldedPlayers = mutableListOf<Player>()
    val allInPlayers = mutableListOf<Player>()
    val bankruptPlayers = mutableListOf<Player>()

    var dealer = players.last()
    var currentBet = 0
    var currentPlayer = nextPlayer(dealer)
    var isFinished = false

    override fun toString() = "Community cards: $communityCards; currentBet: $currentBet"

    fun playGame() {
        while((players.filter { it.wealth > 0 }).size > 1) {
            playRound()
        }
    }

    fun playRound() {
        println()
        println("new round")
        initializeRound()

        while (!isFinished) {
            if (needsActionFromPlayer()) {
                val player = copyPlayer(currentPlayer, true)
                val board = copyBoard()

//            println("Player to move: ${player.name} (wealth: ${player.wealth}; betted this round: ${player.betThisRound}; betted this hand: ${player.betTotal}")

                val move = try {
                    player.move(board)
                } catch(_: Exception) {
                    Fold(currentPlayer)
                }

                handlePlayerInput(move)
            }

            println("Current total: ${players.sumBy { it.wealth + it.betThisRound + it.betTotal }}; active players: ${activePlayers.size}")
            checkTransition()
        }
    }

    private fun needsActionFromPlayer(): Boolean {
        if (activePlayers.size > 1) {
            return true
        }

        if (activePlayers.size == 0) {
            return false
        }

        if (actions.last() is BoardAction) {
            return false
        }

        return currentPlayer.betThisRound != currentBet
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
    }

    private fun smallBlind(): PlayerAction {
        if (actions.size > 0) {
            return fold()
        }

        val amount = min(minBet / 2, currentPlayer.wealth)
        if (amount == currentPlayer.wealth) {
            return allIn()
        }

        println("Small blind of $amount by ${currentPlayer.name}")
        currentPlayer.wealth -= amount
        currentPlayer.betThisRound += amount
        currentBet = amount

        return SmallBlind(currentPlayer, amount)
    }

    private fun bigBlind(): PlayerAction {
        if (actions.size != 1) {
            return fold()
        }

        val amount = min(minBet, currentPlayer.wealth)
        if (amount == currentPlayer.wealth) {
            return allIn()
        }

        println("Big blind of $amount by ${currentPlayer.name}")
        currentPlayer.wealth -= amount
        currentPlayer.betThisRound += amount
        currentBet = amount

        return BigBlind(currentPlayer, amount)
    }

    private fun check(): PlayerAction {
        if (currentPlayer.betThisRound == currentBet) {
            println("Check by ${currentPlayer.name}")
            return Check(currentPlayer)
        } else {
            return fold()
        }
    }

    private fun fold(): PlayerAction {
        activePlayers.remove(currentPlayer)
        foldedPlayers.add(currentPlayer)

        println("Fold by ${currentPlayer.name}")
        return Fold(currentPlayer)
    }

    private fun call(): PlayerAction {
        val toCall = currentBet - currentPlayer.betThisRound
        if (toCall >= currentPlayer.wealth) {
            return allIn()
        } else if (toCall == 0) {
            return check()
        } else {
            currentPlayer.wealth -= toCall
            currentPlayer.betThisRound += toCall
            println("Call of $toCall by ${currentPlayer.name}")

            if (!isValid()) {
                println()
            }

            return Call(currentPlayer, toCall)
        }
    }

    private fun raise(amount: Int): PlayerAction {
        if (currentPlayer.wealth <= amount) {
            return allIn()
        } else if (amount < minBet) {
            return fold()
        } else {
            currentPlayer.wealth -= amount
            currentPlayer.betThisRound += amount
            currentBet += amount

            if (!isValid()) {
                println()
            }
            println("Raise of $amount by ${currentPlayer.name}")
            return Raise(currentPlayer, amount)
        }
    }

    private fun allIn(): PlayerAction {
        val adding = max(0, currentPlayer.wealth - (currentBet - currentPlayer.betThisRound))
        if (adding > 0) {
            currentBet += adding
            println("All in by raising with $adding by ${currentPlayer.name}")
        } else {
            println("All in by ${currentPlayer.name}")
        }

        currentPlayer.betThisRound += currentPlayer.wealth
        currentPlayer.wealth = 0
        activePlayers.remove(currentPlayer)
        allInPlayers.add(currentPlayer)

        if (!isValid()) {
            println()
        }

        return AllIn(currentPlayer, adding)
    }

    private fun isEndOfBettingRound(): Boolean {
        if (activePlayers.size < 2) {
            return true
        } else if (activePlayers.all { it.lastAction == Check::class || it.lastAction == Call::class }) {
            return true
        } else {
            val raisers = activePlayers.filter { it.lastAction == Raise::class }
            if (raisers.size == 1) {
                return raisers[0] == currentPlayer
            } else {
                return false
            }
        }
    }

    private fun isEndOfRoundWithoutShowdown(): Boolean {
        return activePlayers.size < 2 && allInPlayers.size == 0
    }

    private fun checkTransition() {
        if (isEndOfRoundWithoutShowdown()) {
            endWithoutShowdown()
        } else if (isEndOfBettingRound()) {
            goToNextPhase()
        }
    }

    private fun endWithoutShowdown() {
        println("Current total: ${players.sumBy { it.wealth + it.betThisRound + it.betTotal }}; active players: ${activePlayers.size}")
        val winner = activePlayers[0]
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
        }

        winner.wealth += players.map { it.betTotal }.sum()
        println("Current total: ${players.sumBy { it.wealth }}; active players: ${activePlayers.size}")
        if (players.sumBy { it.wealth } != 40000) {
            println()
        }
        isFinished = true
    }

    private fun goToNextPhase() {
        if (communityCards.size == 0) {
            println("Flop")
            val flop = Flop(deck.removeAt(0), deck.removeAt(0), deck.removeAt(0))
            actions.add(flop)
            communityCards.add(flop.first)
            communityCards.add(flop.second)
            communityCards.add(flop.third)
        } else if (communityCards.size == 3) {
            println("Turn")
            val turn = Turn(deck.removeAt(0))
            actions.add(turn)
            communityCards.add(turn.fourth)
        } else if (communityCards.size == 4) {
            println("River")
            val river = River(deck.removeAt(0))
            actions.add(river)
            communityCards.add(river.fifth)
        } else {
            println("Showdown")
            val playerIterator = players.iterator()
            while (playerIterator.hasNext()) {
                val value = playerIterator.next()
                println(value.name + " has a " + rankHand(value.cards + communityCards))
            }
            println()
            return handleShowdown()
        }

        currentBet = 0
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
            it.lastAction = null
        }

        println("Current total: ${players.sumBy { it.wealth + it.betThisRound + it.betTotal }}; active players: ${activePlayers.size}")

        if (!isValid()) {
            println()
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
            val playersWithHands = results.filter { it.key.betTotal != 0 && it.key !in foldedPlayers }
            val highest = playersWithHands.maxBy { it.value }
            val winners = playersWithHands.filter { it.value.compareTo(highest!!.value) == 0 }
            val pot = actualAmount * playersWithHands.size / winners.size
            winners.forEach {
                it.key.wealth += pot
            }

            playersWithHands.forEach {
                it.key.betTotal -= actualAmount
            }

            previous = amount
        }

        actions.add(Showdown())
        isFinished = true
    }

    private fun initializeRound() {
        actions.clear()
        isFinished = false
        currentBet = 0

        activePlayers.clear()
        activePlayers.addAll(players.filter { it.wealth > 0 })

        foldedPlayers.clear()
        allInPlayers.clear()
        bankruptPlayers.clear()
        bankruptPlayers.addAll(players.filter { it.wealth <= 0 })

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

        dealer = nextPlayer(dealer)

        if (!isValid()) {
            println()
        }

        currentPlayer = nextPlayer(dealer)
        handlePlayerInput(SmallBlind(currentPlayer, minBet / 2))
        handlePlayerInput(BigBlind(currentPlayer, minBet))
    }

    private fun nextPlayer(player: Player): Player {
        if (activePlayers.size == 0) {
            return player
        }
        return activePlayers.firstOrNull { it.id > player.id } ?: activePlayers.first()
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
            it.currentPlayer = copyPlayer(currentPlayer, false)
            it.isFinished = isFinished

            it.activePlayers.clear()
            it.foldedPlayers.clear()
            it.allInPlayers.clear()
            it.bankruptPlayers.clear()

            it.activePlayers.addAll(activePlayers.map { p ->  copyPlayer(p, p == currentPlayer) })
            it.foldedPlayers.addAll(foldedPlayers.map { p ->  copyPlayer(p, p == currentPlayer) })
            it.allInPlayers.addAll(allInPlayers.map { p ->  copyPlayer(p, p == currentPlayer) })
            it.bankruptPlayers.addAll(bankruptPlayers.map { p ->  copyPlayer(p, p == currentPlayer) })
        }
    }

    private fun isValid(): Boolean {
        return players.sumBy { it.wealth + it.betTotal + it.betThisRound } == 40000
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
