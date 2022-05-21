package ccats

import cats.effect.{IO, IOApp}
import cats.free.Free
import cats.~>
import console.{Console, PrintLine, ReadLine}

object Console {
  type Dsl[A] = Free[Console, A]

  def readLine: Dsl[String] = Free.liftF(ReadLine)

  def printLine(line: String): Dsl[Unit] = Free.liftF(PrintLine(line))
}

object Test extends IOApp.Simple {

  val interpreter: Console ~> IO = new (Console ~> IO) {
    override def apply[A](fa: Console[A]): IO[A] = fa match {
      case ReadLine => IO { scala.io.StdIn.readLine }
      case PrintLine(line) => IO { println(line) }
    }
  }

  val program: Free[Console, Unit] = for {
    _ <- Console.printLine("helloo!!")
  } yield ()

  val run = program.foldMap(interpreter)
}