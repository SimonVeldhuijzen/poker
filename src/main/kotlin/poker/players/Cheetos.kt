package poker.players

import poker.*
import kotlin.math.min
import kotlin.math.pow

class Cheetos(override var name: String = "Cheetos", var acceptableRisk: Float) : AIPlayer {
    private val totalHands          = 133784560
    private val highCardHands       = 22294460
    private val pairHands           = 58627800
    private val twoPairHands        = 31433400
    private val threeOfAKindHands   = 6461620
    private val straightHands       = 6180020
    private val flushHands          = 4047644
    private val fullHouseHands      = 3473184
    private val fourOfAKindHands    = 224848
    private val straightFlushHands  = 37260 + 4324

    private val belowHighCardHand = 0
    private val belowPairHand = highCardHands
    private val belowTwoPairHand = highCardHands + pairHands
    private val belowThreeOfAKindHand = highCardHands + pairHands + twoPairHands
    private val belowStraightHand = highCardHands + pairHands + twoPairHands + threeOfAKindHands
    private val belowFlushHand = highCardHands + pairHands + twoPairHands + threeOfAKindHands + straightHands
    private val belowFullHouseHand = highCardHands + pairHands + twoPairHands + threeOfAKindHands + straightHands + flushHands
    private val belowFourOfAKindHand = highCardHands + pairHands + twoPairHands + threeOfAKindHands + straightHands + flushHands + fullHouseHands
    private val belowStraightFlushHand = highCardHands + pairHands + twoPairHands + threeOfAKindHands + straightHands + flushHands + fullHouseHands + fourOfAKindHands

    private val belowHands = listOf(belowHighCardHand, belowPairHand, belowTwoPairHand, belowThreeOfAKindHand, belowStraightHand, belowFlushHand, belowFullHouseHand, belowFourOfAKindHand, belowStraightFlushHand)

    private val startingHandOdds: List<List<Float>> = listOf(
            listOf<Float>(),
            listOf<Float>(),
            listOf<Float>(0F,0F,0.51F,0.35F,0.36F,0.37F,0.37F,0.37F,0.4F ,0.42F,0.44F,0.47F,0.47F,0.53F,0.57F),
            listOf<Float>(0F,0F,0F   ,0.55F,0.38F,0.39F,0.39F,0.39F,0.4F ,0.43F,0.45F,0.48F,0.50F,0.54F,0.58F),
            listOf<Float>(0F,0F,0F   ,0F   ,0.58F,0.41F,0.41F,0.41F,0.42F,0.43F,0.46F,0.48F,0.51F,0.54F,0.59F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0.61F,0.43F,0.43F,0.44F,0.45F,0.47F,0.49F,0.52F,0.55F,0.60F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0.64F,0.45F,0.46F,0.47F,0.48F,0.50F,0.53F,0.56F,0.59F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0.67F,0.47F,0.48F,0.5F ,0.52F,0.54F,0.57F,0.6F ),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.69F,0.50F,0.52F,0.53F,0.55F,0.58F,0.61F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.62F,0.53F,0.55F,0.57F,0.59F,0.62F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.75F,0.57F,0.59F,0.61F,0.64F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.78F,0.59F,0.62F,0.65F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.8F ,0.62F,0.65F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.83F,0.66F),
            listOf<Float>(0F,0F,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0F   ,0.85F)
    )

    override fun move(state: Board, player: Player): PlayerAction {
        var livePlayers = state.activePlayers.size  + state.allInPlayers.size
        var winChance: Float
        if (state.communityCards.size == 0) {
            //starting hand
            var ranks = player.cards.map { it.rank.rank }
            ranks = ranks.sorted()
            winChance = startingHandOdds[ranks[0]][ranks[1]]

        } else if (state.communityCards.size == 5){
            val handToRank = player.cards
            handToRank.addAll(state.communityCards)
            val rankedHand = rankHand(handToRank)
            var winsOf: Float = (belowHands.get(rankedHand.handRank).toFloat()) / (totalHands.toFloat())
            winChance = 1 - winsOf.pow(state.activePlayers.size - 1)
        } else {
            var ranks = player.cards.map { it.rank.rank }
            ranks = ranks.sorted()
            var winChanceOld = startingHandOdds[ranks[0]][ranks[1]]

            val handToRank = player.cards
            handToRank.addAll(state.communityCards)
            val rankedHand = rankHand(handToRank)
            var winsOf: Float = (belowHands.get(rankedHand.handRank).toFloat()) / (totalHands.toFloat())
            println("$winsOf")
            winChance = ((1 - winsOf.pow(livePlayers - 1)) + winChanceOld) / 2
        }

        var harm: Float = (player.betTotal.toFloat() + state.currentBet.toFloat()) / (min(player.wealth, 5000) )
        var risk = winChance * harm
//        println("${player.cards[0].suit}:${player.cards[0].rank} + ${player.cards[1].suit}:${player.cards[1].rank}")
//        for (card in state.communityCards) {
//            print("${card.suit}:${card.rank} ")
//        }
//        println("")
//        println("${player.cards[0].suit}:${player.cards[0].rank} + ${player.cards[1].suit}:${player.cards[1].rank}")
        println("Hmm: $winChance, $harm, $risk")

        if (risk <= acceptableRisk) {
            return Call(player)
        } else {
            return Check(player)
        }
    }
}
