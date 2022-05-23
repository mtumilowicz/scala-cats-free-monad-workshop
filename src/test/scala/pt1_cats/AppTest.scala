package pt1_cats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AppTest extends AnyFunSuite with Matchers {

  test("test console app") {
    // given
    val files = Map("test.txt" -> "Igor".getBytes)

    // when
    val (state, result) = App.program
      .foldMap(Config.inMemoryInterpreter)
      .run(files)
      .value

    // then
    state shouldBe Map("test.txt" -> "Igor".getBytes, "log.txt" -> "Hello Igor!".getBytes)
    result shouldBe "Hello Igor!"
  }

}
