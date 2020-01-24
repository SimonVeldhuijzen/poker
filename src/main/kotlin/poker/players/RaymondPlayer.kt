package poker.players

import poker.*

class RaymondPlayer(override var name: String) : AIPlayer {

    override fun move(state: Board, player: Player): PlayerAction {
        val rankHand = rankHand(state.deck + player.cards)
        val action = when(rankHand) {
            is HighCard -> hightCard(state, player)
            is Pair-> pair(state, player)
            is TwoPair -> twoPair(state, player)
            is ThreeOfAKind -> threeOfAKind(state, player)
            is Straight -> straight(state, player)
            is Flush -> flush(state, player)
            is FullHouse -> fullHouse(state, player)
            is FourOfAKind -> fourOfAKind(state, player)
            is StraightFlush -> straightFlush(state, player)
        }
        return action
    }

    fun hightCard(state: Board, player: Player) : PlayerAction {
        return play(state, player, 100)
    }

    fun pair(state: Board, player: Player) : PlayerAction {
        return play(state, player, 90)
    }

    fun twoPair(state: Board, player: Player) : PlayerAction {
        return play(state, player, 8)
    }

    fun threeOfAKind(state: Board, player: Player) : PlayerAction {
        return play(state, player, 7)
    }

    fun straight(state: Board, player: Player) : PlayerAction {
        return play(state, player, 6)
    }

    fun flush(state: Board, player: Player) : PlayerAction {
        return play(state, player, 5)
    }

    fun fullHouse(state: Board, player: Player) : PlayerAction {
        return play(state, player, 4)
    }

    fun fourOfAKind(state: Board, player: Player) : PlayerAction {
        return play(state, player, 30)
    }

    fun straightFlush(state: Board, player: Player) : PlayerAction {
        return play(state, player, 20)
    }

    fun play(state: Board, player: Player, amountDiv: Int) : PlayerAction {
        when(state.communityCards.size) {
            1 -> return Call(player, 100)
            3,4 -> return Check(player)
            else -> return Call(player, player.wealth/amountDiv)
        }
    }

}

