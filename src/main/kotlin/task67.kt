import kotlin.random.Random
//Done
fun task67() {
    //Радиолампа принадлежит к одной из трёх партий с вероятностями 0.25 (1 лампочка из 4х)
    //0.25 (1 лампочка из 4х) и 0.5 (2 лампочки из 4х)
    val part = listOf(1, 3, 2, 2).shuffled()
    //Вероятности того, что лампа проработает заданное число часов, для этих партий
    val worked1 = listOf(true, false, false, false, false, false, false, false, false, false).shuffled()
    val worked2 = listOf(true, true, false, false, false, false, false, false, false, false).shuffled()
    val worked3 = listOf(true, true, true, true, false, false, false, false, false, false).shuffled()
    var worked = 0
    for (lamp in 1..Int.MAX_VALUE) {
        val lampWorked = when (part[Random.nextInt(0, 4)]) {
            1 -> worked1[Random.nextInt(0, 10)]
            2 -> worked2[Random.nextInt(0, 10)]
            else -> worked3[Random.nextInt(0, 10)]
        }
        if (lampWorked) worked++
    }
    println(worked.toDouble()/Int.MAX_VALUE)
}