package business.database

import java.time.Instant
import javax.inject.{Inject, Singleton}
import models.{Time, TimeDAO, TimeEnd, TimeSpan}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import java.sql.Timestamp

import scala.concurrent.{ExecutionContext, Future}

class TimesBegin(tag: Tag) extends Table[Time](tag, "TIMES_BEGIN") {
  def timeID   = column[Long]("ID", O.PrimaryKey, O.Unique, O.AutoInc)
  def timeStamp = column[Instant]("TIME_STAMP")
  def taskName   = column[String]("TASK_NAME")
  def userName   = column[String]("USERNAME")

  def * = (timeID, timeStamp, taskName, userName) <> (Time.tupled,  Time.unapply)
}

class TimesEnd(tag: Tag) extends Table[TimeEnd](tag, "TIMES_END") {
  def timeID   = column[Long]("ID", O.PrimaryKey, O.Unique)
  def timeStamp = column[Instant]("TIME_STAMP")

  def * = (timeID, timeStamp) <> (TimeEnd.tupled,  TimeEnd.unapply)
}

@Singleton
class TimeDbDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext) extends TimeDAO with HasDatabaseConfigProvider[JdbcProfile]{

  val timesBegin = TableQuery[TimesBegin]
  val timesEnd = TableQuery[TimesEnd]

  implicit val instantColumnType: BaseColumnType[Instant] =
    MappedColumnType.base[Instant, Timestamp](
      instant => Timestamp.from(instant),
      ts => ts.toInstant
    )

  override def putTime(isBegin: Boolean, timeStamp: Instant, taskName: String, username: String): Future[Option[Time]] = {
    if(isBegin){
      db.run((timesBegin returning timesBegin.map(_.timeID)) += Time(0, timeStamp, taskName, username))
        .map(id => Option(Time(id, timeStamp, taskName, username)))
    } else {
      db.run(timesBegin.filter(a => a.taskName === taskName && a.userName === a.userName)
        .sortBy(_.timeStamp).result)
        .map(_.headOption)
    }
  }

  override def removeLatestBegin(username: String): Future[Boolean] = {
    db.run(timesBegin.filter(_.userName === username)
      .sortBy(_.timeStamp).take(1).delete
    ).map( _ > 0 )
  }

  override def removeTimes(timeID: Long): Future[Boolean] = {
    db.run( timesEnd.filter(_.timeID === timeID).delete )
      .flatMap(_ =>
        db.run( timesBegin.filter(_.timeID === timeID).delete )
        .map(_ == 1)
      )
  }

  val joinedQuery: Query[(TimesBegin, Rep[Option[TimesEnd]]), (Time, Option[TimeEnd]), Seq]
  = timesBegin joinLeft timesEnd on (_.timeID === _.timeID)

  override def getLatestTime(username: String): Future[(Boolean, Option[Time])] = {
    db.run(
      joinedQuery.filter(_._1.userName === username)
      .sortBy(_._1.timeStamp)
      .result.headOption
    ).map{
      case None => (true, None)
      case Some((begin, maybeEnd)) =>
        maybeEnd match {
          case Some(value) => (true, Option(Time(0, value.timeStamp, begin.taskName, begin.username)))
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
