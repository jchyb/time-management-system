package business.memory

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import models.{Session, SessionDAO}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class SessionInMemoryDAO extends SessionDAO {
  def removeSession(maybeToken: Option[String])(implicit ex: ExecutionContext) = maybeToken match {
    case Some(token) => Future{sessions.remove(token)}
    case None => Future{ Option.empty }
  }

  private val sessions = mutable.Map.empty[String, Session]

  def getSession(token: String)(implicit ex: ExecutionContext) : Future[Option[Session]] = {
    Future{sessions.get(token)}
  }

  def generateToken(username: String)(implicit ex: ExecutionContext) : Future[String] = {
    val token = s"$username-${UUID.randomUUID().toString}"
    sessions.put(token, Session(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30)))
    Future{token}
  }

}
