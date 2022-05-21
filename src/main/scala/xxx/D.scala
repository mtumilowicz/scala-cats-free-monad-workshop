package xxx

import cats.free.Free
import cats.~>

object IO {
  type Thunk[A] = () => A
  type IO[A] = Free[Thunk, A]

  val identity = new (Thunk ~> Thunk) {
    def apply[A](t: Thunk[A]): Thunk[A] = t
  }

  def apply[A](body: => A): IO[A] = Free.liftF(() => body)

  def run[A](io: IO[A]): A = io.foldMap(identity).apply()

}