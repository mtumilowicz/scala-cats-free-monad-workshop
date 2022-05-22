package pt2_vanilla

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pt2_vanilla.implicits._
import pt2_vanilla.monoid.FreeMonoid

class FreeMonoidTest extends AnyFunSuite with Matchers {

  case class Person(name: String)

  val abraham = Person("Abraham")
  val donald = Person("Donald")
  val barrack = Person("Barrack")

  test("order matters when combining with free monoid") {
    //    given
    val free = (FreeMonoid.Value(abraham) ++ FreeMonoid.Value(donald)) ++ FreeMonoid.Value(barrack)
    val free2 = FreeMonoid.Value(abraham) ++ (FreeMonoid.Value(donald) ++ FreeMonoid.Value(barrack))

    //    expect
    free.toString shouldNot equal(free2)
  }

  test("order doesn't matter because foldMap needs monoid") {
    //    given
    val free = (FreeMonoid.Value(abraham) ++ FreeMonoid.Value(donald)) ++ FreeMonoid.Value(barrack)

    //    expect
    free.foldMap(_.name)
  }

}
