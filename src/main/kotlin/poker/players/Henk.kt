package poker.players
import poker.*
import java.lang.Integer.max
class SirRaiseALot(override var name: String = "SirRaiseALot", var piet: Double = 1.0): AIPlayer {
    var maxBetThisRound = 0
    var myBetSoFar = 0
    var willingToBet = 0
    var minBet = 0
    var wealth = 0
    var hand = 0
    var timesRaised = 0
    var size = 0
    override fun move(state: Board, player: Player): PlayerAction {
        var cards = player.cards
        for (comminityCard in state.communityCards) {
            cards.add(comminityCard)
        }
        var renk = rankHand(cards)
        var highCard = renk.deciders[0]
        hand = renk.handRank
        maxBetThisRound = state.currentBet
        myBetSoFar = player.betThisRound
        minBet = state.minBet
        wealth = player.wealth
        if (state.communityCards.size > size) {
            size = state.communityCards.size
            timesRaised = 0
        }
        if (state.communityCards.size == 0) {
            if (hand == 1) {
                setWillingToBetHighBeforeFlop()
                return raise(player)
            } else if (highCard >= 10) {
                setWillingToBetLowBeforeFlop()
                return raise(player)
            } else {
                return Check(player)
            }
        } else {
            if (hand > 1) {
                setWillingToBetHigh()
                return raise(player)
            } else if (hand == 1){
                setWillingToBetLow()
                raise(player)
            } else {
                return Check(player)
            }
        }
        return Raise(player, 100)
    }
    fun raise(player: Player): PlayerAction {
        val henk = maxBetThisRound - myBetSoFar
        if ( henk < willingToBet) {
            if (timesRaised < 3) {
                return Raise(player, willingToBet)
            } else {
                return Call(player)
            }
        } else if (henk < willingToBet*1.5 || hand > 2) {
            return Call(player)
        }
        return Check(player)
    }
    fun setWillingToBetHigh() {
        willingToBet = max(minBet, wealth/(2*piet).toInt())
    }
    fun setWillingToBetLow() {
        willingToBet = max(minBet, wealth/(10*piet).toInt())
    }
    fun setWillingToBetHighBeforeFlop() {
        willingToBet = max(minBet, wealth/(10*piet).toInt())
    }
    fun setWillingToBetLowBeforeFlop() {
        willingToBet = max(minBet, wealth/(20*piet).toInt())
    }
}
