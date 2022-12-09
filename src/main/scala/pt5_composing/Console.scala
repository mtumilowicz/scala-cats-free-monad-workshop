package pt5_composing

import cats.InjectK
import cats.free.Free

sealed trait ConsoleReader[A]

case object ReadLine extends ConsoleReader[String]

sealed trait ConsolePrinter[A]

case class PrintLine(line: String) extends ConsolePrinter[Unit]

class ConsoleReaders[F[_]](implicit I: InjectK[ConsoleReader, F]) {
  type Dsl[A] = Free[F, A]

  def readLine: Dsl[String] = Free.liftInject[F](ReadLine)
}

object ConsoleReaders {
  implicit def consoleReaders[F[_]](implicit I: InjectK[ConsoleReader, F]): ConsoleReaders[F] = new ConsoleReaders[F]
}

class ConsolePrinters[F[_]](implicit I: InjectK[ConsolePrinter, F]) {
  type Dsl = Free[F, Unit]

  def printLine(line: String): Dsl = Free.liftInject[F](PrintLine(line))
}

object ConsolePrinters {
  implicit def consolePrinters[F[_]](implicit I: InjectK[ConsolePrinter, F]): ConsolePrinters[F] = new ConsolePrinters[F]
}