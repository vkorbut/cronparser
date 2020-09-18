import cron.{CronEntry, CronEntryParser}

object Main extends App {

  def validateArguments: String Either String = {
    args.headOption.toRight(s"please specify one argument which is cron line")
  }

  def printCronEntry(validResult: CronEntry): Unit = {
    validResult.specification.foreach { v =>
      print(v.fieldType.name.padTo(14, ' '))
      println(v.values.flatMap(_.flatten).sorted.mkString(" "))
    }
    print("command".padTo(14, ' '))
    println(validResult.command)
  }

  validateArguments.flatMap(CronEntryParser.parseCronString).fold(
    error => System.err.println(error),
    validResult => printCronEntry(validResult)
  )

}
