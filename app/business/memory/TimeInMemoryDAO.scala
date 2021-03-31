package business.memory

import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

import javax.inject.Singleton
import models.{Time, TimeDAO, TimeSpan}
import play.api.Logger

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

@Singleton
class TimeInMemoryDAO extends TimeDAO{

  val logger = Logger(this.getClass)

  val seq: AtomicLong = new AtomicLong(0)
  val timesBegin: TrieMap[Long, Time] = TrieMap.empty[Long, Time]
  val timesEnd: TrieMap[Long, Time] = TrieMap.empty[Long, Time]

  override def putTime(isBegin: Boolean, timeStamp: Instant,
                       taskName: String, username: String): Future[Option[Time]] = {

    if(isBegin) {
      val id = seq.getAndIncrement()
      val time = Time(id, timeStamp, taskName, username)
      timesBegin.addOne(id, time)
      logger.info("Added: " + time.toString) //TODO remove
      Future.successful(Option(time))
    } else {
      timesBegin.values.filter(_.username == username).toList //TODO verify !!!!!!!!!
        .sortBy(_.timeStamp)(Ordering[Instant].reverse).map(_.timeID).headOption match {
        case Some(value) =>
          val time = Time(value, timeStamp, taskName, username)
          logger.info("Added 2: " + time.toString) //TODO remove
          timesEnd.addOne(value, time)
          Future.successful(Option(time))
        case None => Future.successful(None)
      }
    }
  }
  //TODO remove sortBy
  override def removeLatestBegin(username: String): Future[Boolean] = {
    timesBegin.values.filter(_.username == username).toList.sortBy(_.timeStamp)(Ordering[Instant].reverse).headOption.map(_.timeID) match {
      case Some(value) =>
        timesBegin.remove(value)
        Future.successful(true)
      case None => Future.successful(false)
    }
  }

  override def removeTimes(timeID: Long): Future[Boolean] = {
    Future.successful(timesBegin.remove(timeID).isDefined || timesEnd.remove(timeID).isDefined)
  }

  override def getLatestTime(username: String): Future[(Boolean, Option[Time])] = {
    val lastBegin = timesBegin.values.filter(_.username == username).toList.sortBy(_.timeStamp)(Ordering[Instant].reverse).headOption
    val lastEnd = timesEnd.values.filter(_.username == username).toList.sortBy(_.timeStamp)(Ordering[Instant].reverse).headOption
    logger.info("begin:" + lastBegin)
    logger.info("end:" + lastEnd)
    if(lastBegin.isEmpty) {
      return Future.successful((true, lastBegin))
    }
    if(lastEnd.flatMap(e => lastBegin.map(b => b.timeID == e.timeID)).getOrElse(false)) {
      return Future.successful((true, lastEnd))
    }
    Future.successful((false, lastBegin))
  }


  // queries
  override def getAllTimes(username: String) : Future[List[TimeSpan]] = {
    Future.successful(
      timesBegin.values.filter(_.username == username).map( begin =>
        timesEnd.get(begin.timeID) match {
          case Some(end) => TimeSpan(begin.timeID, begin.timeStamp, end.timeStamp, begin.taskName, begin.username)
          case None => TimeSpan(begin.timeID, begin.timeStamp, null, begin.taskName, begin.username)
        }
      ).toList
    )
  }
}
