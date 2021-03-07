package models

import java.time.LocalDateTime
case class Times(timeID: Long, startTime: LocalDateTime, endTime: LocalDateTime, taskName: String, userID: Long)
trait TimesDAO {
  def putTimes(startTime: LocalDateTime, endTime: LocalDateTime, taskName: String, userID: Long)
  def removeTimes(timeID: Long, userID: Long)
}
