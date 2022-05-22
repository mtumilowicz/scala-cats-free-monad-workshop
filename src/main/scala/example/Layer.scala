package example

sealed trait Layer[+A] extends Product with Serializable { self =>

  def fold[B](
               ifValue: A => B
             )(ifHorizontal: (B, B) => B)(ifVertical: (B, B) => B): B =
    self match {
      case Layer.Horizontal(left, right) =>
        ifHorizontal(
          left.fold(ifValue)(ifHorizontal)(ifVertical),
          right.fold(ifValue)(ifHorizontal)(ifVertical)
        )
      case Layer.Vertical(left, right) =>
        ifVertical(
          left.fold(ifValue)(ifHorizontal)(ifVertical),
          right.fold(ifValue)(ifHorizontal)(ifVertical)
        )
      case Layer.Value(value) => ifValue(value)
    }

  def ++[A1 >: A](that: Layer[A1]): Layer[A1] =
    Layer.Horizontal(this, that)

  def >>>[A1 >: A](that: Layer[A1]): Layer[A1] =
    Layer.Vertical(this, that)
}

object Layer {
  final case class Horizontal[A](left: Layer[A], right: Layer[A]) extends Layer[A]
  final case class Vertical[A](left: Layer[A], right: Layer[A])   extends Layer[A]
  final case class Value[A](value: A)                             extends Layer[A]
}