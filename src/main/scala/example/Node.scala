package example

final case class Node[+A](inputs: List[Type], outputs: List[Type], expr: A)