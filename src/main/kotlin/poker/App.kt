package poker

fun main(args: Array<String>) {
    val players = listOf(DummyPlayer(), DummyPlayer(), DummyPlayer(), DummyPlayer())
    val board = Board(players)
    board.playRound()
}
