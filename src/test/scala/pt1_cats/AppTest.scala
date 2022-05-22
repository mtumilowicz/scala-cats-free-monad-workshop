package pt1_cats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class AppTest extends AnyFunSuite with Matchers {

  test("test1") {
    // given
    val inputs = mutable.Stack("Michal")
    val outputs = ListBuffer.empty[String]

    // when
    App.program.foldMap(Config.inMemoryInterpreter(inputs, outputs))

    // then
    inputs shouldBe empty
    outputs shouldBe ListBuffer("What is your name?", "Hi Michal!")
  }

}
