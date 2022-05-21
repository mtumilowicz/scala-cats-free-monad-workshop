package vanilla

trait NaturalTransformation[F[_], G[_]] {

  def apply[A](fa: F[A]): G[A]
}

object NaturalTransformation {
  type ~>[F[_], G[_]] = NaturalTransformation[F, G]
}
