package pt4_practice

final case class Expr(string: String) {
  def ++(that: Expr): Expr =
    Expr(s"($string ++ ${that.string})")

  def >>>(that: Expr): Expr =
    Expr(s"($string >>> ${that.string})")
}
