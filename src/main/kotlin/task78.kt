import kotlin.random.Random
//Done
fun task78() {
    val numberOfExperiments = Int.MAX_VALUE
    val first = listOf(true, true, true, true, false).shuffled()
    val second = listOf(true, true, true, false).shuffled()
    val third = listOf(true, true, false).shuffled()

    var thirdMissed = 0

    for (i in 1..numberOfExperiments) {
        //третий попал
        var thirdHit = third[Random.nextInt(0, 3)]
        //попадания всех трёх стреклов
        val listOfHits = mutableListOf(
            first[Random.nextInt(0, 5)],
            second[Random.nextInt(0, 4)],
            thirdHit
        )
        //генирируем случайные попадания так, чтобы из трёх выстрелов трёх стрелков было только два попадания
        while (listOfHits.count { it } != 2) {
            listOfHits.clear()
            thirdHit = third[Random.nextInt(0, 3)]
            listOfHits.add(first[Random.nextInt(0, 5)])
            listOfHits.add(second[Random.nextInt(0, 4)])
            listOfHits.add(thirdHit)
        }

        if (!thirdHit) thirdMissed++
    }
    println(thirdMissed.toDouble() / numberOfExperiments)
}