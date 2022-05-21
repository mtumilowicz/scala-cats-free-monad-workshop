package vanilla.monad

import vanilla.Console

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import vanilla.implicits._

object MonadExample extends App {

  val program: Free[Console, Unit] = for {
    _ <- Console.printLine("hello!")
    _ <- Console.printLine("hello2!")
    _ <- Console.printLine("hello4!")
  } yield ()

  println(program)

  val stack = mutable.Stack.empty[String]
  val list = ListBuffer.empty[String]
  val interpreterTest = IO.inMemoryInterpreter(stack, list)
  IO.run(program.foldMap(IO.ioInterpreter))

  println(stack)
  println(list)
}
