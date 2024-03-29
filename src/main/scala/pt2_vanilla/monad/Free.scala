package pt2_vanilla.monad

import pt2_vanilla.NaturalTransformation.~>
import pt2_vanilla.monad.Free._

sealed trait Free[F[_], A] {

  def map[B](f: A => B): Free[F, B] = flatMap(a => Free.pure(f(a)))

  def flatMap[B](f: A => Free[F, B]): Free[F, B] = FlatMap(this, f)

  def foldMap[G[_]](nt: F ~> G)(implicit M: Monad[G]): G[A] = this match {
    case Pure(a)  => M.pure(a)
    case LiftF(fa) => nt(fa)
    case FlatMap(target, f) => M.flatMap(target.foldMap(nt)) { e => f(e).foldMap(nt)}
  }
}

object Free {
  def pure[F[_], A](a: A): Free[F, A] = Pure(a)

  def liftF[F[_], A](fa: F[A]): Free[F, A] = LiftF(fa)

  final case class Pure[F[_], A](a: A) extends Free[F, A]

  final case class LiftF[F[_], A](fa: F[A]) extends Free[F, A]

  final case class FlatMap[F[_], E, A](target: Free[F, E], f: E => Free[F, A]) extends Free[F, A]
}