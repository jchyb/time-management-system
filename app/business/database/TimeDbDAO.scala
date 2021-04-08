package business.database

import java.time.Instant

import javax.inject.{Inject, Singleton}
import models.{Time, TimeDAO, TimeEnd, TimeSpan}
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class TimesBegin(tag: Tag) extends Table[Time](tag, "TIMES_BEGIN") {
  def timeID   = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def timeStamp = column[Instant]("TIME_STAMP")
  def taskName   = column[String]("TASK_NAME")
  def userName   = column[String]("USERNAME")

  def * = (timeID, timeStamp, taskName, userName) <> (Time.tupled,  Time.unapply)
}

class TimesEnd(tag: Tag) extends Table[TimeEnd](tag, "TIMES_END") {
  def timeID   = column[Long]("ID", O.PrimaryKey)
  def timeStamp = column[Instant]("TIME_STAMP")

  def * = (timeID, timeStamp) <> (TimeEnd.tupled,  TimeEnd.unapply)
}

@Singleton
class TimeDbDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext) extends TimeDAO with HasDatabaseConfigProvider[JdbcProfile]{

  val timesBegin = TableQuery[TimesBegin]
  val timesEnd = TableQuery[TimesEnd]
  val logger = Logger(this.getClass())
  override def putTime(isBegin: Boolean, timeStamp: Instant, taskName: String, username: String): Future[Unit] = {
    if (isBegin) {
      db.run((timesBegin += Time(0, timeStamp, taskName, username))).map(_ => {})
    } else {
      db.run(
        timesBegin.filter(a => a.taskName === taskName && a.userName === a.userName)
          .sortBy(_.timeStamp.desc).result.headOption
          .flatMap[Unit, NoStream, Effect.Write] {
            case Some(begin) => (timesEnd += TimeEnd(begin.timeID, timeStamp)).map(_ => {})
            case None => DBIO.successful(None)
          }
      )
    }
  }

  override def removeTimes(timeID: Long): Future[Boolean] = {
    db.run( timesEnd.filter(_.timeID === timeID).delete )
      .flatMap(_ =>
        db.run( timesBegin
          .filter(_.timeID === timeID)
          .delete
        ).map(_ == 1)
      )
  }

  val joinedQuery: Query[(TimesBegin, Rep[Option[TimesEnd]]), (Time, Option[TimeEnd]), Seq]
  = timesBegin joinLeft timesEnd on (_.timeID === _.timeID)

  override def getLatestTime(username: String): Future[(Boolean, Option[Time])] = {
    db.run(
      joinedQuery.filter(_._1.userName === username)
      .sortBy(_._1.timeStamp.desc)
      .result.headOption
    ).map{
      case None => (true, None)
      case Some((begin, maybeEnd)) =>
        maybeEnd match {
          case Some(end) => {
            (true, Option(Time(end.timeID, end.timeStamp, begin.taskName, begin.username)))
          }
          case None => (false, Option(begin))
        }
    }
  }

  override def getAllTimes(username: String): Future[List[TimeSpan]] = {
    db.run(joinedQuery.result).map( a => a.toList
      .map( tuple =>
        tuple._2 match {
          case None =>
            TimeSpan(tuple._1.timeID, tuple._1.timeStamp, null, tuple._1.taskName, tuple._1.username)
          case Some(end) =>
            TimeSpan(tuple._1.timeID, tuple._1.timeStamp, end.timeStamp, tuple._1.taskName, tuple._1.username)
        }
      )
    )
  }
}
