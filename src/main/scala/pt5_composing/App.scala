package pt5_composing

import cats.effect.{ExitCode, IO, IOApp}
import cats.free.Free

object App extends IOApp {

  def program(implicit I: ConsoleReaders[CatsApp], D: ConsolePrinters[CatsApp]): Free[CatsApp, Unit] = {

    import I._, D._

    for {
      _ <- printLine("What is your name?")
      name <- readLine
      _ <- printLine(s"Hi $name!")
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] =
    program
      .foldMap(Config.ioInterpreter)
      .as(ExitCode.Success)
}
