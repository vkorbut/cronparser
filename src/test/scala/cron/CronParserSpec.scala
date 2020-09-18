package cron

import cron.CronEntryParser.parseCronString
import org.specs2.matcher.MustMatchers
import org.specs2.mutable._

class CronParserSpec extends Specification with MustMatchers {

  "CronValidator" should {
    "parse valid string" in {
      parseCronString("*/15 0 1,15 * 1-5 /usr/bin/find") must beRight //TODO: check value
    }

    "consume command with spaces" in {
      val command = "command with spaces"
      parseCronString(s"*/15 1 1,15 * 1-5 $command ") must beRight((ce:CronEntry) => ce.command must_==command )
    }
    
    "fail when parsing" >> {

      "not enough fields" in {
        parseCronString("*/15 /usr/bin/find") must beLeft
      }

      "multiple slashes" in {
        parseCronString("*/15 0//5 1,15 * 1-5 /usr/bin/find") must beLeft
      }

      "empty step" in {
        parseCronString("*/15 0/ 1,15 * 1-5 /usr/bin/find") must beLeft
      }

      "negative step" in {
        parseCronString("*/15 0/-1 1,15 * 1-5 /usr/bin/find") must beLeft
      }

      "non number step" in {
        parseCronString("*/15 0/asa 1,15 * 1-5 /usr/bin/find") must beLeft
      }

      "non number field" in {
        parseCronString("*/15 da 1,15 * 1-5 /usr/bin/find") must beLeft
      }

      "empty commas" in {
        parseCronString("*/15 1 1,,15 * 1-5 /usr/bin/find") must beLeft
      }
    }
  }
}
