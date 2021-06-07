import kotlin.math.pow
import kotlin.random.Random
//Done
fun task37() {
    for(ii in 1..10) {
        val r = Random.nextDouble()
        val numberOfExperiments = Int.MAX_VALUE

        //пункт а)
        var numberOfFavorableOutcomesA = 0
        val conditionA = { x: Double, y: Double -> x.pow(2) + y.pow(2) <= 4 * r.pow(2) }

        //пункт б)
        var numberOfFavorableOutcomesB = 0
        val conditionB = { x: Double, y: Double ->
            val currentR = x * x + y * y
            //точка в круге радиусом r
            currentR <= r.pow(2)
                    //точка в кольце, ограниченном радиусами 2r и 3r
                    || (currentR >= 4 * r.pow(2) && currentR <= 9 * r.pow(2))
                    //точка в кольце, ограниченном радиусами 4r и 5r
                    || (currentR >= 16 * r.pow(2) && currentR <= 25 * r.pow(2))
        }

        for (i in 1..numberOfExperiments) {
            //координатная плоскость. Центры окружностей в точке (0.0, 0.0)
            var x = Random.nextDouble(-5.0 * r, 5.0 * r)
            var y = Random.nextDouble(-5.0 * r, 5.0 * r)
            // точка внутри окружности радусом 5r
            while (x.pow(2) + y.pow(2) > 25.0 * r * r) {
                x = Random.nextDouble(-5.0 * r, 5.0 * r)
                y = Random.nextDouble(-5.0 * r, 5.0 * r)
            }

            if (conditionA(x, y)) numberOfFavorableOutcomesA++
            if (conditionB(x, y)) numberOfFavorableOutcomesB++
        }

        print("r=$r\tP(A) = ${numberOfFavorableOutcomesA.toDouble() / numberOfExperiments}")
        println("\tP(B) = ${numberOfFavorableOutcomesB.toDouble() / numberOfExperiments}")
    }
}
