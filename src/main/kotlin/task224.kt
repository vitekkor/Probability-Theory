import kotlin.random.Random
//Done
fun task224() {
    val n = 100
    val k = 2
    val m = 25
    val productToTag = mutableMapOf<Int, Int>()
    for (i in 1..n) {
        productToTag[i] = (1..k).random()
    }
    var flag = false
    var l = 0
    val mj = mutableMapOf<Int, Int>()
    var remaining: Int
    while(!flag) {
        l += 1
        while (mj.values.sum() + 1 != m) {
            remaining = m
            mj.clear()
            for (i in 1..k) {
                val count = Random.nextInt(remaining)
                mj[i] = count
                remaining -= count
            }
        }
        mj[k] = mj[k]!! + 1
        val randomM = productToTag.keys.shuffled().take(m).map { it to productToTag[it] }
        val result = mutableMapOf<Int, Int>()
        randomM.forEach { result[it.second!!] = (result.getOrPut(it.second!!) {0}) + 1 }
        if (result.entries == mj.entries)
            flag = true
    }

    var prod = 1.0
    for (j in 1..k) {
        prod *= comb(productToTag.entries.sumBy { if (it.value == j) 1 else 0 }, mj[j]!!)
    }
    val theory = prod / comb(n, m)
    println(theory)
    println(1.0/l)
}

fun factorial(num: Int): Long {
    var result = 1L
    for (i in 2..num) result *= i
    return result
}
fun comb(n: Int, m: Int): Double {
    return factorial(n).toDouble() / factorial(n-m) / factorial(m)
}