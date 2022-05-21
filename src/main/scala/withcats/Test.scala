package withcats

import cats.effect.{IO, IOApp}
import cats.free.Free
import cats.~>

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