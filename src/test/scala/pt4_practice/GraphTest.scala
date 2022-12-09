package pt4_practice

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import pt4_practice._

class GraphTest extends AnyFunSuite with Matchers {

  val layerCNode =
    Node(
      inputs = List(Type("A"), Type("B")),
      outputs = List(Type("C")),
      expr = Expr("layerC")
    )

  val layerBNode =
    Node(
      inputs = List(Type("D")),
      outputs = List(Type("B")),
      expr = Expr("layerB")
    )

  val layerANode =
    Node(inputs = List(), outputs = List(Type("A")), expr = Expr("layerA"))

  val layerDNode =
    Node(inputs = List(), outputs = List(Type("D")), expr = Expr("layerD"))

  val layerZNode =
    Node(
      inputs = List(Type("A"), Type("B")),
      outputs = List(Type("X")),
      expr = Expr("layerZ")
    )

  val graph =
    Graph(
      List(
        layerCNode,
        layerBNode,
        layerANode,
        layerDNode,
        layerZNode
      )
    )

  test("build graph (intermediary representation)") {
    //    given
    val layer: Layer[Expr] = graph.build(List(Type("C")))

    //    expect
    layer.toString shouldBe "Vertical(Horizontal(Value(Expr(layerA)),Vertical(Value(Expr(layerD)),Value(Expr(layerB)))),Value(Expr(layerC)))"
  }

  test("resolve graph") {
    //    given
    val layer: Layer[Expr] = graph.build(List(Type("C")))

    //    when
    val expr = layer.fold[Expr](identity)(_ ++ _)(_ >>> _)

    //    then
    expr.string shouldBe "((layerA ++ (layerD >>> layerB)) >>> layerC)"
  }

  test("show only used layers") {
    //    given
    val layer: Layer[Expr] = graph.build(List(Type("C")))

    //    when
    val used = layer.fold[List[Expr]](a => List(a))(_ ++ _)(_ ++ _)

    //    then
    used shouldBe List(Expr("layerA"), Expr("layerD"), Expr("layerB"), Expr("layerC"))
  }

}
