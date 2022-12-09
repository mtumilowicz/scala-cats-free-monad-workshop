package pt5_composing

import cats.effect.IO
import cats.{Id, ~>}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.StdIn

object Config {

  val ioConsoleReaderInterpreter = new (ConsoleReader ~> IO) {
    override def apply[A](fa: ConsoleReader[A]): IO[A] = {
      fa match {
        case ReadLine => IO {
          StdIn.readLine()
        }
      }
    }
  }

  val ioConsolePrinterInterpreter = new (ConsolePrinter ~> IO) {
    override def apply[A](fa: ConsolePrinter[A]): IO[A] = {
      fa match {
        case PrintLine(line) => IO {
          println(line)
        }
      }
    }
  }

  val ioInterpreter: (CatsApp ~> IO) = ioConsoleReaderInterpreter or ioConsolePrinterInterpreter


  def inMemoryConsolePrinterInterpreter(output: ListBuffer[String]): ConsolePrinter ~> Id = new (ConsolePrinter ~> Id) {

    def apply[A](inout: ConsolePrinter[A]): Id[A] = inout match {
      case PrintLine(line) =>
        output += line
        ()
    }
  }

  def inMemoryConsoleReaderInterpreter(input: mutable.Stack[String]): ConsoleReader ~> Id = new (ConsoleReader ~> Id) {

    def apply[A](inout: ConsoleReader[A]): Id[A] = inout match {
      case ReadLine =>
        input.pop
    }
  }

  def inMemoryInterpreter(input: mutable.Stack[String], output: ListBuffer[String]): (CatsApp ~> Id) =
    inMemoryConsoleReaderInterpreter(input) or inMemoryConsolePrinterInterpreter(output)

}
