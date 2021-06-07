import kotlin.random.Random
//Done
fun task1024() {
    val numberOfExperiments = Int.MAX_VALUE
    var success = 0
    val p = MutableList(100) { false }
    p[0] = true
    for (i in 1..numberOfExperiments) {
        p.shuffle()
        var count = 0
        for (thing in 1..200) {
            if (p[Random.nextInt(0, 100)]) count++
            if (count > 3) {
                success++
                break
            }
        }
    }
    println(success.toDouble() / numberOfExperiments)
}