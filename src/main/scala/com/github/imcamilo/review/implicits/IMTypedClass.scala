package com.github.imcamilo.review.implicits

trait IMTypedClass[C] {
  def action(a: C): String
}

object IMTypedClass {
  def apply[C](implicit instance: IMTypedClass[C]): IMTypedClass[C] = instance
}
