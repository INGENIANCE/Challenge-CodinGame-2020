import math._
import scala.util._
import scala.io.StdIn._
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Map
import scala.util.Random
import scala.util.control.Breaks._

class Case(var x : Int, var y: Int){
    def equals(other: Case): Boolean = {
        if(other.x == this.x && other.y == this.y){
            return true
        } else {return false}
    }

    def sameCoordinates(x: Int, y: Int): Boolean = {
        //Console.err.println("this.x, this.y: " + this.x + " " + this.y)
        //Console.err.println("x, y: " + x + " " + y)
        if(this.x == x && this.y == y){
            return true
        } else {return false}
    }

    def carré(x: Int) = x * x

    def distance(other: Case): Int ={
        return (abs(this.x - other.x) + abs(this.y - other.y)).toInt
    }

    def vectorTo(other: Case): Vecteur={
        return new Vecteur(other.x - this.x, other.y - this.y)
    }

    override def toString() : String ={
        return "Case: x = " + this.x + ", y = " + this.y
    }
}

class Vecteur(var x :Int, var y: Int){}

//class Pastille(var x : Int, var y: Int, var value: Int){}

class Grille(val grille : ArrayBuffer[String], width: Int, height: Int){
    def isWall(caseToBeAnalysed : Case): Boolean ={
        if(caseToBeAnalysed.x < 0 || caseToBeAnalysed.x >= width || caseToBeAnalysed.y < 0 || caseToBeAnalysed.y >= height ){
            return true //Out of bounds
        }
        if(grille(caseToBeAnalysed.y).charAt(caseToBeAnalysed.x) == '#'){
            return true
        } else {return false}
    }
}

class Objectif(var direction: Case, var label: String){}

class Pac(var id : Int,
     var mine : Boolean,
     var x : Int,
     var y : Int,
     var speedTurnsLeft : Int,
     var abilityCooldown :Int,
     var lastPosition: Case,
     var typeId: String
     )
     {
        override def toString() : String = {
            return "Pac n° " + this.id + " located at x=" + this.x +"; y=" + this.y
        }
     }

/**
 * Grab the pellets as fast as you can!
 **/
object Player extends App {
    // width: size of the grid
    // height: top left corner is (x=0, y=0)
    val grilleTemp: ArrayBuffer[String] = new ArrayBuffer[String]()
    val Array(width, height) = (readLine split " ").map (_.toInt)
    for(i <- 0 until height) {
        val row = readLine // one line of the grid: space " " is floor, pound "#" is wall
        grilleTemp += row
    }
    val grille : Grille = new Grille(grilleTemp, width, height)

    val dernierePosition: Map[Int, Case] = Map()
    val objectifs: Map[Int, Objectif] = Map()

    def oppositeType(typeId: String): String ={
        if(typeId == "PAPER"){return "SCISSORS"}
        else if(typeId == "SCISSORS"){return "ROCK"}
        else if(typeId == "ROCK"){return "PAPER"}
        else return "ERREUR"
    }

    def printObjectifs(objectifs: Map[Int, Objectif]) = {
        for (pacId <- objectifs.keys){
            Console.err.println("Le pac n°" + pacId + " va vers la case: " + objectifs.get(pacId).get.direction.toString)
        }
    }

    def getAdjacentCase(caseToGetAdjacent : Case, pastillesNormales : ArrayBuffer[Case], grille: Grille):Case ={
        Console.err.println("Finding adjacent case to: " + caseToGetAdjacent.toString())
        val possibleResults = new ArrayBuffer[Case]()
        possibleResults += new Case(caseToGetAdjacent.x +1, caseToGetAdjacent.y)
        possibleResults += new Case(caseToGetAdjacent.x -1, caseToGetAdjacent.y)
        possibleResults += new Case(caseToGetAdjacent.x, caseToGetAdjacent.y+1)
        possibleResults += new Case(caseToGetAdjacent.x, caseToGetAdjacent.y-1)
        for(possibleResult <- possibleResults){
            if (possibleResult != null && grille.isWall(possibleResult)){possibleResults -= possibleResult}
        }
        if(possibleResults.isEmpty){return null}
        else {
            var caseWithPastille : Option[Case] = None
            for(pastille : Case <- pastillesNormales){
                for(possibleResult : Case <- possibleResults){
                    if(pastille != null && possibleResult != null && pastille.equals(possibleResult)){caseWithPastille = Some(possibleResult)}
                }
            }
            if(caseWithPastille.isDefined){
                Console.err.println("Found case: " + caseWithPastille.toString)
                caseWithPastille.get
            }
            else{
                for(possibleResult <- possibleResults){
                    if(possibleResult != null){
                        "Found case: " + caseWithPastille.toString
                        return possibleResult
                    }
                }
                return null
            }
        }
    }

    def getAdjacentCaseButNot(caseToGetAdjacent : Case, pastillesNormales : ArrayBuffer[Case], grille: Grille, caseAEviter: Case):Case ={
        Console.err.println("Finding adjacent case to: " + caseToGetAdjacent.toString())
        val possibleResults = new ArrayBuffer[Case]()
        possibleResults += new Case(caseToGetAdjacent.x +1, caseToGetAdjacent.y)
        possibleResults += new Case(caseToGetAdjacent.x -1, caseToGetAdjacent.y)
        possibleResults += new Case(caseToGetAdjacent.x, caseToGetAdjacent.y+1)
        possibleResults += new Case(caseToGetAdjacent.x, caseToGetAdjacent.y-1)
        for(possibleResult <- possibleResults){
            if (possibleResult != null && grille.isWall(possibleResult)){possibleResults -= possibleResult}
            if (possibleResult != null && possibleResult.equals(caseAEviter)){possibleResults -= possibleResult}
        }
        if(possibleResults.isEmpty){return null}
        else {
            var caseWithPastille : Option[Case] = None
            for(pastille : Case <- pastillesNormales){
                for(possibleResult : Case <- possibleResults){
                    if(pastille != null && possibleResult != null && pastille.equals(possibleResult)){caseWithPastille = Some(possibleResult)}
                }
            }
            if(caseWithPastille.isDefined){
                Console.err.println("Found case: " + caseWithPastille.toString)
                caseWithPastille.get
            }
            else{
                for(possibleResult <- possibleResults){
                    if(possibleResult != null){
                        "Found case: " + caseWithPastille.toString
                        return possibleResult
                    }
                }
                return null
            }
        }
    }

    // game loop
    while(true) {
        val Array(myScore, opponentScore) = (readLine split " ").map (_.toInt)
        val visiblePacCount = readLine.toInt // all your pacs and enemy pacs in sight

        val enemyPacs: Map[Int, Pac] = Map()
        val myPacs: Map[Int, Pac] = Map()
        for(i <- 0 until visiblePacCount) {
            // pacId: pac number (unique within a team)
            // mine: true if this pac is yours
            // x: position in the grid
            // y: position in the grid
            // typeId: unused in wood leagues
            // speedTurnsLeft: unused in wood leagues
            // abilityCooldown: unused in wood leagues
            val Array(_pacId, _mine, _x, _y, _typeId, _speedTurnsLeft, _abilityCooldown) = readLine split " "
            val pacId = _pacId.toInt
            val mine = _mine.toInt != 0
            val x = _x.toInt
            val y = _y.toInt
            val speedTurnsLeft = _speedTurnsLeft.toInt
            val abilityCooldown = _abilityCooldown.toInt
            val typeId = _typeId
            if(mine){
                myPacs += (pacId ->(new Pac(pacId, mine, x, y, speedTurnsLeft, abilityCooldown, new Case(-1, 0), typeId)))
            } else {
                enemyPacs += (pacId ->(new Pac(pacId, mine, x, y, speedTurnsLeft, abilityCooldown, new Case(-1, 0), typeId)))
            }
        }
        val visiblePelletCount = readLine.toInt // all pellets in sight

        //Data des pastilles
        var superPastilles = new ArrayBuffer[Case]()
        var pastillesNormales = new ArrayBuffer[Case]()
        val idlePacs = new ArrayBuffer[Int]()
        
        for(i <- 0 until visiblePelletCount) {
            // value: amount of points this pellet is worth
            val Array(x, y, value) = (readLine split " ").map (_.toInt)
            if(value == 10){
                superPastilles.append(new Case(x, y))
            } else {
                pastillesNormales.append(new Case(x, y))
            }
        }

        //Here we make sure that all gums already chased by a pac are not attributed to other pacs
        // We also reset the goal of pacs when their goal disapeared
        for(pacId: Int <- objectifs.keys){
            if(objectifs.get(pacId).isDefined){
                val objectif: Objectif = objectifs.get(pacId).get
                if(objectif.label == "getSuperPastille"){
                    var superPastilleDisapeared : Boolean = true
                    for(pastille <- superPastilles){
                        if (pastille != null && pastille.equals(objectif.direction)){
                            superPastilles -= pastille //This goal is taken
                            superPastilleDisapeared = false
                        }
                    }
                    if (superPastilleDisapeared){
                        //The supa gum disapeared!
                        objectifs -= pacId //Let us change our goal
                        Console.err.println("The supa gum disapeared!")
                    }
                } else if(objectif.label == "getPastille"){
                    var pastilleDisapeared : Boolean = true
                    for(pastille <- pastillesNormales){
                        if (pastille != null && pastille.equals(objectif.direction)){
                            pastillesNormales -= pastille //This goal is taken
                            pastilleDisapeared = false
                        }
                    }
                    if (pastilleDisapeared){
                        //The gum disapeared!
                        objectifs -= pacId //Let us change our goal
                        Console.err.println("The gum disapeared!")
                    }
                }
            }
        }

        //Check that no one is stuck or has reached its goal 
        for(pacId <- myPacs.keys){
            if(dernierePosition.get(pacId).isDefined){
                val pac : Pac = myPacs.get(pacId).get
                val lastPosition: Case = dernierePosition.get(pacId).get
                if(lastPosition.equals(new Case(pac.x, pac.y))){
                    objectifs -= pacId //Change our goal 
                    breakable{
                        while(true){
                            val firstRandom: Int = Random.nextInt(this.width)
                            val secondRandom: Int = Random.nextInt(this.height)
                            val obj : Case = new Case(firstRandom, secondRandom)
                            if(!grille.isWall(obj)){
                                System.err.println("Pac " + pacId + "is headed now for: " + obj.x + " " + obj.y)
                                objectifs += (pacId -> new Objectif(obj, "wanderer"))
                                break
                            }
                        }
                        throw new Exception("How did I end up here?")
                    }  
                } else if(objectifs.get(pacId).isDefined && objectifs.get(pacId).get.direction.equals(new Case(pac.x, pac.y))){
                    idlePacs += pacId
                }
            }
        }

        printObjectifs(objectifs)

        for (pac <- myPacs.values){
            if (objectifs.get(pac.id) == None){
                idlePacs += pac.id
            }
        }

        System.err.println("Super pastilles:")
        for (superGum: Case <- superPastilles){
            System.err.println("-> " + superGum.x + " " + superGum.y)
            var min: Int = 500000
            var closestPac: Option[Pac] = None
            for(pacId <- idlePacs){
                var pac : Pac = myPacs.get(pacId).get
                System.err.println(pac)
                var pacLocation: Case = new Case(pac.x, pac.y)
                if(superGum.distance(pacLocation)< min){
                    closestPac = Some(pac)
                    min = superGum.distance(pacLocation)
                }
            }
            if(closestPac.isDefined){
                objectifs += (closestPac.get.id -> new Objectif(superGum, "getSuperPastille"))
                System.err.println("Donné au pac: " + closestPac.get.id)
                idlePacs -= closestPac.get.id
                }
        }

        val notIdlePacs = new ArrayBuffer[Int]()

        for (pacId <- objectifs.keys){
            notIdlePacs += pacId
        }

        for(pacId <- idlePacs){
            breakable{
                if(!pastillesNormales.isEmpty){
                    for(pastille <- pastillesNormales){
                        Console.err.println(pastille)
                        Console.err.println("Pac " + myPacs.get(pacId).get.toString)
                        Console.err.println(pastille.distance(new Case( myPacs.get(pacId).get.x, myPacs.get(pacId).get.y)))
                        if(pastille.distance(new Case( myPacs.get(pacId).get.x,  myPacs.get(pacId).get.y)) < 3){
                            Console.err.println("Attributed " + pastille.toString + " to " + pacId)
                            pastillesNormales -= pastille // This goal is now taken 
                            objectifs += (pacId -> new Objectif(pastille, "getPastille"))
                            notIdlePacs += pacId
                            break
                        }
                    }
                }
            }
        }

        for(pac <- notIdlePacs){
            idlePacs -= pac
        }

        for(pac <- notIdlePacs){
            for(pastille <- pastillesNormales){
                //Console.err.println(pastille)
                //Console.err.println("Pac " + myPacs.get(pacId).get.toString)
                ///Console.err.println(pastille.distance(new Case( myPacs.get(pacId).get.x, myPacs.get(pacId).get.y)))
                if(pastille != null && myPacs.get(pac).isDefined && objectifs.get(pac).isDefined && pastille.distance(new Case(myPacs.get(pac).get.x,  myPacs.get(pac).get.y)) == 1){
                    if(objectifs.get(pac).get.label != "getSuperPastille") {
                        if(myPacs.get(pac).get.speedTurnsLeft != 0){
                            Console.err.println("Pac n°" + pac + " is in super speed and need to move 2 cases at once")
                            var direction : Case = getAdjacentCase(pastille, pastillesNormales, grille)
                            objectifs -= pac
                            objectifs += (pac -> new Objectif(direction, "getPastille"))
                        } else {
                            Console.err.println("Attributed " + pastille.toString + " to " + pac)
                            pastillesNormales -= pastille // This goal is now taken 
                            objectifs -= pac
                            objectifs += (pac -> new Objectif(pastille, "getPastille"))
                        }
                    } 
                }
            }
        }

        for (pac <- idlePacs){
            breakable{
                while(true){
                    val firstRandom: Int = Random.nextInt(this.width)
                    val secondRandom: Int = Random.nextInt(this.height)
                    val obj : Case = new Case(firstRandom, secondRandom)
                    if(!grille.isWall(obj)){
                        System.err.println("Pac " + pac + "is headed now for: " + obj.x + " " + obj.y)
                        objectifs += (pac -> new Objectif(obj, "wanderer"))
                        idlePacs -= pac
                        break
                    }
                }
                throw new Exception("How did I end up here?")
            }  
        }


        val enemyNear: Map[Int, Int] = Map()
        val transformation: Map[Int, String] = Map()
        //Abilities
        Console.err.println("ENEMIS")
        for (enemyPac <- enemyPacs.values){
            if(enemyPac != null){
                Console.err.println("CONTACT")
                val enemyLocation : Case = new Case(enemyPac.x, enemyPac.y)
                for (pacId <- myPacs.keys){
                    Console.err.println("Enemis à " + enemyLocation.distance(new Case(myPacs.get(pacId).get.x, myPacs.get(pacId).get.y)) + " cases")
                    if(enemyLocation.distance(new Case(myPacs.get(pacId).get.x, myPacs.get(pacId).get.y)) < 3){
                        Console.err.println("I, pac n° " + pacId + " should turn into " + oppositeType(enemyPac.typeId.toString))
                        transformation += (pacId -> oppositeType(enemyPac.typeId.toString))
                    }
                    if(enemyLocation.distance(new Case(myPacs.get(pacId).get.x, myPacs.get(pacId).get.y)) < 8){
                        Console.err.println("I, pac n° " + pacId + " am near an enemy")
                        enemyNear += (pacId -> enemyPac.id)
                    }
                }
            }
        }
  
        
        var commande :String = ""

        for(pacId: Int <- objectifs.keys){
            if(myPacs.get(pacId).isDefined){
                if(!transformation.get(pacId).isDefined){
                    val pac: Pac = myPacs.get(pacId).get
                    val position : Case = new Case(pac.x, pac.y)
                    val direction : Case = objectifs.get(pac.id).get.direction
                    if(myPacs.get(pacId).get.abilityCooldown == 0 && !enemyNear.get(pacId).isDefined){
                        commande = commande + "SPEED " + pac.id + " " + direction.x + " " + direction.y + "|"
                    } else {
                        commande = commande + "MOVE " + pac.id + " " + direction.x + " " + direction.y + "|"
                        dernierePosition += (pacId -> new Case(myPacs.get(pacId).get.x, myPacs.get(pacId).get.y))
                    }
                } else {
                    if(transformation.get(pacId).get.equals(myPacs.get(pacId).get.typeId)){
                        Console.err.println("I am already the right type")
                        val pac: Pac = myPacs.get(pacId).get
                        val direction : Case = objectifs.get(pac.id).get.direction
                        commande = commande + "MOVE " + pac.id + " " + direction.x + " " + direction.y + "|"
                        transformation -= pacId
                        dernierePosition += (pacId -> new Case(myPacs.get(pacId).get.x, myPacs.get(pacId).get.y))
                    } else {
                        if(myPacs.get(pacId).get.abilityCooldown != 0){
                            //RUN
                            Console.err.println("Pac n°: " + pacId + ": Should we run, flee?")
                            var enemyPac : Pac = enemyPacs.get(enemyNear.get(pacId).get).get
                            var enemyPacPosition : Case = new Case(enemyPac.x, enemyPac.y)
                            var myPosition : Case = new Case(myPacs.get(pacId).get.x, myPacs.get(pacId).get.y)

                            val fuitesPossibles: ArrayBuffer[Case] = new ArrayBuffer[Case]()
                            fuitesPossibles += new Case(myPosition.x +1, myPosition.y)
                            fuitesPossibles += new Case(myPosition.x -1, myPosition.y)
                            fuitesPossibles += new Case(myPosition.x, myPosition.y+1)
                            fuitesPossibles += new Case(myPosition.x, myPosition.y-1)
                            
                            breakable{
                                for(fuite <- fuitesPossibles) {
                                    val distanceToEnemy : Int = myPosition.distance(enemyPacPosition)
                                    if(!grille.isWall(fuite) && !fuite.equals(enemyPacPosition) && (fuite.distance(enemyPacPosition) > distanceToEnemy)){
                                        Console.err.println("Pac n°: " + pacId + "I need to flee to: " + fuite.toString)
                                        if(myPacs.get(pacId).get.speedTurnsLeft != 0){
                                            Console.err.println("Pac n°: " + pacId + "I flee FULL SPEED")
                                            var direction : Case = getAdjacentCaseButNot(fuite, pastillesNormales, grille, myPosition)
                                            if(direction == null){commande = commande + "MOVE " + pacId + " " + fuite.x + " " + fuite.y + "|"}
                                            else{commande = commande + "MOVE " + pacId + " " + direction.x + " " + direction.y + "|"}
                                            break
                                        } else {
                                             commande = commande + "MOVE " + pacId + " " + fuite.x + " " + fuite.y + "|"
                                            break
                                        }
                                    }
                                }
                            }
                            
                        } else {
                            commande = commande + "SWITCH " + pacId + " " + transformation.get(pacId).get + "|"
                            transformation -= pacId
                        }
                    }
                }
            }
        }

        // Write an action using println
        // To debug: Console.err.println("Debug messages...")
        
        println(commande) 
    }
}