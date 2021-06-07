# fun main(args: Array<String>) {
import math
import random
from enum import Enum


def fun():
    deck = Deck()
    dealer = Dealer(deck)

    playerHands = Hand()
    dealerHands = Hand()

    nGames = 1000000
    wins = 0
    loses = 0
    draws = 0

    loseStreak = 0
    funProb = [0.0 for i in range(0, 100)]  # DoubleArray(100) { 0.0 }
    averageTotalWin = []  # DoubleArray(nGames) { 0.0 }
    vi = 1.96 * 1300

    wager = 100.0
    commission = 0.05
    ev = 0.0

    casinoWallet = 0.0
    playerWallet = 1000.0

    for i in range(nGames):  # for (i in 0 until nGames) {
        deck.assembleDeck53()

        dealer.dealCards(playerHands)
        dealer.dealCards(dealerHands)

        dealerHands.findCombinationsAndDistribute()
        playerHands.findCombinationsAndDistribute()

        # print("DEALER\n%s\n", dealerHands)
        # print("PLAYER\%s", playerHands)

        comprassion = playerHands.compareWithDealerHands(dealerHands)

        if comprassion == 0:
            loses = loses + 1
            loseStreak = loseStreak + 1
            playerWallet = playerWallet - wager
            casinoWallet = casinoWallet + wager
        elif comprassion == 1:
            wins = wins + 1
            funProb[loseStreak] = funProb[loseStreak] + 1
            loseStreak = 0
            playerWallet = playerWallet + wager - (wager * commission)
            casinoWallet = casinoWallet - wager + (wager * commission)
        else:
            draws = draws + 1
            loseStreak = loseStreak + 1

        playerHands.clearHand()
        dealerHands.clearHand()

        averageTotalWin.append(playerWallet)  # [i] += playerWallet

        thPayback = vi / math.sqrt(i)
        # print("%f\n", -1 * thPayback)

    for i in range(len(funProb)):
        print("%f\n", funProb[i] / nGames * 10)

    print("funprob1\n")

    s = 0.0
    for i in range(len(funProb)):
        s = s + funProb[i] / nGames * 10
        print("%f\n", funProb[i] / nGames * 10)
        print("%f\n", s)
    print("funprob\n")

    # for i in range(len(averageTotalWin)):
    #   print("%f\n", averageTotalWin[i] / 1000)

    # print(playerWallet)
    # print(casinoWallet)

    print(wins.toDouble() / nGames)
    print("\n")
    print(loses.toDouble() / nGames)
    print("\n")
    print(draws.toDouble() / nGames)
    print("\n")
    print("\n")
    pW = wins.toDouble() / nGames
    pL = loses.toDouble() / nGames + draws.toDouble() / nGames

    s = 0.0
    for i in range(31):
        step = pL ** i * pW
        s = s + step
        print("%f\n", step)


class Cards:
    class CardName(Enum):
        Joker = 0.14
        Ace = 0.14
        King = 0.13
        Queen = 0.12
        Jack = 0.11
        N10 = 0.10
        N9 = 0.09
        N8 = 0.08
        N7 = 0.07
        N6 = 0.06
        N5 = 0.05
        N4 = 0.04
        N3 = 0.03
        N2 = 0.02

    class Color(Enum):
        Clubs = 0  # Крести
        Spades = 1  # Пики
        Hearts = 2  # Червы
        Diamonds = 3  # Бубны
        Joker = 4


class Deck:
    deck = []  # mutableListOf<Card>()

    def getListOfCards(self):
        return self.deck

    def assembleDeck53(self):
        self.deck.clear()
        for name in Cards.CardName:
            if name == Cards.CardName.Joker:
                self.deck.append(Card(name, Cards.Color.Joker))
            else:
                for COLOR in Cards.Color:
                    if COLOR.value > 3: break
                    self.deck.append(Card(name, COLOR))

    def takeCard(self, ind):
        card = self.deck[ind]
        self.deck.__delitem__(ind)
        return card


class Card:
    name: Cards.CardName
    color: Cards.Color

    def __init__(self, name, color):
        self.name = name
        self.color = color

    def __str__(self):
        return "{} of {}".format(self.name, self.color)

class Combinations:
    joker = False

    class Combination(Enum):
        FiveAces = 200_000
        RoyalFlush = 100_000
        StraightFlush = 85_000
        FourOfAKind = 30_000
        FullHouse = 3500
        Flush = 690
        Straight = 240
        ThreeOfAKind = 110
        TwoPair = 12
        OnePair = 4
        HighCard = 1

    def checkHandComb(self, hand):
        handCombs = [] #mutableListOf<Combination>()
        for i in range(len(hand)):
            if hand[i].name == Cards.CardName.Joker:
                joker = True

        if self.checkFiveAces(hand):
            handCombs.append(self.Combination.FiveAces)
        if self.checkRoyalFlush(hand):
            handCombs.append(self.Combination.RoyalFlush)
        else:
            if self.checkStraightFlush(hand):
                handCombs.append(self.Combination.StraightFlush)
            else:
                if self.checkFlush(hand): handCombs.append(self.Combination.Flush)
        if self.checkFourOfAKind(hand):
            handCombs.append(self.Combination.FourOfAKind)
        if self.checkFullHouse(hand): handCombs.append(self.Combination.FullHouse)
        if self.checkStraight(hand): handCombs.append(self.Combination.Straight)
        if self.checkThreeOfAKind(hand): handCombs.append(self.Combination.ThreeOfAKind)
        if self.checkOnePair(hand): handCombs.append(self.Combination.OnePair)
        if self.checkTwoPair(hand): handCombs.append(self.Combination.TwoPair)
        handCombs.append(self.Combination.HighCard)


        self.joker = False
        return handCombs

    def checkFiveAces(hand):
        chA = 0
        for i in range(len(hand)):
            if hand[i].name == Cards.CardName.Ace:
                chA = chA + 1
        if chA == 4 and joker:
            return True
        return False

    def checkRoyalFlush(hand):
        listRoyal = [Cards.CardName.Ace, Cards.CardName.King, Cards.CardName.Queen, Cards.CardName.Jack,Cards.CardName.N10]
        arr = [0 for i in range(5)]
        color_: Cards.Color
        helpColor_ = False
        for i in range(len(hand)):
            arr[hand[i].color.value] = arr[hand[i].color.value] + 1
        for i in range(len(arr)):
            if arr[i] == 5 or (arr[i] == 4 and joker):
                for c in Cards.Color:
                    if c.value == i:
                        color_ = c
                        break
                helpColor_ = True

        if helpColor_:
            ch = 0
            for el in listRoyal:
                for card in hand.reverse():
                    if el == card.name and color == card.color:
                        ch = ch + 1
                        break
            if ch == 5 or (ch == 4and joker):
                return True
        return False

    def checkStraightFlush(hand):
        arr = [0 for i in range(5)]
        color = Cards.Color.Hearts
        helpColor_ = False
        for i in range(len(hand)):
            arr[hand[i].color.value] = arr[hand[i].color.value] + 1
        for i in range(len(arr)):
            if arr[i] == 5 or (arr[i] == 4 and joker):
                for c in Cards.Color:
                    if c.value == i:
                        color_ = c
                        break
                helpColor_ = True

        if helpColor_:
            ch = 0
            j = 0
            if joker:
                j = 1

            for i in range(len(hand)):
                if ch == 4:
                    return True
                if hand[i].color != color:
                    continue
            if i != len(hand) - 1:
                if int((hand[i].name.value + 0.01) * 100) == int(hand[i + 1].name.value * 100) and hand[i + 1].color == color:
                    ch = ch + 1
                else:
                    if j == 1:
                        if int((hand[i].name.value + 0.02) * 100) == int(hand[i + 1].name.value * 100) and hand[i + 1].color == color and ch != 3:
                            j = j - 1
                            ch = ch +2
                        if ch == 3:
                            j = j - 1
                            ch = ch + 1
                        else:
                            ch = 0
                            if joker:
                                j = 1
        return False

    def checkFourOfAKind(hand)
        arr = [0 for i in range(Cards.CardName.__len__())]
        for i in range(len(hand)):
            arr[hand[i].color.value] = arr[hand[i].color.value] + 1
        for i in range(len(arr)):
            ifstate = False
            for c in Cards.CardName:
                if c == Cards.CardName.Ace:
                    ifstate = True
                    break
            if arr[i] == 4 or (arr[i] == 3 and joker and ifstate):
                return True
        return False


    def checkFullHouse(hand):
        arr = [0 for i in range(Cards.CardName.__len__())]
        for i in range(len(hand)):
            arr[hand[i].color.value] = arr[hand[i].color.value] + 1
        two = 0
        three = 0
        indOfTwo = []
        for i in range(len(arr)):
            if arr[i] == 3:
                three = three + 1
            if arr[i] == 2:
                two = two + 1
                indOfTwo.append(i)

        thereAce = False
        for i in range(len(indOfTwo)):
            cc:Cards.CardName
            for c in Cards.CardName:
                if c.value == i:
                    cc = c
                    break
            if cc == Cards.CardName.Ace:
                thereAce = True
        if (two > 0 and three > 0) or (two > 1 and joker and thereAce):
            return True
        return False

    def checkFlush(hand):
        arr = [0 for i in range(5)]
        for i in range(len(hand)):
            arr[hand[i].color.value] = arr[hand[i].color.value] + 1
        for i in range(len(arr)):
            if arr[i] == 5 or (a[i] == 4 and joker):
                return True
        return False

    def checkStraight(hand):
        arr = [0 for i in range(Cards.CardName.__len__() - 1)]
        # TODO
       # for i in range(len(hand)):
            # if (Cards.CardName.values().indexOf(it.name) != 0) arr[Cards.CardName.values().indexOf(it.name)-1]++

        ch = 0
        j = 0
        i = 0
        if joker:
            j = 1
        while i < arr.__len__():
            if ch == 5:
                return True
            if arr[i] > 0:
                ch = ch + 1
                i = i +1
            else:
                if j == 1:
                    if i != arr.__len__() - 1 and arr[i+1] > 0:
                        j = j- 1
                        ch = ch+1
                        i = i +1
                    else:
                        ch = 0
                        i = i +1
                else:
                    ch = 0
                    if joker:
                        j = 1
                    i = i +1

        if ch == 5: return True
        return False


    def checkThreeOfAKind(hand):
        arr = [0 for i in range(Cards.CardName.__len__())]
        # TODO hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

        for ((i, a) in arr.withIndex()){
            if (a >= 3 || (a == 2 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) return true
        }
        return false
    }

    private fun checkTwoPair(hand: List<Card>): Boolean {
        arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        ch = 0
        j = false
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
        arr = IntArray(Cards.CardName.values().size) { 0 }
        hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
        for ((i, a) in arr.withIndex()){
            if (a >= 2 || (a == 1 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) return true
        }
        return false
    }

    fun scoreHand(hand: MutableList<Card>, handType: Hand.HandType, comb: Combination) {
        hand.forEach { if(it.name == Cards.CardName.Joker) joker = true}
        when (comb){
            Combination.FiveAces -> {
                i = 0
                while (i < hand.size) {
                    if (hand[i].name == Cards.CardName.Ace || hand[i].name == Cards.CardName.Joker) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }
                handType.score = (handType.cards.sumByDouble { it.name.value } * comb.value)
            }

            Combination.RoyalFlush -> {
                listRoyal = listOf(Cards.CardName.Ace, Cards.CardName.King, Cards.CardName.Queen, Cards.CardName.Jack,Cards.CardName.N10)
                arr = IntArray(5) { 0 }
                lateinit color: Cards.Color
                hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
                arr.forEachIndexed { ind, ar -> if (ar == 5 || (ar == 4 && joker)) { color = Cards.Color.values()[ind] } }

                j = 0
                if(joker) {
                    j = 1
                }
                scoreJ = 0.0
                fi@for (el in listRoyal) {
                    i = hand.size - 1
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

                score = handType.cards.sumByDouble {it.name.value}
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }

            Combination.StraightFlush -> {
                arr = IntArray(5) { 0 }
                color = Cards.Color.Diamonds
                hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
                arr.forEachIndexed { ind, ar ->
                    if (ar == 5 || (ar == 4 && joker)) {
                        color = Cards.Color.values()[ind]
                    }
                }

                ch = 0
                j = 0
                if (joker) {
                    j = 1
                }
                listCurComb = mutableListOf<Card>()
                scoreJ = 0.0
                helpRep = true
                for (i in hand.indices) {
                    if (ch == 4) break
                    if (hand[i].color != color) continue
                    if (i != hand.size - 1) {
                        if (((hand[i].name.value + 0.01)*100).toInt() == ((hand[i + 1].name.value)*100).toInt() && hand[i + 1].color == color) {
                            ch++
                            if (helpRep) {
                                listCurComb.add(hand[i])
                                helpRep = false
                            }
                            listCurComb.add(hand[i+1])
                        } else {
                            if (j == 1) {
                                if (((hand[i].name.value + 0.02)*100).toInt() == ((hand[i + 1].name.value)*100).toInt() && hand[i + 1].color == color && ch != 3) {
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

                score = handType.cards.sumByDouble {it.name.value}
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }
            Combination.FourOfAKind -> {
                arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

                j = 0
                if (joker) {
                    j = 1
                }
                tCard = Cards.CardName.N2
                for ((i, a) in arr.withIndex()) {
                    if (a == 4 || (a == 3 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) {
                        tCard = Cards.CardName.values()[i]
                        if(a == 3) j--
                    }
                }

                i = 0
                while (i < hand.size) {
                    if (hand[i].name == tCard || (hand[i].name == Cards.CardName.Joker && joker && j == 0)) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }

                handType.score = (handType.cards.sumByDouble {it.name.value} * comb.value)
            }
            Combination.FullHouse -> {
                arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
                two = 0
                three = 0
                for ((i, a) in arr.withIndex()){
                    if (a == 3 && three == 0) {
                        o = 0
                        while(o < hand.size) {
                            if (hand[o].name == Cards.CardName.values()[i]) {
                                handType.cards.add(hand[o])
                                hand.removeAt(o)
                            } else o++
                        }
                        three++
                    }
                    if ((a == 2 && two < 1 && three == 1) || (a > 0 && two == 0 && three == 0 && Cards.CardName.values()[i] == Cards.CardName.Ace && joker)) {
                        if (a > 0 && two == 0 && three == 0 && Cards.CardName.values()[i] == Cards.CardName.Ace && joker) {
                            o = 0
                            while(o < hand.size) {
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
                            o = 0
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

                handType.score = (handType.cards.sumByDouble {it.name.value} * comb.value)
            }
            Combination.Flush -> {
                arr = IntArray(5) { 0 }
                color = Cards.Color.Joker
                hand.forEach { arr[Cards.Color.values().indexOf(it.color)]++ }
                arr.forEachIndexed {ind, a -> if (a == 5 || (a == 4 && joker)) color = Cards.Color.values()[ind]}

                j = if(joker) 1 else 0
                ch = 0
                i = hand.size - 1
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

                scoreJ = 0.0
                for (i in Cards.CardName.values()) {
                    if (!handType.cards.contains(Card(i, color))) {
                        scoreJ = i.value
                    }
                }

                score = handType.cards.sumByDouble {it.name.value}
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }
            Combination.Straight -> {
                arr = IntArray(Cards.CardName.values().size-1) { 0 }
                hand.forEach { if(Cards.CardName.values().indexOf(it.name) != 0) arr[Cards.CardName.values().indexOf(it.name)-1]++ }

                ch = 0
                j = 0
                i = 0
                if (joker) {
                    j = 1
                }
                scoreJ = 0.0
                listCurComb = mutableListOf<Card>()
                while (i < arr.size && ch != 5) {
                    if (arr[i] > 0) {
                        for(c in hand) {
                            if (c.name == Cards.CardName.values()[i+1]) {
                                listCurComb.add(c)
                                break
                            }
                        }
                        ch++
                        i++
                    } else {
                        if (j == 1) {
                            if (i != arr.size - 1 && arr[i+1] > 0) {
                                listCurComb.add(Card(Cards.CardName.Joker, Cards.Color.Joker))
                                scoreJ = Cards.CardName.values()[i+2].value
                                j--
                                ch++
                                i++
                            }else {
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

                score = handType.cards.sumByDouble {it.name.value}
                if (j == 0 && joker) {
                    score = score - Cards.CardName.Joker.value + scoreJ
                }
                handType.score = (score * comb.value)
            }
            Combination.ThreeOfAKind -> {
                arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

                j = 0
                if (joker) {
                    j = 1
                }
                tCard = Cards.CardName.N2
                for ((i, a) in arr.withIndex()) {
                    if (a == 3 || (a == 2 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) {
                        tCard = Cards.CardName.values()[i]
                        if(a == 2) j--
                    }
                }

                i = 0
                while (i < hand.size) {
                    if (hand[i].name == tCard || (hand[i].name == Cards.CardName.Joker && joker && j == 0)) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }

                handType.score = (handType.cards.sumByDouble {it.name.value} * comb.value)
            }
            Combination.TwoPair -> {
                arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }
                ch = 0
                j = false
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

                handType.score = (handType.cards.sumByDouble {it.name.value} * comb.value)
            }
            Combination.OnePair -> {
                arr = IntArray(Cards.CardName.values().size) { 0 }
                hand.forEach { arr[Cards.CardName.values().indexOf(it.name)]++ }

                j = 0
                if (joker) {
                    j = 1
                }
                tCard = Cards.CardName.N2
                for ((i, a) in arr.withIndex()) {
                    if (a == 2 || (a == 1 && joker && Cards.CardName.values()[i] == Cards.CardName.Ace)) {
                        tCard = Cards.CardName.values()[i]
                        if(a == 1) j--
                    }
                }

                i = 0
                while (i < hand.size) {
                    if (hand[i].name == tCard || (hand[i].name == Cards.CardName.Joker && joker && j == 0)) {
                        handType.cards.add(hand[i])
                        hand.remove(hand[i])
                    } else i++
                }

                handType.score = (handType.cards.sumByDouble {it.name.value} * comb.value)
            }
            else -> {
                hc = hand[0]
                hand.forEach { if (it.name.value > hc.name.value) hc = it}
                handType.cards.add(hc)
                hand.remove(hc)
                handType.score = hc.name.value
            }
        }
    }
}

class Hand:
    class HandType:
        def __init__(self, cards, comb=Combinations.Combination.HighCard, score=0.0):
            self.cards = cards
            self.comb = comb
            self.score = score

    hand = []  # mutableListOf<Card>()
    high = HandType([], Combinations.Combination.HighCard, 0.0)
    low = HandType([], Combinations.Combination.HighCard, 0.0)

    def addCardInHands(self, card: Card):
        self.hand.append(card)

    def findCombinationsAndDistribute(self):
        comb = Combinations()
        sorted(self.hand, key=lambda card: card.name)  # self.hand.sortBy { it.name.value }
        sorted(self.hand, key=lambda card: card.color)  # self.hand.sortBy { it.color }
        allCombOnHand = sorted(comb.checkHandComb(self.hand).sortedBy, key=lambda combinations: combinations.value)
        bestComb = allCombOnHand[-1]
        self.high.comb = bestComb
        comb.scoreHand(self.hand, self.high, bestComb)
        remainingCombs = sorted(comb.checkHandComb(self.hand), key=lambda combinations: combinations.value)
        if remainingCombs.__contains__(Combinations.Combination.OnePair):
            bestRemainingComb = Combinations.Combination.OnePair
        else:
            bestRemainingComb = Combinations.Combination.HighCard

        self.low.comb = bestRemainingComb
        comb.scoreHand(self.hand, self.low, bestRemainingComb)

        if self.low.cards.size != 2 and self.hand.__len__() != 0:
            randomCard = random.choice(self.hand)
            self.low.cards.append(randomCard)
            self.hand.remove(randomCard)

        self.high.cards.addAll(self.hand)
        self.hand.clear()
        return allCombOnHand

    def compareWithDealerHands(self, dealerHand):
        playerHighScore = self.high.score
        playerLowScore = self.low.score
        dealerHighScore = dealerHand.high.score
        dealerLowScore = dealerHand.low.score
        if playerHighScore > dealerHighScore and playerLowScore > dealerLowScore:
            return 1
        elif playerHighScore < dealerHighScore and playerLowScore > dealerLowScore:
            return 2
        elif playerHighScore > dealerHighScore and playerLowScore < dealerLowScore:
            return 2
        return 0

    def clearHand(self):
        self.hand.clear()
        self.high.cards.clear()
        self.high.score = 0.0
        self.low.cards.clear()
        self.low.score = 0.0

    def __str__(self):
        return "HIGH :: {} :: {}\n{}\nLOW :: {} :: {}\n{}\n\n".format(self.high.comb, self.high.score, self.high.cards,
                                                                      self.low.comb, self.low.score, self.low.cards)


class Dealer:
    def __init__(self, deck: Deck):
        self.deck = Deck

    def dealCards(self, hands: Hand):
        for i in range(1, 8):
            randInd = random.randint(0, self.deck.getListOfCards().__len__())
            randCard = self.deck.takeCard(randInd)
            hands.addCardInHands(randCard)
