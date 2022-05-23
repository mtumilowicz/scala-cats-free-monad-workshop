# scala-cats-free-monad-workshop

* references
    * [Zymposium - Data vs Function (Part 1) — Church Encodings](https://www.youtube.com/watch?v=-xo7VvKpE8w)
    * [Zymposium — Data vs Function — Free Structures (Part 2)](https://www.youtube.com/watch?v=hbyVu5tgAiA)
    * [Move Over Free Monads: Make Way for Free Applicatives! — John de Goes](https://www.youtube.com/watch?v=H28QqxO7Ihc)
    * [John De Goes: One Monad to Rule Them All](https://www.youtube.com/watch?v=M0Fe2SRTm5c)
    * [Free as in Monads by Daniel Spiewak](https://www.youtube.com/watch?v=aKUQUIHRGec)
    * [Free monad or tagless final? How not to commit to a monad too early - Adam Warski](https://www.youtube.com/watch?v=IhVdU4Xiz2U)
    * [Free Monads—Paweł Szulc](https://www.youtube.com/watch?v=ycrpJrcWMp4)
    * [Uniting Church and State: FP and OO Together by Noel Welsh](https://www.youtube.com/watch?v=IO5MD62dQbI)
    * https://softwaremill.com/free-tagless-compared-how-not-to-commit-to-monad-too-early/
    * https://github.com/softwaremill/free-tagless-compare
    * https://github.com/zivergetech/Zymposium
    * https://github.com/noelwelsh/church-and-state
    * https://underscore.io/blog/posts/2017/06/02/uniting-church-and-state.html
    * http://jim-mcbeath.blogspot.com/2008/11/practical-church-numerals-in-scala.html
    * https://softwaremill.com/free-tagless-compared-how-not-to-commit-to-monad-too-early/
    * https://softwaremill.com/free-monads/
    * https://gist.github.com/igor-ramazanov/bd7d2a9dd5726d8ca9c356cf6cd85abf

## free monad
* example
    ```
    sealed trait DiskIO[A]
    final case class Read(file: String) extends DiskIO[Array[Byte]]
    final case class Write(file: String, contents: Array[Byte]) extends DiskIO[Unit]

    object DiskIO {
      def read(filename: String): Free[Disk, Array[Byte]] = Free.lift(Read(filename))
      def write(filename: String, data: Array[Byte]) = Free.lift(Write(filename, data))
    }

    def program: Free[DiskIO, Unit] = for {
        foo <- DiskIO.read("foo.txt")
        bar <- DiskIO.read("bar.txt")
        _ <- DiskIO.write("output.txt", foo ++ bar)
      } yield ()

    val interpreterTask: DiskIO ~> Task = new (DiskIO ~> Task) {
      def apply[A](fa: DiskIO[A]): Task[A] = fa match {
        case Read(file) => Task { ... }
        case Write(file, contents) => Task { ... }
      }
    }

    foo2.foldMap(interpreterTask)
    ```
* free monad = represent program as data
    * `Free[F, A]`
      * Free = program
      * F - algebra of the program (language)
      * A - value produced by the program
* allows us to separate the structure of the computation from its interpreter, thereby allowing
different interpretation depending on context
* description of a program
    * set of things you can do is algebra of a free monad program
    * you can go through cases one by one and, and say: program can this
    * easier to transform, compose, reason about
* using the free monad:
    * programs are data; built from constructors of an ADT
    * each operation is reified as a value
    * we can pattern-match on the programs (which are values) to transform & optimize them
    * interpretation is deferred until an interpreter is provided
* trouble with monads
  * sequential computation: a program can depend on a value produced by previous program
  * trait Monad[F[_]]
    * def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
      * fa - first program
      * A - runtime value
      * F[B] - second program
      * return = result program
  * def bind[A, B](fa: F[A])(f: A => F[B]): F[B]
    * can only be interpreted, not introspected and transformed prior to interpretation
  * def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]
    * no program depends on any runtime value: structure is static
    * (doX |@| doY |@| doZ)(doResults(_, _, _))
      * parallel, not sequential

## free applicatives
  * record the structure of applicative composition
  * interpret it later in any way
  * applicative language for describing configuration
  * classic example for free applicative: parsers
  * object example
    * import dsl._
    * case class AuthConfig(port: Int, host: String)
    * case class ServerConfig(logging: Boolean, auth: AuthConfig)
    * val authConfig = (int("port") |@| server("host"))(AuthConfig)
    * val serverConfig = (int("logging") |@| server("auth"))(authConfig)(ServerConfig)
* intuition for free functor hierarchy
  * free functor: programs that change values
  * free applicatives: programs that build data
  * free monads: programs that build programs

## free monoid
* monoid laws
  * combine(identity, a) == a
  * combine(a, identity) == a
  * associativity
* free monoid
  * sealed trait FreeMonoid[+A]
  * object FreeMonoid
    * case object Identity extend FreeMonoid[Nothing]
    * case class Combine[A](a1: FreeMonoid[A], a2: FreeMonoid[A]) extend FreeMonoid[A]
    * case class Value[A](value: A) extends FreeMonoid[A]
    * foldMap[Z](ifValue: A => Z)(implicit monoid: Monoid[Z]): Z = ...
    * def ++(that: FreeMonoid[A]): FreeMonoid[A] = Combine(self, that)
  * use case: first build a tree, then analyze it, then do optimizations, then combine it
    * val free = FreeMonoid.Value(kit) ++ FreeMonoid.Value(adam)
      * println(free) // Combine(Combine(Value(kit), Value(adam)), ...)
  * it is not associative (tree) - but you could always combine it to be associative
    * (FreeMonoid.Value(kit) ++ FreeMonoid.Value(adam)) ++ FreeMonoid.Value(joe)
    * FreeMonoid.Value(kit) ++ (FreeMonoid.Value(adam) ++ FreeMonoid.Value(joe))
    * but when running foldMap it does not matter
  * similar to list
      * case class Combine[A](a1: A, a2: FreeMonoid[A]) extend FreeMonoid[A]
      * ++ pattern match Identity => that; Combine(a1, a2) => Combine(a1, a2 + that)
      * def value[A](value: A): FreeMonoid[A] = Combine(value, Identity)
      * if we change it like this the structure is associative (we are combine it like a list)
      * a1 = head; a2 = tail; Identity - Nil, Combine - Cons


## church encoding
* OO vs FP
    * OO: easy to add operations, hard to add actions (ex. return type change)
        ```
        class Calculator {
            def literal(v: Double): Double = v
            def add(a: Double, b: Double): Double = a + b
            def subtract(a: Double, b: Double): Double = a - b
            ...
        }

        val c = new Calculator
        import c._

        add(literal(1.0), subtract(literal(3.0), literal(2.0)))

        // inheritance (add operations)
        class TrigonometricCalculator extends Calculator {
            def sin(a: Double): Double = Math.sin(a)
        }
        ```
    * FP: hard to add operations, easy to add actions (ex. new interpreter)
        ```
        // classic FP = represent operations as data
        sealed trait Calculation
        case class Literal(v: Double) extends Calculation
        case class Add(a: Calculation, b: Calculation) extends Calculation
        case class Subtract(a: Calculation, b: Calculation) extends Calculation
        ...

        // define interpreters (add actions)
        def eval(c: Calculation): Double =
            c match {
                case Literal(v) => v
                case Add(a, b) => eval(a) + eval(b)
                case Subtract(a, b) => eval(a) - eval(b)
                ...
            }

        def pretty(c: Calculation): String =
            c match {
                case Literal(v) => v.toString
                case Add(a, b) => s"${pretty(a)} + ${pretty(b)}"
                case Subtract(a, b) => s"${pretty(a)} - ${pretty(b)}"
                ...
            }
        ```
        *  it’s impossible to add new operations, like sin and cos, to this representation without code changes

* OO and FP are related bu the Church encoding
* takes us from FP to OO
    * constructors become method calls
    * operator types because action types
* reification: takes from OO to FP
* type classes are Church encodings of free structures
* free structures are reifications of type classes
* in scala, according to "Towards Equal Rights for Higher-Kinded Types" by Moors, Piessens and Odersky, July 2007
    * Scala's kinds correspond to the types of the simply-typed lambda calculus. This means that we can express addition on natural numbers on the level of types using a Church Encoding.
    * example: https://w.pitula.me/2017/typelevel-church-enc/

## zio layer context