import kotlin.random.Random
//Done
fun task1120() {
    val numberOfExperiments = Int.MAX_VALUE / 10
    var black = 0
    var white = 0
    for (i in 1..numberOfExperiments) {
        val urn = mutableListOf<Boolean>()
        val balls = MutableList(100) { false }.apply { repeat(50) { this[it] = true } }
        balls.shuffle()
        var blackBallArrived = false
        for (ball in 1..10) {
            val index = Random.nextInt(0, balls.size)
            if (balls[index] && !blackBallArrived) blackBallArrived = true
            if (blackBallArrived) {
                urn.add(balls[index])
                balls.removeAt(index)
            }
        }
        black += urn.count { it }
        white += urn.count { !it }
    }
    println(black.toDouble() / numberOfExperiments)
    println(white.toDouble() / numberOfExperiments)
}