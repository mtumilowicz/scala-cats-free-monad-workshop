package pt1_cats

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AppTest extends AnyFunSuite with Matchers {

  test("test console app") {
    // given
    val files = Map("test.txt" -> "Igor".getBytes)
    val expectedState = Map("test.txt" -> "Igor", "test.log" -> "Hello Igor!")

    // when
    val (state, result) = App.program
      .foldMap(Config.inMemoryInterpreter)
      .run(files)
      .value

    // then
    mapValuesToString(state) shouldBe expectedState
    result shouldBe "Hello Igor!"
  }

  private def mapValuesToString(map: Map[String, Array[Byte]]): Map[String, String] =
    map.view.mapValues(new String(_, "UTF-8")).toMap

}
