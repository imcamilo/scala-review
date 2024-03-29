package com.github.imcamilo.review.advanced

import scala.annotation.tailrec

trait IMSet[A] extends (A => Boolean) {
  def apply(a: A): Boolean = contains(a)
  def contains(a: A): Boolean
  def +(elem: A): IMSet[A]
  def ++(anotherSet: IMSet[A]): IMSet[A] //union
  def map[B](f: A => B): IMSet[B]
  def flatMap[B](f: A => IMSet[B]): IMSet[B]
  def filter(predicate: A => Boolean): IMSet[A]
  def foreach(f: A => Unit): Unit
  def -(elem: A): IMSet[A]
  def --(anotherSet: IMSet[A]): IMSet[A] //difference
  def &(anotherSet: IMSet[A]): IMSet[A] //intersection
  def unary_! : IMSet[A] //unary, negation -> ex: .filter(!anotherSet)
}

class EmptySet[A] extends IMSet[A] {
  def contains(a: A): Boolean = false
  def +(elem: A): IMSet[A] = new NonEmptySet[A](elem, this)
  def ++(anotherSet: IMSet[A]): IMSet[A] = anotherSet
  def map[B](f: A => B): IMSet[B] = new EmptySet[B]
  def flatMap[B](f: A => IMSet[B]): IMSet[B] = new EmptySet[B]
  def filter(predicate: A => Boolean): IMSet[A] = this
  def foreach(f: A => Unit): Unit = ()
  def -(elem: A): IMSet[A] = this
  def --(anotherSet: IMSet[A]): IMSet[A] = this
  def &(anotherSet: IMSet[A]): IMSet[A] = this
  def unary_! : IMSet[A] = new PropertyBaseSet[A](_ => true)
}

class NonEmptySet[A](head: A, tail: IMSet[A]) extends IMSet[A] {
  def contains(elem: A): Boolean = elem == head || tail.contains(elem)
  def +(elem: A): IMSet[A] = if (this contains elem) this else new NonEmptySet[A](elem, this)
  /**
   * how it works, by recursion and polimorfism
   * ex.
   * [1,2,3] ++ [4,5] this would be
   * tail of the first
   * [2, 3] ++ [4, 5] + 1 =
   * [3] ++ [4, 5] + 1 + 2 =
   * [] ++ [4, 5] + 1 + 2 + 3 =
   * [4, 5] + 1 + 2 + 3 = [4, 5, 1, 2, 3]
   *
   * @param anotherSet
   * @return
   */
  def ++(anotherSet: IMSet[A]): IMSet[A] = tail ++ anotherSet + head
  def map[B](f: A => B): IMSet[B] = tail.map(f) + f(head)
  def flatMap[B](f: A => IMSet[B]): IMSet[B] = tail.flatMap(f) ++ f(head)
  def filter(predicate: A => Boolean): IMSet[A] = {
    val filteredTail = tail.filter(predicate)
    if (predicate(head)) filteredTail + head else filteredTail
  }
  def foreach(f: A => Unit): Unit = {
    f(head)
    tail.foreach(f)
  }
  def -(elem: A): IMSet[A] = if (head == elem) tail else tail - elem + head
  def --(anotherSet: IMSet[A]): IMSet[A] = filter(a => !anotherSet(a)) //filter(a => !anotherSet.contains(a))
  def &(anotherSet: IMSet[A]): IMSet[A] = filter(anotherSet) //filter(a => anotherSet(a)) //filter(a => anotherSet.contains(a)) //intersection = filtering!
  def unary_! : IMSet[A] = new PropertyBaseSet[A](a => !this.contains(a))
}

//all elements of type A which satisfy a property
//{{ x in A | property(x) }
class PropertyBaseSet[A](property: A => Boolean) extends IMSet[A] {
  def contains(elem: A): Boolean = property(elem)
  // { x in A | property(x) } + element  = { x in A | property(x) || x == element}
  def +(elem: A): IMSet[A] = new PropertyBaseSet[A](a => property(a) || a == elem)
  // { x in A | property(x) } ++ set => { x in A || property(x) || set contains x }
  def ++(anotherSet: IMSet[A]): IMSet[A] = new PropertyBaseSet[A](a => property(a) || anotherSet(a))
  //all integers (_ % 3) => [0 1 2]
  def map[B](f: A => B): IMSet[B] = politelyFail
  def flatMap[B](f: A => IMSet[B]): IMSet[B] = politelyFail
  def filter(predicate: A => Boolean): IMSet[A] = new PropertyBaseSet[A](a => property(a) && predicate(a))
  def foreach(f: A => Unit): Unit = politelyFail
  def -(elem: A): IMSet[A] = filter(a => a != elem)
  def --(anotherSet: IMSet[A]): IMSet[A] = filter(!anotherSet)
  def &(anotherSet: IMSet[A]): IMSet[A] = filter(anotherSet)
  def unary_! : IMSet[A] = new PropertyBaseSet[A](a => !property(a))
  def politelyFail = throw new IllegalArgumentException("Really deep rabbit hole!")
}

object IMSet {
  /**
   * val imset = IMSet(1, 2, 3) = buildSet(seq(1,2,3), [])
   * = buildSet(seq(2,3), [] + 1)
   * = buildSet(seq(3), [1] + 2)
   * = buildSet(seq(), [1, 2] +3)
   * = [1,2,3]
   */
  def apply[A](values: A*): IMSet[A] = {
    @tailrec
    def buildSet(valSeq: Seq[A], acc: IMSet[A]): IMSet[A] = {
      if (valSeq.isEmpty) acc
      else buildSet(valSeq.tail, acc + valSeq.head)
    }

    buildSet(values.toSeq, new EmptySet[A])
  }
}

//exercise implement a functional set
object IMSetPlayground extends App {
  val s = IMSet(1, 2, 3, 4)
  s + 5 ++ IMSet(0, -1, -2) + 3 map (_ * 10) foreach println
  println("------- 1")
  s flatMap (a => IMSet(a, a * 10)) foreach println
  println("------- 2")
  s filter (_ % 3 == 0) foreach println
  println("------- 3")
  val x = IMSet(1, 2, 775, 32)
  s.&(x).foreach(println)
  println("------- 3")
  x.--(s).foreach(println)
  println("------- wtf 4")
  val negative = !s //s.unary_! = all the naturals not equal to 1, 2, 3, 4
  println(negative(2))
  println(negative(5))
  val negativeEven = negative.filter(_ % 2 == 0)
  println(negativeEven(5))
  val negativeEven5 = negativeEven + 5 //all the even numbers > 4  + 5
  println(negativeEven5(5))
}