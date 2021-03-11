package business.database
import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import javax.inject.Inject
import models.{Session, SessionDAO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class Sessions(tag: Tag) extends Table[Session](tag, "SESSIONS") {
  def token      = column[String]("TOKEN", O.Unique, O.PrimaryKey)
  def username   = column[String]("USERNAME")
  def expiration = column[LocalDateTime]("EXPIRATION_DATE")

  def * = (token, username, expiration) <> (Session.tupled,  Session.unapply)
}

//TODO DatabaseExecutionContext
class SessionDbDAO @Inject()(@NamedDatabase("SESSIONS") ordersDatabase: Database,
                             protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext)
  extends SessionDAO with HasDatabaseConfigProvider[JdbcProfile] {

  val sessions = TableQuery[Sessions]

  override def getSession(token: String): Future[Option[Session]] = {
    db.run (sessions.filter(_.token === token).take(1).result).map(seq => seq.head).mapTo[Option[Session]]
  }

  override def generateToken(username: String): Future[String] = {
    val token = s"$username-${UUID.randomUUID().toString}"
    db.run(sessions += Session(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30)))
      .map(_ => token)
  }

  override def removeSession(maybeToken: Option[String]): Future[Int] = {
    maybeToken match {
      case None => Future.successful(0)
      case Some(token) => db.run(sessions.filter(_.token === token).delete)
    }
  }

}
