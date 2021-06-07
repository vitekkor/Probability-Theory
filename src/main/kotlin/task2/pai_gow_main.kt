package task2

import java.io.File
import kotlin.math.sqrt

fun main(args: Array<String>) {
    val deck = Deck()
    val dealer = Dealer(deck)

    val playerHands = Hand()
    val dealerHands = Hand()

    val nGames = 1000000
    var wins = 0
    var loses = 0
    var draws = 0

    var loseStreak = 0
    val funProb = DoubleArray(100) { 0.0 }
    val averageTotalWin = DoubleArray(500) { 0.0 }
    val vi = 1.96 * 1300

    val wager = 100.0
    val commission = 0.05
    var ev = 0.0

    var casinoWallet = 0.0
    var playerWallet = 1000.0

    val payBack = File("./thPayback.txt")

    if (!payBack.exists()) {
        payBack.createNewFile()
    }

    payBack.bufferedWriter().use {
        for (i in 0 until nGames) {
            deck.assembleDeck53()

            dealer.dealCards(playerHands)
            dealer.dealCards(dealerHands)

            dealerHands.findCombinationsAndDistribute()
            playerHands.findCombinationsAndDistribute()

            println("DEALER\n$dealerHands")
            println("PLAYER\n$playerHands")

            when (playerHands.compareWithDealerHands(dealerHands)) {
                0 -> {
                    loses++
                    loseStreak++
                    playerWallet -= wager
                    casinoWallet += wager
                }
                1 -> {
                    wins++
                    funProb[loseStreak]++
                    loseStreak = 0
                    playerWallet += wager - (wager * commission)
                    casinoWallet = casinoWallet - wager + (wager * commission)
                }
                2 -> {
                    draws++
                    loseStreak++
                }
            }

            playerHands.clearHand()
            dealerHands.clearHand()

            if (i < 500) averageTotalWin[i] += playerWallet

            val thPayback = vi / sqrt(i.toDouble())
            if (i < 1000) it.append("$thPayback ")
        }
    }

    val fileFunProb = File("./funProb.txt")
    if (!fileFunProb.exists()) {
        fileFunProb.createNewFile()
    }
    fileFunProb.bufferedWriter().use {
        funProb.forEachIndexed { i, f ->
            if (i in 3..34) it.append("${f / nGames * 10} ")
        }
    }

    val averageTotalWinFile = File("./averageTotalWin.txt")

    if (!averageTotalWinFile.exists()) {
        averageTotalWinFile.createNewFile()
    }
    averageTotalWinFile.bufferedWriter().use {
        averageTotalWin.forEach { a ->
            if (a.isFinite()) it.append("$a ")
        }
    }

    println("Player wallet - $playerWallet")
    println("Casino wallet - $casinoWallet")

    println("Wins prob - ${wins.toDouble() / nGames}")
    println("Loses prob - ${loses.toDouble() / nGames}")
    println("Draws prob - ${draws.toDouble() / nGames}")
}

class Cards {
    enum class CardName(val value: Double) {
        Joker(0.14),
        Ace(0.14),
        King(0.13),
        Queen(0.12),
        Jack(0.11),
        N10(0.10),
        N9(0.09),
        N8(0.08),
        N7(0.07),
        N6(0.06),
        N5(0.05),
        N4(0.04),
        N3(0.03),
        N2(0.02);
    }

    enum class Color {
        Clubs, // Крести
        Spades, // Пики
        Hearts, // Червы
        Diamonds, // Бубны
        Joker
    }
}

class Deck {
    private val deck = mutableListOf<Card>()

    fun getListOfCards() = deck

    fun assembleDeck53() {
        deck.clear()
        for (name in Cards.CardName.values()) {
            if (name == Cards.CardName.Joker) {
                deck.add(Card(name, Cards.Color.Joker))
            } else {
                for (color in 0..Cards.Color.values().size - 2) {
                    deck.add(Card(name, Cards.Color.values()[color]))
                }
            }
        }
    }

    fun takeCard(ind: Int): Card {
        val card = deck[ind]
        deck.removeAt(ind)
        return card
    }
}

data class Card(val name: Cards.CardName, val color: Cards.Color) {
    override fun toString(): String = "$name OF $color"
}

open class Hand {
    data class HandType(
        var cards: MutableList<Card> = mutableListOf(),
        var comb: Combinations.Combination = Combinations.Combination.HighCard,
        var score: Double = 0.0
    )

    private val hand = mutableListOf<Card>()
    private val high = HandType(mutableListOf(), Combinations.Combination.HighCard, 0.0)
    private val low = HandType(mutableListOf(), Combinations.Combination.HighCard, 0.0)


    fun addCardInHands(card: Card) {
        hand.add(card)
    }

    fun findCombinationsAndDistribute(): List<Combinations.Combination> {
        val comb = Combinations()
        hand.sortBy { it.name.value }
        hand.sortBy { it.color }
        val allCombOnHand = comb.checkHandComb(hand).sortedBy { it.value }
        val bestComb = allCombOnHand.last()
        high.comb = bestComb
        comb.scoreHand(hand, high, bestComb)
        val remainingCombs = comb.checkHandComb(hand).sortedBy { it.value }
        val bestRemainingComb =
            if (remainingCombs.contains(Combinations.Combination.OnePair)) Combinations.Combination.OnePair else Combinations.Combination.HighCard
        low.comb = bestRemainingComb
        comb.scoreHand(hand, low, bestRemainingComb)

        if (low.cards.size != 2 && hand.isNotEmpty()) {
            val randomCard = hand.random()
            low.cards.add(randomCard)
            hand.remove(randomCard)
        }
        high.cards.addAll(hand)
        hand.clear()
        return allCombOnHand
    }

    fun compareWithDealerHands(dealerHand: Hand): Int {
        val playerHighScore = this.high.score
        val playerLowScore = this.low.score
        val dealerHighScore = dealerHand.high.score
        val dealerLowScore = dealerHand.low.score
        return if (playerHighScore > dealerHighScore && playerLowScore > dealerLowScore) {
            1
        } else if (playerHighScore < dealerHighScore && playerLowScore > dealerLowScore) {
            2
        } else if (playerHighScore > dealerHighScore && playerLowScore < dealerLowScore) {
            2
        } else 0
    }

    fun clearHand() {
        hand.clear()
        high.cards.clear()
        high.score = 0.0
        low.cards.clear()
        low.score = 0.0
    }

    override fun toString(): String {
        return "HIGH :: ${high.comb} :: ${high.score}\n${high.cards}\n" +
                "LOW :: ${low.comb} :: ${low.score}\n${low.cards}\n\n"
    }
}

class Dealer(private val deck: Deck) {
    fun dealCards(hands: Hand) {
        for (i in 1..7) {
            val randInd = (deck.getListOfCards().indices).random()
            val randCard = deck.takeCard(randInd)
            hands.addCardInHands(randCard)
        }
    }
}

class Combinations {
    private var joker = false

    enum class Combination(val value: Int) {
        FiveAces(200_000),
        RoyalFlush(100_000),
        StraightFlush(85_000),
        FourOfAKind(30_000),
        FullHouse(3500),
        Flush(690),
        Straight(240),
        ThreeOfAKind(110),
        TwoPair(12),
        OnePair(4),
        HighCard(1)
    }

    fun checkHandComb(hand: List<Card>): List<Combination> {
        val handCombs = mutableListOf<Combination>()
        hand.forEach { if (it.name == Cards.CardName.Joker) joker = true }

        if (checkFiveAces(hand)) handCombs.add(Combination.FiveAces)
        if (checkRoyalFlush(hand)) {
            handCombs.add(Combination.RoyalFlush)
        } else {
            if (checkStraightFlush(hand)) {
                handCombs.add(Combination.StraightFlush)
            } else {
                if (checkFlush(hand)) handCombs.add(Combination.Flush)
            }
        }
        if (checkFourOfAKind(hand)) handCombs.add(Combination.FourOfAKind)
        if (checkFullHouse(hand)) handCombs.add(Combination.FullHouse)
        if (checkStraight(hand)) handCombs.add(Combination.Straight)
        if (checkThreeOfAKind(hand)) handCombs.add(Combination.ThreeOfAKind)
        if (checkOnePair(hand)) handCombs.add(Combination.OnePair)
        if (checkTwoPair(hand)) handCombs.add(Combination.TwoPair)
        handCombs.add(Combination.HighCard)


        joker = false
        return handCombs
    }

    private fun checkFiveAces(hand: List<Card>): Boolean {
        var chA = 0
        hand.forEach { if (it.name == Cards.CardName.Ace) chA++ }
        if (chA == 4 && joker)
            return true
        return false
    }

    private fun checkRoyalFlush(hand: List<Card>): Boolean {
        val listRoyal = listOf(
            Cards.CardName.Ace,
            Cards.CardName.King,
            Cards.CardName.Queen,
            Cards.CardName.Jack,
            Cards.CardName.N10
        )
        val arr = IntArray(5) { 0 }
        lateinit var color: Cards.Color
        var helpColor = false
        hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
        arr.forEachIndexed { ind, ar ->
            if (ar == 5 || (ar == 4 && joker)) {
                color = Cards.Color.values()[ind]; helpColor = true
            }
        }

        if (helpColor) {
            var ch = 0
            fi@ for (el in listRoyal) {
                for (card in hand.reversed()) {
                    if (el == card.name && color == card.color) {
                        ch++
                        continue@fi
                    }
                }
            }
            if (ch == 5 || (ch == 4 && joker)) return true
        }

        return false
    }

    private fun checkStraightFlush(hand: List<Card>): Boolean {
        val arr = IntArray(5) { 0 }
        var color = Cards.Color.Hearts
        var helpColor = false
        hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
        arr.forEachIndexed { ind, ar ->
            if (ar == 5 || (ar == 4 && joker)) {
                color = Cards.Color.values()[ind]; helpColor = true
            }
        }

        if (helpColor) {
            var ch = 0
            var j = 0
            if (joker) {
                j = 1
            }
            for (i in hand.indices) {
                if (ch == 4) return true
                if (hand[i].color != color) continue
                if (i != hand.size - 1) {
                    if (((hand[i].name.value + 0.01) * 100).toInt() == ((hand[i + 1].name.value) * 100).toInt() && hand[i + 1].color == color) {
                        ch++
                    } else {
                        if (j == 1) {
                            if (((hand[i].name.value + 0.02) * 100).toInt() == ((hand[i + 1].name.value) * 100).toInt() && hand[i + 1].color == color && ch != 3) {
                                j--
                                ch += 2
                            }
                            if (ch == 3) {
                                j--
                                ch++
                            }
                        } else {
                            ch = 0
                            if (joker) {
                                j = 1
                            }
                        }
                    }
                }
            }
        }

        return false
    }

    private fun checkFourOfAKind(hand: List<Card>): Boolean {
        val arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        for ((i, a) in arr.withIndex()) {
            if (a == 4 || (a == 3 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) return true
        }
        return false
    }

    private fun checkFullHouse(hand: List<Card>): Boolean {
        val arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        var two = 0
        var three = 0
        val indOfTwo = mutableListOf<Int>()
        for ((i, a) in arr.withIndex()) {
            if (a == 3) three++
            if (a == 2) {
                two++
                indOfTwo.add(i)
            }
        }

        var thereAce = false
        indOfTwo.forEach {
            if (Cards.CardName.values()[it] == Cards.CardName.Ace) {
                thereAce = true
            }
        }
        if ((two > 0 && three > 0) || (two > 1 && joker && thereAce)) return true
        return false
    }

    private fun checkFlush(hand: List<Card>): Boolean {
        val arr = IntArray(5) { 0 }
        hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
        arr.forEach { if (it == 5 || (it == 4 && joker)) return true }
        return false
    }

    private fun checkStraight(hand: List<Card>): Boolean {
        val arr = IntArray(Cards.CardName.values().size - 1) { 0 }
        hand.forEach {
            if (Cards.CardName.values().indexOf(it.name) != 0) arr[Cards.CardName.values().indexOf(it.name) - 1]++
        }

        var ch = 0
        var j = 0
        var i = 0
        if (joker) {
            j = 1
        }
        while (i < arr.size) {
            if (ch == 5) return true
            if (arr[i] > 0) {
                ch++
                i++
            } else {
                if (j == 1) {
                    if (i != arr.size - 1 && arr[i + 1] > 0) {
                        j--
                        ch++
                        i++
                    } else {
                        ch = 0
                        i++
                    }
                } else {
                    ch = 0
                    if (joker) {
                        j = 1
                    }
                    i++
                }
            }

        }
        if (ch == 5) return true
        return false
    }

    private fun checkThreeOfAKind(hand: List<Card>): Boolean {
        val arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        for ((i, a) in arr.withIndex()) {
            if (a >= 3 || (a == 2 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) return true
        }
        return false
    }

    private fun checkTwoPair(hand: List<Card>): Boolean {
        val arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        var ch = 0
        var j = false
        if (joker)
            j = true

        for ((i, a) in arr.withIndex()) {
            if (a >= 2)
                ch++
            else if (a == 1 && j && Cards.CardName.values()[i] == Cards.CardName.Ace) {
                ch++
                j = false
            }
        }

        if (ch > 1) return true
        return false
    }

    private fun checkOnePair(hand: List<Card>): Boolean {
        val arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        for ((i, a) in arr.withIndex()) {
            if (a >= 2 || (a == 1 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) return true
        }
        return false
    }

    fun scoreHand(hand: MutableList<Card>, handType: Hand.HandType, comb: Combination) {
        hand.forEach { if (it.name == Cards.CardName.Joker) joker = true }
        when (comb) {
            Combination.FiveAces -> {
                var i = 0
                while (i < hand.size) {
                    if (hand[i].name == Cards.CardName.Ace || hand[i].name == Cards.CardName.Joker) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }
                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }

            Combination.RoyalFlush -> {
                val listRoyal = listOf(
                    Cards.CardName.Ace,
                    Cards.CardName.King,
                    Cards.CardName.Queen,
                    Cards.CardName.Jack,
                    Cards.CardName.N10
                )
                val arr = IntArray(5) { 0 }
                lateinit var color: Cards.Color
                hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
                arr.forEachIndexed { ind, ar ->
                    if (ar == 5 || (ar == 4 && joker)) {
                        color = Cards.Color.values()[ind]
                    }
                }

                var j = 0
                if (joker) {
                    j = 1
                }
                var scoreJ = 0.0
                fi@ for (el in listRoyal) {
                    var i = hand.size - 1
                    while (i > -1) {
                        if (el == hand[i].name && color == hand[i].color) {
                            handType.cards.add(hand[i])
                            hand.remove(hand[i])
                            continue@fi
                        } else {
                            i--
                        }
                    }
                    if (j == 1) {
                        handType.cards.add(hand[hand.indexOf(Card(Cards.CardName.Joker, Cards.Color.Joker))])
                        hand.remove(hand[hand.indexOf(Card(Cards.CardName.Joker, Cards.Color.Joker))])
                        scoreJ = el.value
                        j--
                        continue@fi
                    }
                }

                var score = handType.cards.sumByDouble { it.name.value }
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }

            Combination.StraightFlush -> {
                val arr = IntArray(5) { 0 }
                var color = Cards.Color.Diamonds
                hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
                arr.forEachIndexed { ind, ar ->
                    if (ar == 5 || (ar == 4 && joker)) {
                        color = Cards.Color.values()[ind]
                    }
                }

                var ch = 0
                var j = 0
                if (joker) {
                    j = 1
                }
                val listCurComb = mutableListOf<Card>()
                var scoreJ = 0.0
                var helpRep = true
                for (i in hand.indices) {
                    if (ch == 4) break
                    if (hand[i].color != color) continue
                    if (i != hand.size - 1) {
                        if (((hand[i].name.value + 0.01) * 100).toInt() == ((hand[i + 1].name.value) * 100).toInt() && hand[i + 1].color == color) {
                            ch++
                            if (helpRep) {
                                listCurComb.add(hand[i])
                                helpRep = false
                            }
                            listCurComb.add(hand[i + 1])
                        } else {
                            if (j == 1) {
                                if (((hand[i].name.value + 0.02) * 100).toInt() == ((hand[i + 1].name.value) * 100).toInt() && hand[i + 1].color == color && ch != 3) {
                                    j--
                                    ch += 2
                                    listCurComb.add(hand[i])
                                    scoreJ = hand[i].name.value + 0.01
                                    listCurComb.add(Card(Cards.CardName.Joker, Cards.Color.Joker))
                                }
                                if (ch == 3) {
                                    j--
                                    ch++
                                    scoreJ = hand[i].name.value + 0.01
                                    listCurComb.add(Card(Cards.CardName.Joker, Cards.Color.Joker))
                                }
                            } else {
                                ch = 0
                                scoreJ = 0.0
                                listCurComb.clear()
                                helpRep = true
                                if (joker) {
                                    j = 1
                                }
                            }
                        }
                    }
                }

                for (c in listCurComb) {
                    hand.remove(c)
                    handType.cards.add(c)
                }

                var score = handType.cards.sumByDouble { it.name.value }
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }
            Combination.FourOfAKind -> {
                val arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

                var j = 0
                if (joker) {
                    j = 1
                }
                var tCard = Cards.CardName.N2
                for ((i, a) in arr.withIndex()) {
                    if (a == 4 || (a == 3 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) {
                        tCard = Cards.CardName.values()[i]
                        if (a == 3) j--
                    }
                }

                var i = 0
                while (i < hand.size) {
                    if (hand[i].name == tCard || (hand[i].name == Cards.CardName.Joker && joker && j == 0)) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }

                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }
            Combination.FullHouse -> {
                val arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
                var two = 0
                var three = 0
                for ((i, a) in arr.withIndex()) {
                    if (a == 3 && three == 0) {
                        var o = 0
                        while (o < hand.size) {
                            if (hand[o].name == Cards.CardName.values()[i]) {
                                handType.cards.add(hand[o])
                                hand.removeAt(o)
                            } else o++
                        }
                        three++
                    }
                    if ((a == 2 && two < 1 && three == 1) || (a > 0 && two == 0 && three == 0 && Cards.CardName.values()[i] == Cards.CardName.Ace && joker)) {
                        if (a > 0 && two == 0 && three == 0 && Cards.CardName.values()[i] == Cards.CardName.Ace && joker) {
                            var o = 0
                            while (o < hand.size) {
                                if (hand[o].name == Cards.CardName.values()[i]) {
                                    handType.cards.add(hand[o])
                                    hand.removeAt(o)
                                } else o++
                            }
                            hand.remove(Card(Cards.CardName.Joker, Cards.Color.Joker))
                            handType.cards.add(Card(Cards.CardName.Joker, Cards.Color.Joker))
                            if (a == 1) {
                                two++
                            } else three++
                        } else {
                            var o = 0
                            while (o < hand.size) {
                                if (hand[o].name == Cards.CardName.values()[i]) {
                                    handType.cards.add(hand[o])
                                    hand.removeAt(o)
                                } else o++
                            }
                            two++
                        }
                    }
                }

                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }
            Combination.Flush -> {
                val arr = IntArray(5) { 0 }
                var color = Cards.Color.Joker
                hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
                arr.forEachIndexed { ind, a -> if (a == 5 || (a == 4 && joker)) color = Cards.Color.values()[ind] }

                val j = if (joker) 1 else 0
                var ch = 0
                var i = hand.size - 1
                while (i > -1 && ch != 5) {
                    if (hand[i].name == Cards.CardName.Joker) {
                        handType.cards.add(hand[i])
                        hand.removeAt(i)
                        ch++
                    } else if (hand[i].color == color) {
                        handType.cards.add(hand[i])
                        hand.removeAt(i)
                        ch++
                    }
                    i--
                }

                var scoreJ = 0.0
                for (i in Cards.CardName.values()) {
                    if (!handType.cards.contains(Card(i, color))) {
                        scoreJ = i.value
                    }
                }

                var score = handType.cards.sumByDouble { it.name.value }
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }
            Combination.Straight -> {
                val arr = IntArray(Cards.CardName.values().size - 1) { 0 }
                hand.forEach {
                    if (Cards.CardName.values().indexOf(it.name) != 0) arr[Cards.CardName.values()
                        .indexOf(it.name) - 1]++
                }

                var ch = 0
                var j = 0
                var i = 0
                if (joker) {
                    j = 1
                }
                var scoreJ = 0.0
                val listCurComb = mutableListOf<Card>()
                while (i < arr.size && ch != 5) {
                    if (arr[i] > 0) {
                        for (c in hand) {
                            if (c.name == Cards.CardName.values()[i + 1]) {
                                listCurComb.add(c)
                                break
                            }
                        }
                        ch++
                        i++
                    } else {
                        if (j == 1) {
                            if (i != arr.size - 1 && arr[i + 1] > 0) {
                                listCurComb.add(Card(Cards.CardName.Joker, Cards.Color.Joker))
                                scoreJ = Cards.CardName.values()[i + 2].value
                                j--
                                ch++
                                i++
                            } else {
                                listCurComb.clear()
                                ch = 0
                                i++
                            }
                        } else {
                            listCurComb.clear()
                            ch = 0
                            if (joker) {
                                j = 1
                            }
                            i++
                        }
                    }
                }

                for (c in listCurComb) {
                    handType.cards.add(c)
                    hand.remove(c)
                }

                var score = handType.cards.sumByDouble { it.name.value }
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }
            Combination.ThreeOfAKind -> {
                val arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

                var j = 0
                if (joker) {
                    j = 1
                }
                var tCard = Cards.CardName.N2
                for ((i, a) in arr.withIndex()) {
                    if (a == 3 || (a == 2 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) {
                        tCard = Cards.CardName.values()[i]
                        if (a == 2) j--
                    }
                }

                var i = 0
                while (i < hand.size) {
                    if (hand[i].name == tCard || (hand[i].name == Cards.CardName.Joker && joker && j == 0)) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }

                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }
            Combination.TwoPair -> {
                val arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
                var ch = 0
                var j = false
                if (joker)
                    j = true

                for ((i, a) in arr.withIndex()) {
                    if (a >= 2 && ch < 2) {
                        for (c in hand) {
                            if (c.name == Cards.CardName.values()[i]) {
                                handType.cards.add(c)
                            }
                        }
                        ch++
                    } else if (a == 1 && j && Cards.CardName.values()[i] == Cards.CardName.Ace) {
                        for (c in hand) {
                            if (c.name == Cards.CardName.values()[i]) {
                                handType.cards.add(c)
                            }
                        }
                        handType.cards.add(Card(Cards.CardName.Joker, Cards.Color.Joker))
                        ch++
                        j = false
                    }
                }

                for (c in handType.cards) {
                    hand.remove(c)
                }

                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }
            Combination.OnePair -> {
                val arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

                var j = 0
                if (joker) {
                    j = 1
                }
                var tCard = Cards.CardName.N2
                for ((i, a) in arr.withIndex()) {
                    if (a == 2 || (a == 1 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) {
                        tCard = Cards.CardName.values()[i]
                        if (a == 1) j--
                    }
                }

                var i = 0
                while (i < hand.size) {
                    if (hand[i].name == tCard || (hand[i].name == Cards.CardName.Joker && joker && j == 0)) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }

                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }
            else -> {
                var hc = hand[0]
                hand.forEach { if (it.name.value > hc.name.value) hc = it }
                handType.cards.add(hc)
                hand.remove(hc)
                handType.score = hc.name.value
            }
        }
    }
}