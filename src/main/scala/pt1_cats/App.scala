package pt1_cats

import cats.effect.{ExitCode, IO, IOApp}
import cats.free.Free

object App extends IOApp {

  val program: Free[Disk, String] = for {
    data <- Disk.readFile("test.txt")
    _ <- Disk.writeToFile("test.log", "Hello ".getBytes ++ data ++ "!".getBytes)
    newData <- Disk.readFile("test.log")
  } yield new String(newData, "UTF-8")

  override def run(args: List[String]): IO[ExitCode] =
    program
      .foldMap(Config.ioInterpreter)
      .as(ExitCode.Success)
}