package poker

interface AIPlayer {
    fun move(state: Board): PlayerAction
}

class DummyPlayer: AIPlayer {
    override fun move(state: Board): PlayerAction {
        return when (val action = readLine()) {
            "p" -> Check(0)
            "f" -> Fold(0)
            "c" -> Call(0)
            else -> Raise(0, action!!.toInt())
        }
    }
}
