package pt1_cats

import cats.free.Free

sealed trait Disk[A]
case class Read(filename: String) extends Disk[Array[Byte]]
case class Write(filename: String, data: Array[Byte]) extends Disk[Unit]

object Disk {
  type Dsl[A] = Free[Disk, A]

  def readFile(filename: String): Dsl[Array[Byte]] = Free.liftF(Read(filename))
  def writeToFile(filename: String, data: Array[Byte]): Dsl[Unit] = Free.liftF(Write(filename, data))
}