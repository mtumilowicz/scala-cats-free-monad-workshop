import cats.data.EitherK

package object pt5_composing {
  type CatsApp[A] = EitherK[ConsoleReader, ConsolePrinter, A]
}
