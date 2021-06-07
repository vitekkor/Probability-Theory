package task2

import kotlin.math.pow
import kotlin.math.sqrt

fun main(args: Array<String>) {

    funProb()
    //vi()
}

fun funProb() {
    val pW = 0.22
    val pL = 0.2815 + 0.4985

    var sum = 0.0
    for(i in 0..30){
        val step = pL.pow(i) * pW
        sum += step
        println(step)
    }
}

fun vi() {
    val vi = 1.96 * 1376.4982
    val n = 1000

    for (i in 1..n){
        val thPayback = vi / sqrt(i.toDouble())
        println(-1 * thPayback)
    }
}