package vanilla

import vanilla.IO.{IO, Thunk}

object implicits {

  implicit val monadThunk: Monad[Thunk] = new Monad[Thunk] {
    override def pure[A](x: A): Thunk[A] = () => x

    override def flatMap[A, B](fa: Thunk[A])(f: A => Thunk[B]): Thunk[B] = f.apply(fa())
  }

  implicit val monadIo: Monad[IO] = new Monad[IO] {
    override def pure[A](x: A): IO[A] = IO(x)

    override def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] = fa.flatMap(f)
  }

  implicit val monoidString: Monoid[String] = new Monoid[String] {
    override def empty: String = ""

    override def combine(a1: String, a2: String): String = a1 ++ a2
  }

}
