package pt3_churchencoding

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pt3_churchencoding.ChurchEncoding.{ChurchBoolean, ChurchNaturalNumber, ChurchOption}

class ChurchEncodingTest extends AnyFunSuite with Matchers {

  test("test && of church boolean") {
    //    expect
    (ChurchBoolean.False && ChurchBoolean.False) shouldBe ChurchBoolean.False
    (ChurchBoolean.False && ChurchBoolean.True) shouldBe ChurchBoolean.False
    (ChurchBoolean.True && ChurchBoolean.False) shouldBe ChurchBoolean.False
    (ChurchBoolean.True && ChurchBoolean.True) shouldBe ChurchBoolean.True
  }

  test("isEmpty of church option") {
    //    given
    val none = ChurchOption.None
    val some = ChurchOption.Some(5)
    //    expect
    none.isEmpty shouldBe ChurchBoolean.True
    some.isEmpty shouldBe ChurchBoolean.False
  }

  test("evaluate church natural number to int") {
    //    given
    val zero = ChurchNaturalNumber.Zero
    val two = ChurchNaturalNumber.Two
    val four = ChurchNaturalNumber.Four

    //    expect
    zero.toInt shouldBe 0
    two.toInt shouldBe 2
    four.toInt shouldBe 4
  }

}
