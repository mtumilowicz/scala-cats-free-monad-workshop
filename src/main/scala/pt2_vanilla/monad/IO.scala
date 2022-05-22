package pt2_vanilla.monad

import cats.Id
import pt2_vanilla.NaturalTransformation.~>
import pt2_vanilla.{Console, PrintLine, ReadLine}
import pt2_vanilla.implicits._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object IO {
  type Thunk[A] = () => A
  type IO[A] = Free[Thunk, A]

  val identity = new (Thunk ~> Thunk) {
    def apply[A](t: Thunk[A]): Thunk[A] = t
  }

  def apply[A](body: => A): IO[A] = Free.liftF(() => body)

  def run[A](io: IO[A]): A = io.foldMap(identity).apply()

  val ioInterpreter: Console ~> IO = new (Console ~> IO) {
    override def apply[A](fa: Console[A]): IO[A] = fa match {
      case ReadLine => IO {
        scala.io.StdIn.readLine
      }
      case PrintLine(line) => IO {
        println(line)
      }
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
