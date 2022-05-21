package ccats

import cats.free.Free
import cats.~>

sealed trait Console[A]

case class ReadLine[A](value: String => A) extends Console[A]

case class PrintLine[A](line: String, value: A) extends Console[A]

object Console {
  type Dsl[A] = Free[Console, A]

  def readLine: Dsl[String] = Free.liftF(ReadLine(identity))

  def printLine(line: String): Dsl[Unit] = Free.liftF(PrintLine(line, ()))
}

object IO {
  type Thunk[A] = () => A
  type IO[A] = Free[Thunk, A]

  val identity = new (Thunk ~> Thunk) {
    def apply[A](t: Thunk[A]): Thunk[A] = t
  }

  def apply[A](body: => A): IO[A] = Free.liftF(() => body)

  def run[A](io: IO[A]): A = io.foldMap(identity).apply()

}

object IOApp extends App {
  val program = for {
    _ <- IO(println("helloo!!"))
  } yield ()

  IO.run(program)
}