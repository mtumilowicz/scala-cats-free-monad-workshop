package pt2_vanilla.monad

import pt2_vanilla.Console
import pt2_vanilla.implicits._

object App {

  val program: Free[Console, Unit] = for {
    _ <- Console.printLine("What is your name?")
    name <- Console.readLine
    _ <- Console.printLine(s"Hi $name!")
  } yield ()

  def main(args: Array[String]): Unit = {
    IO.run(program.foldMap(Config.ioInterpreter))
  }
}
