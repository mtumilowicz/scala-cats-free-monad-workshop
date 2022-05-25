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
    * https://stackoverflow.com/questions/46789807/can-i-represent-non-sequential-parallel-execution-with-monads
    * https://hackage.haskell.org/package/base-4.16.1.0/docs/Control-Monad.html
    * https://hackage.haskell.org/package/base-4.16.1.0/docs/Control-Applicative.html
    * https://stackoverflow.com/questions/46913472/how-exactly-does-the-ap-applicative-monad-law-relate-the-two-classes
    * https://www.reddit.com/r/scala/comments/hu1xgk/struggling_with_cats_applicative_implementation/

## free monad
* example
    ```
    sealed trait Console[A]
    case object ReadLine extends Console[String]
    case class PrintLine(line: String) extends Console[Unit]

    object Console {
      type Dsl[A] = Free[Console, A]

      def readLine: Dsl[String] = Free.liftF(ReadLine)
      def printLine(line: String): Dsl[Unit] = Free.liftF(PrintLine(line))
    }

    val program: Free[Console, Unit] = for {
      _ <- Console.printLine("What is your name?")
      name <- Console.readLine
      _ <- Console.printLine(s"Hi $name!")
    } yield ()

    // interpreter for live app
    val ioInterpreter : Console ~> IO = new (Console ~> IO) {
      override def apply[A](fa: Console[A]): IO[A] = fa match {
        case ReadLine => IO { scala.io.StdIn.readLine }
        case PrintLine(line) => IO { println(line) }
      }
    }

    // interpreter for tests
    def inMemoryInterpreter(input: ..., output: ...): Console ~> Id = new (Console ~> Id) {
        ...
    }


    IO.run(program.foldMap(IO.ioInterpreter))
    ```
* free monad = represent program as data
    * `Free[F, A]`
        * Free = program
        * F - algebra of the program (language)
            * you can go through cases one by one and, and say: program can this
            * easier to transform, compose, reason about
        * A - value produced by the program
* allows us to separate the structure of the computation from its interpreter
    * different interpretation depending on context (live vs tests)
* pros
    * we can pattern-match on the programs (which are values) to transform & optimize them
    * interpretation is deferred until an interpreter is provided
* digression: trouble with monads
    * `def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]`
        * sequential computation: a program can depend on a value produced by previous program
        * fa - first program
        * A - runtime value
        * F[B] - second program
        * return = result program
        * can only be interpreted, not introspected and transformed prior to interpretation
    * however applicatives: `def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]`
        * no program depends on any runtime value: structure is static
        * `(doX, doY, doZ) mapN (doResults(_, _, _))`
            * parallel, not sequential
    * back to monads
        * can I represent the `ap` function in terms of the `bind` function?
            * yes - every monad is an applicative
        * can I represent parallel execution with monads
            * a type which exposes a monadic interface cannot use Applicative to do parallel computation
            * the Monad and Applicative operations should relate as follows:
                * `(<*>) = ap`, where
                    * `(<*>) :: f (a -> b) -> f a -> f b`
                    * `ap :: Monad m => m (a -> b) -> m a -> m b`
                * `(<*>) = ap` is exactly the same as
                    ```
                    m1 <*> m2 = do {
                        x1 <- m1;
                        x2 <- m2;
                        return (x1 x2)
                    }
                    ```
            * example for List
                * `Applicative[List].map2(List(1, 2, 3), List(1, 2, 3))( _ + _)`
                * two possible returns
                    * `List(2, 4, 6)`
                    * `List(2, 3, 4, 3, 4, 5, 4, 5, 6)`
                * explanation
                    * if you only consider Applicative then both implementations are valid
                    * however
                        * List should have also an instance of Monad
                        * all Monads are also Applicatives (in cats: `Monad[F[_]] extends Applicative[F]`)
                        * then the implementation of `map2` in this case has not only to be consistent with
                        the Applicative laws but also with the Monad laws
                            * map2 is implemented in terms of `ap` which is implemented in terms of `flatMap`
                        * as such, only the current implementation is valid
                            * this is called typeclass coherence
                    * this is the reason why things like `Valided` has only `Applicative` instances but not
                    `Monad` ones, like `Either`
* intuition for free functor hierarchy
  * free functor: programs that change values
  * free applicatives: programs that build data
    * classic example: parsers
        ```
        case class AuthConfig(port: Int, host: String)
        val authConfig = (int("port"), server("host")) mapN (AuthConfig)
        ```
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