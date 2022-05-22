package pt4_practice

final case class Graph[A](nodes: List[Node[A]]) {
  def findNode(output: Type): Node[A] =
    nodes.find(_.outputs.contains(output)) match {
      case Some(value) => value
      case None => throw new Error(s"Compilation error: missing $output layer") // simulates that error you get with ZIO2
    }

  def build(target: List[Type]): Layer[A] = {
    val result: List[Layer[A]] = target.map { t =>
      val node: Node[A] = findNode(t)
      val dependencies: List[Layer[A]] =
        node.inputs.map(t => build(List(t)))
      if (dependencies.isEmpty) Layer.Value(node.expr)
      else dependencies.reduce(_ ++ _) >>> Layer.Value(node.expr)
    }
    result.reduce(_ ++ _)
  }
}