package pt1_cats

import cats.effect.IO
import cats.{Id, ~>}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object Config {

  val ioInterpreter : Console ~> IO = new (Console ~> IO) {
    override def apply[A](fa: Console[A]): IO[A] = fa match {
      case ReadLine => IO { scala.io.StdIn.readLine }
      case PrintLine(line) => IO { println(line) }
    }
  }

  def inMemoryInterpreter(input: mutable.Stack[String], output: ListBuffer[String]): Console ~> Id = new (Console ~> Id) {

    def apply[A](inout: Console[A]): Id[A] = inout match {
      case PrintLine(line) =>
        output += line
        ()
      case ReadLine =>
        input.pop
    }
  }

}
