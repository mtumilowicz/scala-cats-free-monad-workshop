[![Build Status](https://app.travis-ci.com/mtumilowicz/scala-cats-free-monad-workshop.svg?branch=master)](https://app.travis-ci.com/mtumilowicz/scala-cats-free-monad-workshop)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

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
    * https://stackoverflow.com/questions/54164346/what-is-the-main-difference-between-free-monoid-and-monoid
    * https://typelevel.org/cats/datatypes/freemonad.html#composing-free-monads-adts

## preface
* goals of this workshop:
    * show free monads as a opposition to tagless-final
    * introduction to other free structures: monoid, applicative, functor
    * introduction to church encoding and reification
    * understand how to use reification and free structures in practice
    * show how resolve layers graph with reification and free structures

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
            * you can go through cases one by one and say: program can this or that
            * easier to transform, compose, reason about
        * A - value produced by the program
* allows us to separate the structure of the computation from its interpretation
    * different interpretation depending on context (ex. live vs tests)
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
        * can only be interpreted, not introspected and transformed (prior to interpretation)
    * however, in applicatives: `def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]`
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
                    * `(<*>) :: f (a -> b) -> f a -> f b` // from applicative
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
                * `Applicative[List].map2(List(1, 2, 3), List(1, 2, 3))(_ + _)`
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
                            * `map2` is implemented in terms of `ap` which is implemented in terms of `flatMap`
                        * as such, only the second implementation is valid
                            * this is called typeclass coherence
                    * reason why things like Validated has only Applicative instances but not Monad ones (contrary to
                    for example Either)
* intuition for free functor hierarchy
    * free functor: programs that change values
    * free applicatives: programs that build data
        * classic example: parsers
            ```
            case class AuthConfig(port: Int, host: String)
            val authConfig = (int("port"), server("host")) mapN (AuthConfig)
            ```
    * free monads: programs that build programs

## composing free monads
* `EitherK[F[_], G[_], A]` - either on type constructors
* `FunctionK[F[_], G[_]]` is a natural transformation from `F` to `G`
    * we can compose them: `interpreter1 or interpreter2` (`FunctionK[EitherK[...], ...]`)
    * last problem: `type Dsl[A] = Free[F, A]` should have the same type `F`
        * `InjectK[F[_], G[_]]` is a type class providing an injection from type constructor `F` into type constructor `G`
        * we have to lift our DSLs into that common `F`
            * 

## free monoid
* when more than one monoid exists for a type, the free monoid defers
the decision on which specific monoid to use
* example
    * for integers, infinitely many monoids exist, but the most common are addition and multiplication
    * val out = FreeMonoid.Value(2) ++ FreeMonoid.Value(3) ++ ...
    * println(out) // Combine(Combine(Value(2), Value(3)), ...)
* similar to list
    * you could store elements in the list, then fold them with monoid

## church encoding
* OO vs FP
    * OO: easy to add operations (without code changes), hard to add actions (ex. return type change)
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
* OO and FP are related by the Church encoding
    * takes us from FP to OO
        * constructors become method calls
        * operator types become action types
        * example
            ```
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
            ```
    * reification: takes from OO to FP
* type classes are Church encodings of free structures
* free structures are reifications of type classes
* in scala, according to "Towards Equal Rights for Higher-Kinded Types" by Moors, Piessens and Odersky, July 2007
    * Scala's kinds correspond to the types of the simply-typed lambda calculus
    * this means that we can express addition on natural numbers on the level of types using a Church Encoding
    * example: https://w.pitula.me/2017/typelevel-church-enc/

## zio layer context
* suppose that we have two time consuming computations
    * A ===================> C
    * A ===================> C'
* and suppose there is some other orchestration that is trivial
    * B ==> C
    * B ==> C'
* there is often shorter path to some type B
    * A ===========> B
* so we can go:
    * A ===========> B
    * and then
        * B ==> C and B ==> C'
* and therefore save a lot of time
    * ===================>===================>
    * ===========>==>==>
* it could be difficult to see what is that intermediary structure B
    * replace functions call with data representation (B) and then fold over it (C, C')
    * reverse process to church encoding
* suppose we would like to have a program that:
    * shows how to correctly compose zio layers based on set of given layers
    * and prints all layers that are missing
    * so it's better to create first representation of graph, then - evaluate it
        * otherwise we would have to reuse big parts of the same logic twice (generating graph twice)