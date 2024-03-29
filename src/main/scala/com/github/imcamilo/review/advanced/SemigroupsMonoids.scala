package com.github.imcamilo.review.advanced

/*
 These abstractions can be very very useful Many high-level constructs expressed as type classes, can help make our
 apis more general, more expressive and more concise at the same time
 */
object SemigroupsMonoids extends App {

  // Semigroup is very loosely defined as a set in our case a type plus a combination function
  // type + combinatino function
  // this combination function will take 2 elements of that type and
  // will produce a third value which correspond to that type as well

  trait Semigroup[T] {
    def combine(a: T, b: T): T
  }

  object Semigroup {
    def apply[T](implicit instance: Semigroup[T]): Semigroup[T] = instance
  }

  trait Monoid[T] extends Semigroup[T] {
    def empty: T // empty + x == x for all x in the type T //property the monoid need uphold
  }

  object Monoid {
    def apply[T](implicit instance: Monoid[T]): Monoid[T] = instance
  }

  object IntInstances {
    implicit def intMonoid: Monoid[Int] = new Monoid[Int] {
      override def empty: Int = 0
      override def combine(a: Int, b: Int): Int = a + b
    }
  }
  object StringInstances {
    implicit def stringMonoid: Monoid[String] = new Monoid[String] {
      override def empty: String = ""
      override def combine(a: String, b: String): String = a + b
    }
  }

  // now we can create instances of this semigroup which are applicable for some types that we want to support
  // semigroup for integers

  import IntInstances._ // cats.instances.int._ - cats.instances.int.given
  import StringInstances._
  val naturalIntSemigroup = Semigroup[Int]
  val naturalStringSemigroup = Semigroup[String]
  val naturalPort = naturalIntSemigroup.combine(8765, 1)
  val favLanguage = naturalStringSemigroup.combine("Scala ", "Rocks")

  println(naturalPort)
  println(favLanguage)

  // You want to expose the ability to collapse a list of integers to a number, a list of strings, etc
  def reduceInts(list: List[Int]): Int = list.sum // .reduce(_ + _)
  def reduceStrings(list: List[String]): String = list.reduce(_ + _)
  // every other types

  def reduceThings[T](list: List[T])(implicit semigroup: Semigroup[T]): T = list.reduce(semigroup.combine)
  val reduceThings1 = reduceThings(List(1, 2, 3, 4))
  val reduceThings2 = reduceThings(List("Hello ", "Scala", "!."))
  println(reduceThings1)
  println(reduceThings2)

  object SemigroupSyntax {
    implicit class EnrichmentT[T](val a: T) extends AnyVal {
      def |+|(b: T)(implicit semigroup: Semigroup[T]): T = semigroup.combine(a, b)
    }
  }
  import SemigroupSyntax._ // cats.syntax.semigroup.given
  val naturalPort2 = 8765 |+| 1
  def reduceCompact[T: Semigroup](list: List[T]): T = list.reduce(_ |+| _)

  println(naturalPort2)
  val naturalPort3 = reduceCompact(List(12, 3, 6435))
  val favPort = reduceCompact(List("The next ", "port is ", "6435"))
  println(naturalPort3)
  println(favPort)

}
