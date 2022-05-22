package pt2_vanilla

import pt2_vanilla.monad.Free

sealed trait Console[A]

case object ReadLine extends Console[String]

case class PrintLine(line: String) extends Console[Unit]

object Console {
  type Dsl[A] = Free[Console, A]

  def readLine: Dsl[String] = Free.liftF(ReadLine)

  def printLine(line: String): Dsl[Unit] = Free.liftF(PrintLine(line))


}