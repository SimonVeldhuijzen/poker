//package poker
//
//sealed class PokerHand(val handRank: Int, rankCount: Int, vararg cardRanks: Int) : Comparable<PokerHand> {
//    val ranks = cardRanks.copyOf(rankCount)
//
//    override fun compareTo(other: PokerHand): Int {
//        if (handRank != other.handRank) return handRank - other.handRank
//        ranks.forEachIndexed { i, r -> if (other.ranks[i] != r) return r - other.ranks[i]}
//        return 0
//    }
//}
//
//class HighCard(vararg ranks: Int) : PokerHand(0, 5, *ranks)
//class Pair(vararg ranks: Int) : PokerHand(1, 4, *ranks)
//class TwoPair(vararg ranks: Int) : PokerHand(2, 3, *ranks)
//class ThreeOfAKind(vararg ranks: Int) : PokerHand(3, 3, *ranks)
//class Straight(rank: Int) : PokerHand(4, 1, rank)
//class Flush(vararg ranks: Int) : PokerHand(5, 5, *ranks)
//class FullHouse(vararg ranks: Int) : PokerHand(6, 2, *ranks)
//class FourOfAKind(vararg ranks: Int) : PokerHand(7, 2, *ranks)
//class StraightFlush(rank: Int) : PokerHand(8, 1, rank)
//object ScoreLogic {
//    fun applyShowDown(board: Board): List<kotlin.Pair<Player, PokerHand>> {
//        val cCards = board.communityCards
//        val pCards = board.players.map { it to it.cards }.toMap()
//        val bets = board.players.map { it to it.betTotal }.toMap()
//
//        return pCards.map { it.key to it.value + cCards }.toMap().map { cs ->
//            val flush = CardSuit.values().map { s -> cs.value.filter { c -> c.suit == s } }.find { it.size >= 5 }
//            val straightRanker = { it: List<Card> ->
//                run {
//                    var size = 1
//                    it.map { it.rank.rank }.distinct().sortedDescending().reduce { a, b ->
//                        if (a - b == 1 || a == 2 && b == 14) {
//                            size++
//                            if (size == 5) {
//                                return@run a + 3
//                            }
//                        } else {
//                            size = 1
//                        }
//                        b
//                    }
//                    -1
//                }
//            }
//
//            val isFlush = flush != null
//            if (isFlush) {
//                val straightFlushRank = straightRanker(flush!!)
//                if (straightFlushRank != -1) return@map cs.key to StraightFlush(straightFlushRank)
//            }
//            val cardsByOccurrences = cs.value.groupingBy { it.rank.rank }.eachCount().toList().sortedByDescending { it.second * 100 + it.first }
//            val ranks = cardsByOccurrences.map { it.first }.toIntArray()
//            val occurences = cardsByOccurrences.map { it.second }.toIntArray()
//            if (occurences[0] == 4) return@map cs.key to FourOfAKind(ranks[0], ranks.copyOfRange(1, ranks.size).max()!!)
//            if (occurences[0] == 3 && ranks[1] >= 2) return@map cs.key to FullHouse(*ranks)
//            if (isFlush) return@map cs.key to Flush(*flush!!.map { it.rank.rank }.sortedDescending().toIntArray())
//            val straightStrength = straightRanker(cs.value)
//            val isStraigth = straightStrength != -1
//            if (isStraigth) return@map cs.key to Straight(straightStrength)
//            if (occurences[0] == 3) return@map cs.key to ThreeOfAKind(ranks[0], *ranks.copyOfRange(1, ranks.size))
//            if (occurences[0] == 2 && occurences[1] == 2) return@map cs.key to TwoPair(ranks[0], ranks[1], *ranks.copyOfRange(2, ranks.size))
//            if (occurences[0] == 2) return@map cs.key to Pair(ranks[0], *ranks.copyOfRange(1, ranks.size))
//            return@map cs.key to HighCard(*ranks)
//        }
//    }
//}
