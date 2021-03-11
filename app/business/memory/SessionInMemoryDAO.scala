package business.memory

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import models.{Session, SessionDAO}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class SessionInMemoryDAO extends SessionDAO {
  private val sessions = mutable.Map.empty[String, Session]

  def getSession(token: String)(implicit ex: ExecutionContext) : Future[Option[Session]] = {
    Future{sessions.get(token)}
  }

  def generateToken(username: String)(implicit ex: ExecutionContext) : Future[String] = {
    val token = s"$username-${UUID.randomUUID().toString}"
    sessions.put(token, Session(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30)))
    Future{token}
  }

  def removeSession(maybeToken: Option[String])(implicit ex: ExecutionContext): Future[Int] = maybeToken match {
    case Some(token) => Future.successful(sessions.remove(token).fold(0)(_=> 1))
    case None => Future.successful(0)
  }

}
