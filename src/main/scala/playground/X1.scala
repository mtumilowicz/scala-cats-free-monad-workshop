package playground

import cats.{Id, Monoid}
import console.{Console, PrintLine, ReadLine}
import playground.FreeMonoid.{Combine, Identity}
import playground.IO.IO

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

trait Monad[F[_]] {
  def pure[A](x: A): F[A]

  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
}

sealed trait Free[F[_], A] {

  import playground.Free._

  def map[B](f: A => B): Free[F, B] = flatMap(a => Free.pure(f(a)))

  def flatMap[B](f: A => Free[F, B]): Free[F, B] = Bind(this, f)

  def foldMap[G[_]](nt: F ~> G)(implicit M: Monad[G]): G[A] = this match {
    case Pure(a)  => M.pure(a)
    case Suspend(fa) => nt(fa)
    case Bind(target, f) => M.flatMap(target.foldMap(nt)) { e => f(e).foldMap(nt)}
  }
}

object Free {
  def pure[F[_], A](a: A): Free[F, A] = Pure(a)

  def liftM[F[_], A](fa: F[A]): Free[F, A] = Suspend(fa)

  final case class Pure[F[_], A](a: A) extends Free[F, A]

  final case class Suspend[F[_], A](fa: F[A]) extends Free[F, A]

  // FlatMap
  final case class Bind[F[_], E, A](target: Free[F, E], f: E => Free[F, A]) extends Free[F, A]
}

trait ~>[F[_], G[_]] {
  def apply[A](fa: F[A]): G[A]
}

object IO {
  type Thunk[A] = () => A
  type IO[A] = Free[Thunk, A]

  val identity = new (Thunk ~> Thunk) {
    def apply[A](t: Thunk[A]): Thunk[A] = t
  }

  implicit val monadIo: Monad[Thunk] = new Monad[Thunk] {
    override def pure[A](x: A): Thunk[A] = () => x

    override def flatMap[A, B](fa: Thunk[A])(f: A => Thunk[B]): Thunk[B] = f.apply(fa())
  }

  def apply[A](body: => A): IO[A] = Free.liftM(() => body)

  def run[A](io: IO[A]): A = io.foldMap(identity).apply()

}

object Console {
  // All operations like: getNumberOFCrashes, getNumberOfEmployees
  type Dsl[A] = Free[Console, A] // Free is program; Cosnole is algebra, A return type of program

  def readLine: Dsl[String] = Free.liftM(ReadLine)

  def printLine(line: String): Dsl[Unit] = Free.liftM(PrintLine(line))
}

object MonadExample extends App {

  val interpreter: Console ~> IO = new (Console ~> IO) {
    override def apply[A](fa: Console[A]): IO[A] = fa match {
      case ReadLine => IO { scala.io.StdIn.readLine }
      case PrintLine(line) => IO { println(line) }
    }
  }

  def interpreter2(input: mutable.Stack[String], output: ListBuffer[String]): Console ~> Id = new (Console ~> Id) {

    def apply[A](inout: Console[A]): Id[A] = inout match {
      case PrintLine(line) =>
        output += line
        ()
      case ReadLine =>
        input.pop
    }
  }

  val program: Free[Console, Unit] = for {
    _ <- Console.printLine("hello!")
    _ <- Console.printLine("hello2!")
    _ <- Console.printLine("hello4!")
  } yield ()

  println(program)

  implicit val monadIo: Monad[IO] = new Monad[IO] {
    override def pure[A](x: A): IO[A] = IO(x)

    override def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] = fa.flatMap(f)
  }

  val stack = mutable.Stack.empty[String]
  val list = ListBuffer.empty[String]
  val interpreterTest = interpreter2(stack, list)
  IO.run(program.foldMap(interpreter))

  println(stack)
  println(list)
}

sealed trait FreeMonoid[+A] { self =>

  def ++[A1 >: A](that: FreeMonoid[A1]): FreeMonoid[A1] =
    (self, that) match {
      case (Identity, that) => that
      case (self, Identity) => self
      case (self, that) => Combine(self, that)
    }

  def foldMap[Z: Monoid](ifValue: A => Z): Z =
    self match {
      case FreeMonoid.Identity => Monoid[Z].empty
      case FreeMonoid.Value(a) => ifValue(a)
      case FreeMonoid.Combine(a1, a2) => Monoid[Z].combine(
        a1.foldMap(ifValue),
        a2.foldMap(ifValue)
      )
    }
}

object FreeMonoid { // Free = construct your programs as a data
  case object Identity extends FreeMonoid[Nothing]
  case class Combine[A](a1: FreeMonoid[A], a2: FreeMonoid[A]) extends FreeMonoid[A]
  case class Value[A](a: A) extends FreeMonoid[A]
}

object MonoidExample extends App {
  case class Person(name: String)

  val a = Person("Abraham")
  val d = Person("Donald")
  val b = Person("Barrack")

  val free = (FreeMonoid.Value(a) ++ FreeMonoid.Value(d)) ++ FreeMonoid.Value(b)
  val free2 = FreeMonoid.Value(a) ++ (FreeMonoid.Value(d) ++ FreeMonoid.Value(b))

  println(free)
  println(free2)
  println(free.foldMap(_.name)) // associativity
  println(free2.foldMap(_.name))

  // different


}