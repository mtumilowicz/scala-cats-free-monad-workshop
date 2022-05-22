package pt3_churchencoding

object ChurchEncoding {

  trait ChurchBoolean {
    def apply[A](ifTrue: => A, ifFalse: => A): A

    def &&(that: ChurchBoolean): ChurchBoolean =
      this(
        that,
        ChurchBoolean.False
      )
  }

  object ChurchBoolean {
    val True = new ChurchBoolean {
      override def apply[A](ifTrue: => A, ifFalse: => A): A = ifTrue
    }

    val False = new ChurchBoolean {
      override def apply[A](ifTrue: => A, ifFalse: => A): A = ifFalse
    }

  }

  trait ChurchOption[+A] {
    def apply[B](ifNone: => B)(ifSome: A => B): B

    def isEmpty: ChurchBoolean =
      this(ChurchBoolean.True)(_ => ChurchBoolean.False)
  }

  object ChurchOption {
    val None = new ChurchOption[Nothing] {
      override def apply[B](ifNone: => B)(ifSome: Nothing => B): B = ifNone
    }

    def Some[A](value: A): ChurchOption[A] = new ChurchOption[A] {
      override def apply[B](ifNone: => B)(ifSome: A => B): B = ifSome(value)
    }
  }

  sealed trait ChurchNaturalNumber { self =>
    def apply[A](ifZero: A)(ifSuccessor: A => A): A

  }

  object ChurchNaturalNumber {
    val Zero = new ChurchNaturalNumber {
      override def apply[A](ifZero: A)(ifSuccessor: A => A): A = ifZero
    }

    def Successor(i: ChurchNaturalNumber): ChurchNaturalNumber = new ChurchNaturalNumber {
      override def apply[A](ifZero: A)(ifSuccessor: A => A): A = ifSuccessor(i(ifZero)(ifSuccessor))
    }

    val Two  = Successor(Successor(Zero))
    val Four = Successor(Successor(Successor(Successor(Zero))))
  }

}
