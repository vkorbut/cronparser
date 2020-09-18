package cron

import java.util.StringTokenizer

import cats.implicits._
import scala.util.Try


case class CronEntry(specification: Seq[CronFieldValue], command: String)

case class CronRange(from: Int, to: Int, step: Int) {
  def flatten: List[Int] = Range.inclusive(from, to, if (step == 0) 1 else step).toList
}

case class CronFieldValue(fieldType: CronFieldType, values: List[CronRange])

case class CronFieldType(name: String, minValue: Int, maxValue: Int) {
  def contains(value: Int): Boolean = value >= minValue && value <= maxValue
}

object CronEntryParser {

  val MinuteField: CronFieldType = CronFieldType("minute", 0, 59)
  val HourField: CronFieldType = CronFieldType("hour", 0, 23)
  val DayOfMonth: CronFieldType = CronFieldType("day of month", 1, 31)
  val DayOfWeek: CronFieldType = CronFieldType("day of week", 1, 7)
  val Month: CronFieldType = CronFieldType("month", 1, 12)

  val cronFields = Seq(
    MinuteField, HourField, DayOfMonth, Month, DayOfWeek
  )

  val DefaultStepValue = 0

  def parseCronString(cronString: String): Either[String, CronEntry] = {
    (for {
      tokenized <- tokenize(cronString)
      (fields, command) = tokenized
      parsedValues <- parseFields(fields.toList)
    } yield CronEntry(parsedValues, command))
      .left
      .map(error => s"$error\nfor input string:\n$cronString")
  }

  def tokenize(cronString: String): Either[String, (Seq[(CronFieldType, String)], String)] = {
    val sc = new StringTokenizer(cronString, " ")
    val fields: Seq[(CronFieldType, String)] = cronFields.map(f => f -> Try(sc.nextToken()).toOption).collect {
      case (fieldInfo, Some(value)) => fieldInfo -> value
    }

    val command = Try(sc.nextToken("").trim).toOption

    if (fields.size != cronFields.size) {
      s"${cronFields.size} fields are expected but only ${fields.size} found".asLeft
    }
    else {
      command.toRight("Command was not specified").map(cmd => fields -> cmd)
    }
  }

  def parseFields(fields: List[(CronFieldType, String)]): Either[String, List[CronFieldValue]] = {
    val results = fields.map { case (field, stringValue) =>
      parseCronField(field, stringValue).map(CronFieldValue(field, _))
    }

    collectErrors(results).left.map(_.mkString("\n"))
  }


  def parseCronField(rangeField: CronFieldType, value: String): Either[String, List[CronRange]] = {
    val ranges = value.split(",").toList.map(parseRangeWithStep(rangeField, _))

    collectErrors(ranges).left.map { errors =>
      val error = errors.mkString(", ")
      s"$error in field $value"
    }
  }

  def parseRangeWithStep(rangeField: CronFieldType, range: String): String Either CronRange = {
    val rangeString :: stepString :: rest = range.split("/").toList.padTo(2, DefaultStepValue.toString)

    (if (rest != Nil) Left(s"Invalid value")
    else for {
      step <- stepString.parseIntOr(s"Invalid step value ($stepString)")
      pair <- parseSimpleRange(rangeField, rangeString)
      (from, to) = pair
    } yield CronRange(from, to, step))
      .left
      .map(error => s"$error for $range")
  }

  def parseSimpleRange(rangeField: CronFieldType, range: String): Either[String, (Int, Int)] = {
    val parsedPair: Option[(Int, Int)] = range.split("-").toList match {
      case "*" :: Nil =>
        Some(rangeField.minValue -> rangeField.maxValue)
      case fromStr :: toStr :: Nil =>
        for {
          from <- fromStr.parseInt
          to <- toStr.parseInt
          if rangeField.contains(from) && rangeField.contains(to)
        } yield from -> to
      case singleValue :: Nil =>
        singleValue.parseInt.filter(rangeField.contains).map(v => v -> v)
      case _ => None
    }

    parsedPair.toRight(s"Invalid range: $range")
  }
}
