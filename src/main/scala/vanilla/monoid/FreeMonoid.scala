package vanilla.monoid

import vanilla.monoid.FreeMonoid._

sealed trait FreeMonoid[+A] { self =>

  def ++[A1 >: A](that: FreeMonoid[A1]): FreeMonoid[A1] =
    (self, that) match {
      case (Identity, that) => that
      case (self, Identity) => self
      case (self, that) => Combine(self, that)
    }

  def foldMap[Z](ifValue: A => Z)(implicit M: Monoid[Z]): Z =
    self match {
      case FreeMonoid.Identity => M.empty
      case FreeMonoid.Value(a) => ifValue(a)
      case FreeMonoid.Combine(a1, a2) => M.combine(
        a1.foldMap(ifValue),
        a2.foldMap(ifValue)
      )
    }
}

object FreeMonoid { // Free = construct your programs as a data
  case object Identity extends FreeMonoid[Nothing]
  case class Combine[A](a1: FreeMonoid[A], a2: FreeMonoid[A]) extends FreeMonoid[A]
  case class Value[A](a: A) extends FreeMonoid[A]
}