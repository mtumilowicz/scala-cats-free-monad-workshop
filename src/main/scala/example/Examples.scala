package example

object Examples {
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

  def main(args: Array[String]): Unit = {
    val layer: Layer[Expr] = graph.build(List(Type("C")))
    val expr = layer.fold[Expr](identity)(_ ++ _)(_ >>> _)
    val used = layer.fold[List[Expr]](a => List(a))(_ ++ _)(_ ++ _)
    val count = used.length
    println(layer)
    println(expr.string)
    println(used)
    println(count)
  }
}