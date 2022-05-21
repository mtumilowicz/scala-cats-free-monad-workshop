package vanilla.monoid

import vanilla.implicits._

object MonoidExample extends App {
  case class Person(name: String)

  val a = Person("Abraham")
  val d = Person("Donald")
  val b = Person("Barrack")

  val free = (FreeMonoid.Value(a) ++ FreeMonoid.Value(d)) ++ FreeMonoid.Value(b)
  val free2 = FreeMonoid.Value(a) ++ (FreeMonoid.Value(d) ++ FreeMonoid.Value(b))

  println(free)
  println(free2)
  println(free.foldMap(_.name))
  println(free2.foldMap(_.name))

}
