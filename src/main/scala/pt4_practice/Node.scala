package pt4_practice

final case class Node[+A](inputs: List[Type], outputs: List[Type], expr: A)