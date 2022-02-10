package com.github.imcamilo.typesystem

object Variance extends App {

  trait Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  /*
  what is variance?
  problem of "inheritance" -> "" 'cause is the problem of type substitution of generics
  */

  //should a cage cat "inherit" from another cage animal?
  class Cage[C]

  //yes - covariance
  class CCage[+C] //covariance
  val ccage: CCage[Animal] = new CCage[Cat]

  //no - invariance
  class ICage[C] //invariance - i cant replace any type with cage with any other type of cage
  //val badICage: ICage[Animal] = new ICage[Cat]
  val icage: ICage[Animal] = new ICage[Animal]

  //hell no - opposite - contravariance
  class XCage[-C]
  val contraCage: XCage[Cat] = new XCage[Animal]

  //

  class InvariantCage[C](val animal: C) //invariant

  //covariant positions
  class CovariantCage[T](val animal: T) //COVARIANT POSITION

  /*
  Contravariant type C occurs in covariant position in type C of value animal
  class ContraVariantCage[-C](val animal: C)
  this would allow:
    val contraCage: XCage[Animal] = new XCage[Cat](new Crocodile) //this would be wrong for the types.

  Covariant type C occurs in contravariant position in type C of value animal
  class CovariantVariableCage[+C](var animal: C) //types of vars are in CONTRAVARIANT POSITION
  this would allow:
    val covariantCage: CCage[Animal] = new CCage[Cat](new Cat)
    covariantCage.animal = new Crocodile //this would be wrong for the types.


  Contravariant type C occurs in covariant position in type C of value animal
  class ContravariantVariableCage[-C](var animal: C) //also in COVARIANT POSITION
  this would allow:
    val covariantCage: XCage[Cat] = new CCage[Animal](new Crocodile)
  */

  class InvariantVariableCage[T](val animal: T) //OK

  //as we can saw, covariant and contravariance posititions are some compiler restrictions

  /*
  this wont compile
    trait AnotherCovariantCage[+C] {
      def addAnimal(animal: C) //contravariant position
    }
  this would be allow:
    val ccage: CCage[Animal] = new CCage[Cat]
    ccage.addAnimal(new Dog)
   */

  class AnotherContravariantCage[-C] {
    def addAnimal(animal: C) = true
  }
  val anotherContravariantCage: AnotherContravariantCage[Cat] = new AnotherContravariantCage[Animal]
  anotherContravariantCage.addAnimal(new Cat)
  class Misifu extends Cat
  anotherContravariantCage.addAnimal(new Misifu)

  class MyList[+C] {
    //using a supertype of C
    def add[B >: C](element: B): MyList[B] = new MyList[B] //widening the type
  }
  val emptyList = new MyList[Misifu]
  val animals = emptyList.add(new Misifu)
  val moreAnimals = animals.add(new Cat)
  val eventMoreAnimnals = moreAnimals.add(new Dog)

  //METHOD ARGUMENTS ARE IN CONTRAVARIANT POSITION

}
