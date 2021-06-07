import kotlin.random.Random
//Done
fun task843() {
    val event = mutableListOf(true, true, true, true, true, true, true, true, false, false)
    var averageN = 0L
    for (i in 1..Int.MAX_VALUE) {
        var n = 0
        event.shuffle()
        var eventHappened = 0
        while (eventHappened != 20) {
            if (event[Random.nextInt(0, 10)]) eventHappened++
            n++
        }
        averageN += n
    }
    println(averageN / Int.MAX_VALUE)
}