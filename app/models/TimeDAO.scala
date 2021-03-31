package models

import java.time.Instant

import scala.concurrent.Future
case class Time(timeID: Long, timeStamp: Instant, taskName: String, username: String)
case class TimeEnd(timeID: Long, timeStamp: Instant)
case class TimeSpan(timeID: Long, timeBegin: Instant, timeEnd: Instant, taskName: String, username: String)

trait TimeDAO {
  def putTime(isBegin: Boolean, timeStamp: Instant, taskName: String, username: String): Future[Option[Time]]
  def removeLatestBegin(username: String): Future[Boolean] // cancel button
  def removeTimes(timeID: Long): Future[Boolean] //remove begin and end times
  def getLatestTime(username: String): Future[(Boolean, Option[Time])] // GET in routes
  def getAllTimes(username : String): Future[List[TimeSpan]]
}
