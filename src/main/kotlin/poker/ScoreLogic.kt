package poker

sealed class PokerHand(val handRank: Int, vararg val deciders: Int) : Comparable<PokerHand> {
    override fun compareTo(other: PokerHand): Int {
        if (handRank != other.handRank) return handRank - other.handRank
        deciders.forEachIndexed { i, r -> if (other.deciders[i] != r) return r - other.deciders[i]}
        return 0
    }
}

class HighCard(vararg deciders: Int) : PokerHand(0, *deciders)
class Pair(vararg deciders: Int) : PokerHand(1, *deciders)
class TwoPair(vararg deciders: Int) : PokerHand(2, *deciders)
class ThreeOfAKind(vararg deciders: Int) : PokerHand(3, *deciders)
class Straight(decider: Int) : PokerHand(4, decider)
class Flush(vararg deciders: Int) : PokerHand(5, *deciders)
class FullHouse(vararg deciders: Int) : PokerHand(6, *deciders)
class FourOfAKind(vararg deciders: Int) : PokerHand(7, *deciders)
class StraightFlush(decider: Int) : PokerHand(8, decider)

fun applyShowDown(board: Board): List<kotlin.Pair<Player, PokerHand>> {
    return board.players.map { it to rankHand(it.cards + board.communityCards) }
}

fun rankHand(cards: List<Card>): PokerHand {
    val sortedByRanks = cards.sortedByDescending { it.rank.rank }
    val straight = straight(sortedByRanks)
    if (straight != null) {
        val straightFlush = straightFlush(sortedByRanks, straight.deciders[0])
        if (straightFlush != null) {
            return straightFlush
        }
    }

    val fourOfAKind = fourOfAKind(sortedByRanks)
    if (fourOfAKind != null) {
        return fourOfAKind
    }

    val fullHouse = fullHouse(sortedByRanks)
    if (fullHouse != null) {
        return fullHouse
    }

    val flush = flush(sortedByRanks)
    if (flush != null) {
        return flush
    }

    if (straight != null) {
        return straight
    }

    val threeOfAKind = threeOfAKind(sortedByRanks)
    if (threeOfAKind != null) {
        return threeOfAKind
    }

    val twoPair = twoPair(sortedByRanks)
    if (twoPair != null) {
        return twoPair
    }

    val pair = pair(sortedByRanks)
    if (pair != null) {
        return pair
    }

    return highCard(sortedByRanks)
}

fun straight(cards: List<Card>): Straight? {
    for (i in 0 until cards.size-4) {
        if (cards.drop(i).take(5).map { it.rank.rank }.distinct().size == 5 && cards[i].rank.rank - cards[i+4].rank.rank == 4) {
            return Straight(cards[i].rank.rank)
        }
    }

    if (listOf(2, 3, 4, 5, 14).all { it in cards.map { c -> c.rank.rank } }) {
        return Straight(5)
    }

    return null
}

fun straightFlush(cards: List<Card>, highest: Int): StraightFlush? {
    val values = if (highest == 5) {
        listOf(2,3,4,5,14)
    } else {
        ((highest-4)..highest)
    }

    val relevant = cards.filter { it.rank.rank in values }
    val biggestSuit = relevant.groupBy { it.suit }.maxBy { it.value.size }!!.key
    if (values.all { relevant.any { r -> r.rank.rank == it && r.suit == biggestSuit } }) {
        return StraightFlush(highest)
    }

    return null
}

fun fourOfAKind(cards: List<Card>): FourOfAKind? {
    val value = cards.groupBy { it.rank.rank }.toList().firstOrNull { it.second.size == 4 }?.second?.first()?.rank?.rank ?: return null
    val decider = cards.first { it.rank.rank != value }.rank.rank
    return FourOfAKind(value, decider)
}

fun fullHouse(cards: List<Card>): FullHouse? {
    val grouped = cards.groupBy { it.rank.rank }.toList()
    val triple = grouped.firstOrNull{ it.second.size == 3 } ?: return null
    val double = grouped.firstOrNull{ it.second.size == 2 } ?: return null
    return FullHouse(triple.second.first().rank.rank, double.second.first().rank.rank)
}

fun flush(cards: List<Card>): Flush? {
    val grouped = cards.groupBy { it.suit }.maxBy { it.value.size }!!
    if (grouped.value.size >= 5) {
        return Flush(*grouped.value.map { it.rank.rank }.sortedDescending().take(5).toIntArray())
    }

    return null
}

fun threeOfAKind(cards: List<Card>): ThreeOfAKind? {
    val value = cards.groupBy { it.rank.rank }.toList().firstOrNull { it.second.size == 3 }?.second?.first()?.rank?.rank ?: return null
    val decider = cards.filter { it.rank.rank != value }.take(2).map { it.rank.rank }.toIntArray()
    return ThreeOfAKind(value, *decider)
}

fun twoPair(cards: List<Card>): TwoPair? {
    val values = cards.groupBy { it.rank.rank }.toList().filter { it.second.size == 2 }.sortedByDescending { it.first }
    if (values.size >= 2) {
        val value1 = values[0].first
        val value2 = values[1].first
        val decider = cards.first { it.rank.rank !in listOf(value1, value2) }.rank.rank
        return TwoPair(value1, value2, decider)
    }

    return null
}

fun pair(cards: List<Card>): Pair? {
    val value = cards.groupBy { it.rank.rank }.toList().firstOrNull { it.second.size == 2 }?.second?.first()?.rank?.rank ?: return null
    val decider = cards.filter { it.rank.rank != value }.take(3).map { it.rank.rank }.toIntArray()
    return Pair(value, *decider)
}

fun highCard(cards: List<Card>): HighCard {
    return HighCard(*cards.map { it.rank.rank }.take(5).toIntArray())
}
