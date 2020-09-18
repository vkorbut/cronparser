import scala.util.Try
import cats.implicits._

package object cron {

  implicit class StringParseOps(val value: String) extends AnyVal {
    def parseInt: Option[Int] =
      Try(Integer.parseUnsignedInt(value)).toOption

    def parseIntOr(error: => String): String Either Int =
      parseInt.toRight(error)
  }

  def collectErrors[T](values: List[Either[String, T]]): Either[List[String], List[T]] = {
    val errors = values.collect { case Left(error) => error }
    if (errors.nonEmpty) {
      errors.asLeft
    } else {
      values.collect { case Right(value) => value }.asRight
    }
  }
}
