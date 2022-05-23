package pt1_cats

import cats.arrow.FunctionK
import cats.data.State
import cats.effect.IO
import cats.{Eval, Id, ~>}

import java.nio.file.{Files, Paths}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Using

object Config {

  val ioInterpreter = new (Disk ~> IO) {
    override def apply[A](fa: Disk[A]): IO[A] = {
      fa match {
        case Read(filename) =>
          IO {
            Using(Source.fromFile(filename, "UTF-8")) {
              _.mkString.getBytes
            }.getOrElse(Array[Byte]())
          }
        case Write(filename, data) =>
          IO {
            Files.write(Paths.get(filename), data)
            ()
          }
      }
    }
  }

  type InMemoryState[A] = State[Map[String, Array[Byte]], A]

  val inMemoryInterpreter = new (Disk ~> InMemoryState) {
    override def apply[A](fa: Disk[A]): InMemoryState[A] =
      fa match {
        case Read(filename) =>
          State[Map[String, Array[Byte]], Array[Byte]](map =>
            (map, map(filename)))
        case Write(filename, data) =>
          State[Map[String, Array[Byte]], Unit](map =>
            (map.updated(filename, data), ()))
      }
  }

}
