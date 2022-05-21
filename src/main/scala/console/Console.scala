package console

sealed trait Console[A]

case object ReadLine extends Console[String]

case class PrintLine(line: String) extends Console[Unit]
