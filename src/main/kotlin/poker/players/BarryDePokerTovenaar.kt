package poker.players

import poker.*

/**
 * Created by daan.tebokkel on 1/24/20
 */
class BarryDePokerTovenaar: AIPlayer {
    override fun move(state: Board, player: Player): PlayerAction {
        return Check(player)
    }

    //check board state


    //
}