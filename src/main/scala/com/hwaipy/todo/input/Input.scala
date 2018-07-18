package com.hwaipy.todo.input

import java.time.LocalDateTime
import java.time.LocalDate
import java.util.regex.Pattern

import com.hwaipy.todo.action.Events

object Input {
  private val PATTERN_XH = Pattern.compile("^([0-9]+) *h(our)?$")
  private val PATTERN_XD = Pattern.compile("^([0-9]+) *d(ay)?$")
  private val PATTERN_XW = Pattern.compile("^([0-9]+) *w(week)?$")
  private val PATTERN_XM = Pattern.compile("^([0-9]+) *m(onth)?$")
  private val DAY_OF_WEEKS = Map("mon" -> 1, "monday" -> 1, "tue" -> 2, "tuesday" -> 2, "wed" -> 3, "wednesday" -> 3, "thu" -> 4, "thursday" -> 4, "fri" -> 5, "friday" -> 5, "sat" -> 6, "saturday" -> 6, "sun" -> 7, "sunday" -> 7)
  private val PATTERN_DATETIME = Pattern.compile("^([0-9]+)-([0-9]+)-([0-9]+) ([0-9]+):([0-9]+)$")

  def stringToDateTime(str: String): LocalDateTime = {
    str.trim.toLowerCase match {
      case "today" => getTimeOfTheDay(LocalDateTime.now())
      case "tomorrow" => getTimeOfTheDay(LocalDateTime.now().plusDays(1))
      case s if find(PATTERN_XD, s) != None => getTimeOfTheDay(LocalDateTime.now().plusDays(find(PATTERN_XD, s, 1).get.toInt))
      case s if find(PATTERN_XW, s) != None => getTimeOfTheDay(LocalDateTime.now().plusWeeks(find(PATTERN_XW, s, 1).get.toInt))
      case s if find(PATTERN_XM, s) != None => getTimeOfTheDay(LocalDateTime.now().plusMonths(find(PATTERN_XM, s, 1).get.toInt))
      case s if find(PATTERN_XH, s) != None => LocalDateTime.now().plusHours(find(PATTERN_XH, s, 1).get.toInt)
      case s if DAY_OF_WEEKS.contains(s) => getTimeOfTheDay(getNextDayOfWeek(LocalDateTime.now(), DAY_OF_WEEKS(s)))
      case s if find(PATTERN_DATETIME, s) != None => {
        val matcher = PATTERN_DATETIME.matcher(s)
        matcher.find()
        try {
          LocalDateTime.of(matcher.group(1).toInt, matcher.group(2).toInt, matcher.group(3).toInt, matcher.group(4).toInt, matcher.group(5).toInt)
        } catch {
          case e: Throwable => Events.INVALID_TIME_STAMP
        }
      }
      case _ => Events.INVALID_TIME_STAMP
    }
  }

  private def getTimeOfTheDay(localDateTime: LocalDateTime) = LocalDate.from(localDateTime).atStartOfDay().plusHours(17)

  private def getNextDayOfWeek(localDateTime: LocalDateTime, dayOfWeek: Int) = localDateTime.plusDays((dayOfWeek + 7 - localDateTime.getDayOfWeek.getValue) % 7)

  private def find(pattern: Pattern, str: String, retIndex: Int = 0) = {
    val matcher = pattern.matcher(str)
    matcher.find() match {
      case true => Some(matcher.group(retIndex))
      case false => None
    }
  }
}