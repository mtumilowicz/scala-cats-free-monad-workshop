package pt1_cats

import cats.effect.IOApp
import cats.free.Free

object App extends IOApp.Simple {

  val program: Free[Console, Unit] = for {
    _ <- Console.printLine("What is your name?")
    name <- Console.readLine
    _ <- Console.printLine(s"Hi $name!")
  } yield ()

  val run = program.foldMap(Config.ioInterpreter)
}