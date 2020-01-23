package poker

interface AIPlayer {
    fun move(state: Board): PlayerAction
}

class DummyPlayer(): AIPlayer {
    override fun move(state: Board) {

    }
}
