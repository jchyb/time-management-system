package business.memory

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

import models.{Session, SessionDAO}

import scala.collection.mutable
import scala.concurrent.Future
import javax.inject.Singleton

@Singleton
class SessionInMemoryDAO extends SessionDAO {
  private val sessions = mutable.Map.empty[String, Session]

  def getSession(token: String): Future[Option[Session]] = {
    Future.successful(sessions.get(token))
  }

  def generateToken(username: String): Future[String] = {
    val token = s"$username-${UUID.randomUUID().toString}"
    sessions.put(token, Session(token, username, LocalDateTime.now(ZoneOffset.UTC).plusSeconds(3000))) //TODO change seconds
    Future.successful(token)
  }

  def removeSession(maybeToken: Option[String]): Future[Int] = maybeToken match {
    case Some(token) => Future.successful(sessions.remove(token).fold(0)(_=> 1))
    case None => Future.successful(0)
  }

}
