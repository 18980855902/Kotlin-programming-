import kotlin.random.Random

interface Role {//---------------------1 interface
val roleTitle: String;
    fun getEnemy() : String
}
interface Subscriber{
    fun update(dodged: Boolean, hp: Int, numberOfCards: Int){}
}
interface Publisher{
    fun subscribe(s: Subscriber)
    fun notifySubscribers(dodged: Boolean, hp: Int, numOfCards: Int)
    fun removeSubscriber(s: Subscriber)
}

class Monarch() : Publisher, Role{//-----------------------------2 装饰点
var subscribers = mutableListOf<Subscriber>()

    override fun subscribe(s: Subscriber) {
        subscribers.add(s)
    }

    override fun removeSubscriber(s: Subscriber) {
        subscribers.remove(s)
    }

    override fun notifySubscribers(dodged: Boolean, hp: Int, numOfCards: Int) {
        for(s in subscribers){
            s.update(dodged, hp, numOfCards)
        }
    }

    override val roleTitle = "Monarch"
    override fun getEnemy():String  = "NonMonarchFactory"
}//--------------------------------decorater
class Minister() : Role{
    override val roleTitle = "Minister"
    override fun getEnemy():String  = "NonMonarchFactory"
}
class Rebel() :  Role{
    var danger_level : Int = 0
    override val roleTitle = "Rebel"
    override fun getEnemy():String  = "MonarchFactory"
}
class Traitors() : Role{
    override val roleTitle = "Traitors"
    override fun getEnemy():String  = "Everyone"
}

interface Handler{
    fun setNext(h: Handler);
    fun handle(): Boolean;
}
interface Command{
    fun execute():Boolean{
        return Random.nextBoolean()==false
    }
}
interface Strategy {
    fun selectCardToDiscard()
    fun playNextCard(): Boolean
}
class BasicStrategy(val h: Hero) :Strategy{
    override fun selectCardToDiscard() {
        println("dummy message")
    }

    override fun playNextCard(): Boolean {
        if(h.canAttackAgain) {
            h.attack()
        }else return false
        h.canAttackAgain = false
        return true
    }

    fun playCards(){
        while(h.numOfCards!=0&&playNextCard()){
            playNextCard()
        }
    }

    fun discardCards(){
        while(h.hp<h.numOfCards){
            discardCards()
        }
    }

}

abstract class Hero(var b: Role) : Role by b, Command{//--------------------------3 item
    abstract val name: String;
    abstract var maxHP: Int;
    abstract var hp: Int;
    var numOfCards =4;
    open fun discardCards() {
        print("Current HP is " + hp + " number of cards is " + numOfCards)
        if(hp<numOfCards){
            val numtodiscard = numOfCards - hp
            println(", discarding " + numtodiscard + ", now have " + hp + ".\n")
            numOfCards = numOfCards - numtodiscard
        }else println(". No need to discard cards." + "\n")
    }
    fun drawCards() {
        println(name + "'s turn:")
        numOfCards = numOfCards + 2
        println("Drawing 2 cards.")
        println(name + " now has " + numOfCards + " cards.")
    }
    fun attack(){
        if(getEnemy() =="NonMonarchFactory"){
            println(" Rebel, then Traitors")
        } else if(getEnemy() =="MonarchFactory"){
            println(" Monarch, then Minister")
        }else println(" Rebel, then Monarch")
        numOfCards = numOfCards - 1
    }
    abstract fun templateMethod();
    open fun playCards() {
        print(name + " is " + roleTitle + ", spent 1 card to attack")
        attack()
    }
    open fun dodgeAttack() : Boolean{return false};
    open fun beingAttacked() {
        hp--
        println(name + " got attacked, he is unable to dodge attack, current hp is " + hp + ".")
    }
    var willabandon: Boolean = false;
    fun setCommand(){willabandon = true}
    fun executeCommand():Boolean{return execute()}
    var canAttackAgain: Boolean= true;
    lateinit var reference: String;
    fun setStrategy(){
        reference = "set reference"
    }
}
abstract class MonarchHero(b: Monarch) : Hero (b), Publisher by b{
    override var maxHP = 5;
    override var hp = maxHP;
}
abstract class WarriorHero(b: Role) : Hero (b), Subscriber{
    var danger_level : Int = 0
    override var maxHP = 4;
    override var hp = maxHP;
    override fun update(dodged: Boolean, hp: Int, numberOfCards: Int){
        if(dodged){
            danger_level = hp/2 + numberOfCards
        }else danger_level = hp/2 + numberOfCards + 2
        println(roleTitle + "estimates the danger Level as " + danger_level)
    }
}
abstract class AdvisorHero(b: Role) : Hero (b), Subscriber{
    var danger_level : Int = 0
    override var maxHP = 3;
    override var hp = maxHP;
    override fun update(dodged: Boolean, hp: Int, numberOfCards: Int){
        if(dodged){
            danger_level = hp/2 + numberOfCards
        }else danger_level = hp/2 + numberOfCards + 2
        println(roleTitle + "estimates the danger Level as " + danger_level)
    }
}
abstract class WeiHero(b: Role): Hero(b), Handler, Subscriber{
    open var nexthandler : Handler? = null//-----------------------------nullable
    var danger_level : Int = 0
    override fun setNext(h: Handler) {
        nexthandler = h;
    }
    override fun handle(): Boolean {
        if(hp==0){
            if(nexthandler==null){return false}
            else if (nexthandler?.handle() == true) {
                return true
            }
        }

        if(numOfCards!=0&&roleTitle!="Rebel"){
            numOfCards--
            println(name+ " spent 1 card to help his lord to dodge.")
            return true
        }else {
            println(name + " doesn't want to help.")
            if (nexthandler != null) {
                if (nexthandler?.handle() == true) {
                    return true
                }
            }
            return false
        }
    }
    override fun update(dodged: Boolean, hp: Int, numberOfCards: Int){
        if(dodged){
            danger_level = hp/2 + numberOfCards
        }else danger_level = hp/2 + numberOfCards + 2
        println(roleTitle + "estimates the danger Level as " + danger_level)
    }
}

class Cao_Cao(b: Monarch) : MonarchHero(b) {//1

    override var maxHP: Int =5
    override var hp: Int =5
    override var name = "Cao Cao"
    var nexthandler: Handler? = null

    override fun dodgeAttack(): Boolean {
        if(nexthandler?.handle() == true) {
            return true
        }
        println("No one can help")
        return false
    }
    override fun beingAttacked() {
        println(name + " got attacked.")
        if(!dodgeAttack()){
            hp--
            println(name + " is unable to dodge attack, current hp is " + hp + ".")
        }
        else println(name + " dodged attack, current hp is " + hp + ".")
        if (b is Publisher) {
            (b as Publisher).notifySubscribers(true , hp, numOfCards)
        }
    }
    fun setNext(h: Handler){
        nexthandler = h
    }
    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Liu_Bei(b: Monarch) : MonarchHero(b) {//2

    override var name = "Liu Bei"

    override fun beingAttacked() {
        hp--
        println(name + " got attacked, he is unable to dodge attack, current hp is " + hp + ".")
        if (b is Publisher) {
            (b as Publisher).notifySubscribers(false , hp, numOfCards)
        }
    }
    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Sun_Quan(b: Monarch) : MonarchHero(b) {//3

    override var name = "Sun Quan"

    override fun beingAttacked() {
        hp--
        println(name + " got attacked, he is unable to dodge attack, current hp is " + hp + ".")
        if (b is Publisher) {
            (b as Publisher).notifySubscribers(false , hp, numOfCards)
        }
    }
    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Zhang_Fei(b: Role) : WarriorHero(b) {//4

    override var name = "Zhang Fei"

    override fun playCards() {
        print(name + " is " + roleTitle + ", spent 1 card to attack")
        while(numOfCards!=0) {
            print(name + "is " + roleTitle + ", spent 1 card to attack")
            attack()
        }
    }
    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Zhou_Yu(b: Role) : AdvisorHero(b) {//5

    override var name = "Zhou Yu"

    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Diao_Chan(b: Role) : AdvisorHero(b) {//7

    override var name = "Diao Chan"

    override fun discardCards() {
        print("Current HP is " + hp + " number of cards is " + numOfCards)
        if(hp<numOfCards){
            val numtodiscard = numOfCards - hp
            println(", discarding " + numtodiscard + ", now have " + hp + ".")
            numOfCards = numOfCards - numtodiscard
        }else println(". No need to discard cards.")
        numOfCards = numOfCards +1;
        println("I can draw one more card, now I have " + numOfCards + "\n")
    }
    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Si_maYi(b: Role) : WeiHero(b) {//7
    override var hp: Int = 3
    override var maxHP: Int =3
    override var name = "Si maYi"

    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}
class Xu_Chu(b: Role) : WeiHero(b) {//8

    override var hp: Int = 4
    override var maxHP: Int =4
    override var name = "Xu Chu"

    override fun templateMethod() {
        drawCards()
        playCards()
        discardCards()
    }
}

interface GameObjectFactory{
    fun getRandomRole(): Role;
    fun getRandomHero(): Hero;
}
object MonarchFactory: GameObjectFactory {
    lateinit var monarch : Hero//---------------------------公用

    override fun getRandomRole(): Monarch {
        return Monarch()
    }

    override fun getRandomHero(): Hero{
        var x: Int = Random.nextInt(0, 3)//--------------------------------------前闭后开
        monarch = when(x){
            1->Liu_Bei(getRandomRole())
            2->Cao_Cao(getRandomRole())
            else ->Sun_Quan(getRandomRole())
        }
        return monarch
    }
}object NonMonarchFactory: GameObjectFactory{
    override fun getRandomRole(): Role{
        var x: Int = Random.nextInt(0, 3)//前闭后开--------------------------------------
        when(x){
            1->return Minister()
            2->return Rebel()
            else->return Traitors()
        }
    }

    override fun getRandomHero(): Hero{
        lateinit var hero : Hero
        var x: Int = Random.nextInt(0, 5)//前闭后开--------------------------------------
        hero = when(x){
            1-> Zhang_Fei(getRandomRole())
            2-> Zhou_Yu(getRandomRole())
            3-> Diao_Chan(getRandomRole())
            4-> Si_maYi(getRandomRole())
            else-> Xu_Chu(getRandomRole())
        }
        return hero
    }
}
object heroes{
    var heroes = mutableListOf<Hero>()
    fun find(values: MutableList<Hero>, hero: Hero): Boolean {
        return values.any { it.name.equals(hero.name)}
    }
    var setmonarch = MonarchFactory.getRandomHero()
    var randomrole = NonMonarchFactory.getRandomHero()
    var lastWeiHero : WeiHero? = null

    init {
        heroes.add(setmonarch)
        heroes[0].setCommand()

        for (i in 1..5) {
            randomrole = NonMonarchFactory.getRandomHero()

            while (find(heroes, randomrole)) {
                randomrole = NonMonarchFactory.getRandomHero()
            }

            if(randomrole.roleTitle=="Traitors"|| randomrole.roleTitle=="Minister"){
                (heroes[0].b as Monarch).subscribe(randomrole as Subscriber)
            }

            heroes.add(randomrole)

            if(MonarchFactory.monarch is Cao_Cao){
                if (randomrole is WeiHero) {
                    if(lastWeiHero==null) {
                        (heroes[0] as Cao_Cao).setNext(randomrole as Handler);
                        lastWeiHero = randomrole as WeiHero
                    }else {
                        lastWeiHero?.setNext(randomrole as Handler);
                        lastWeiHero = randomrole as WeiHero
                    }
                }
            }
        }
    }
}

fun main() {
    fun check(value: MutableList<Hero>):Boolean {
        for(x in value){
            if(x.hp>0){
                return true
            }
        }
        return false
    }
    println(heroes.heroes[0].name + " being placed the Abandon card.")
    while(check(heroes.heroes)){
        for (x in heroes.heroes) {
            if(x.hp !=0){
                if(x.willabandon==true){
                    if(x.executeCommand()==true){
                        x.beingAttacked()
                        if (x.hp == 0) {
                            println(x.name + "is dead" + "\n")
                            continue
                        }
                        x.drawCards()
                        println(x.name + " 's round got abandoned.")
                        x.discardCards()
                        continue
                    }else x.beingAttacked()
                    if (x.hp == 0) {
                        println(x.name + "is dead" + "\n")
                        continue
                    }
                    x.drawCards()
                    println("Abandon card voided.")
                    x.playCards()
                    x.discardCards()
                    continue
                }

                x.beingAttacked()
                if (x.hp == 0) {
                    println(x.name + "is dead" + "\n")
                    continue
                }
                x.templateMethod()
            }
        }
    }
    println("Game is over.")

}


