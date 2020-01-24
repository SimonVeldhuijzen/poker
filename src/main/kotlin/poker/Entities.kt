package poker

import poker.Board.Companion.copyPlayer
import kotlin.reflect.KClass

interface AIPlayer {
    var name: String
    fun move(state: Board, player: Player): PlayerAction
}

data class Player(
    val id: Int,
    val name: String,
    var wealth: Int,
    var betTotal: Int,
    var betThisRound: Int,
    var lastAction: KClass<out PlayerAction>?,
    val cards: MutableList<Card>,
    val ai: AIPlayer
) {
    val isBankrupt get() = wealth <= 0 && betThisRound <= 0 && betTotal <= 0
    val hasFolded get() = lastAction == Fold::class
    val isActive get() = !isBankrupt && !hasFolded

    fun move(board: Board) = ai.move(board, this)
}

enum class CardSuit {
    HEARTS, DIAMONDS, SPADES, CLUBS;

    override fun toString() = when(this) {
        CLUBS -> "C"
        DIAMONDS -> "D"
        HEARTS -> "H"
        SPADES -> "S"
    }
}
// 2-10 -> 2-10, JQKA -> 11-14
data class CardRank(val rank: Int) {
    override fun toString() = when(this.rank) {
        in 2..10 -> this.rank.toString()
        11 -> "J"
        12 -> "Q"
        13 -> "K"
        14 -> "A"
        else -> error("INVALID CARD")
    }
}
data class Card(val suit: CardSuit, val rank: CardRank) {
    override fun toString() = "$suit$rank"
}

sealed class TurnAction { abstract fun copy() : TurnAction }
sealed class PlayerAction: TurnAction() { abstract val player: Player }

class Check(override val player: Player): PlayerAction() { override fun copy() = Check(copyPlayer(player, false)) }
class Fold(override val player: Player): PlayerAction() { override fun copy() = Fold(copyPlayer(player, false)) }
class Call(override val player: Player, val amount: Int): PlayerAction() { override fun copy() = Call(copyPlayer(player, false), amount) }
class Raise(override val player: Player, val amount: Int): PlayerAction() { override fun copy() = Raise(copyPlayer(player, false), amount) }
class AllIn(override val player: Player, val amount: Int): PlayerAction() { override fun copy() = AllIn(copyPlayer(player, false), amount) }
class SmallBlind(override val player: Player, val amount: Int): PlayerAction() { override fun copy() = SmallBlind(copyPlayer(player, false), amount) }
class BigBlind(override val player: Player, val amount: Int): PlayerAction() { override fun copy() = BigBlind(copyPlayer(player, false), amount) }
class Flop(val first: Card, val second: Card, val third: Card): TurnAction() { override fun copy() = Flop(first.copy(), second.copy(), third.copy()) }
class Turn(val fourth: Card): TurnAction() { override fun copy() = Turn(fourth.copy()) }
class River(val fifth: Card): TurnAction() { override fun copy() = River(fifth.copy()) }
class Showdown: TurnAction() { override fun copy() = Showdown() }
