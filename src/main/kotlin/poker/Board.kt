package poker

import poker.players.HumanPlayer
import kotlin.math.max
import kotlin.math.min

class Board(val players: MutableList<Player>, val minBet: Int = 100) {

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

        for (player in players) {
            println("${player.name}: ${player.wealth}")
        }

        readLine()

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
            println("ILLEGAL ACTION BY ${currentPlayer.name} (smallblind)")
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
            println("ILLEGAL ACTION BY ${currentPlayer.name} (bigblind)")
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

            return Call(currentPlayer)
        }
    }

    private fun raise(amount: Int): PlayerAction {
        call()
        if (currentPlayer.wealth <= amount) {
            return allIn()
        } else if (amount < minBet) {
            println("ILLEGAL ACTION BY ${currentPlayer.name} (amount too low)")
            return fold()
        } else {
            currentPlayer.wealth -= amount
            currentPlayer.betThisRound += amount
            currentBet += amount
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

        }

        return AllIn(currentPlayer, adding)
    }

    private fun isEndOfBettingRound(): Boolean {
        val maxBet = players.map { it.betThisRound }.max()!!
        return activePlayers.all { it.betThisRound == maxBet || it.wealth == 0}
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
        val winner = activePlayers[0]
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
        }

        print("Winner: ${winner.name} (${winner.wealth} ->")
        winner.wealth += players.map { it.betTotal }.sum()
        println("${winner.wealth})")
        if (players.sumBy { it.wealth } != 40000) {

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
            println("Showdown " + communityCards)
            val playerIterator = players.iterator()
            while (playerIterator.hasNext()) {
                val value = playerIterator.next()
                println(value.name + " has a " + rankHand(value.cards + communityCards) + " with: " + value.cards)
            }

            return handleShowdown()
        }

        currentBet = 0
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
            it.lastAction = null
        }


        if (!isValid()) {

        }
    }

    private fun handleShowdown() {
        players.forEach {
            it.betTotal += it.betThisRound
            it.betThisRound = 0
        }
        val results = applyShowDown(this).toMap()

        val playersInShowDown = (players - foldedPlayers).sortedByDescending { results[it] }.toMutableList()

        val highestBet = playersInShowDown.maxBy { it.betTotal }!!
        if (playersInShowDown.any { it.betTotal != highestBet.betTotal && it.wealth > 0 }) {
            error("not calling wtf")
        }

        var change = 0
        while(playersInShowDown.isNotEmpty()) {
            val bestHand = results[playersInShowDown[0]]!!
            val winners = playersInShowDown.filter { results[it]!!.compareTo(bestHand) == 0 }.sortedBy { it.betTotal }
            val lowestBet = winners[0]!!.betTotal
            val removeFromShowdown = winners.filter { it.betTotal == lowestBet }
            if (lowestBet > 0) {
                val moneySum = players.sumBy {
                    val money = min(it.betTotal, lowestBet)
                    it.betTotal -= money
                    money
                }
                change += moneySum
                val moneyPerGuy = moneySum / winners.size
                winners.forEach { it.wealth += moneyPerGuy }
                winners[0].wealth += moneySum - moneyPerGuy * winners.size
            }
            playersInShowDown.removeAll(removeFromShowdown)
        }

        if (players.any { it.betThisRound > 0 }) {
            error("Unreachable")
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
        players.removeAll(bankruptPlayers)

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
        return Board(players.map { copyPlayer(it, it == currentPlayer) }.toMutableList(), minBet).also {
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
                player.copy().also {
                    it.cards = player.cards.map { it.copy() }.toMutableList()
                    it.ai = player.ai
                }
            } else {
                player.copy().also {
                    it.cards = mutableListOf()
                    it.ai = HumanPlayer()
                }
            }
        }
    }
}
