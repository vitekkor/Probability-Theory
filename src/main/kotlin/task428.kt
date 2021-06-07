//Done
fun task428() {
    for (n in 1..10) {
        val men = List(n) { true }
        val women = List(n) { false }
        val people = mutableListOf<Boolean>().also {
            it.addAll(men)
            it.addAll(women)
        }
        val numberOfExperiments = Int.MAX_VALUE
        var failed = 0
        for (ii in 1..numberOfExperiments) {
            //случайная рассадка
            people.shuffle()

            var previous = people.first()
            for (nn in people.indices) {
                if (nn == 0) continue
                if (previous == people[nn]) {
                    failed++
                    break
                }
                previous = people[nn]
            }
        }
        println("n=$n: P(A)=${(numberOfExperiments - failed) / numberOfExperiments.toDouble()}")
    }
}
