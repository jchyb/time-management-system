package models

import java.time.LocalDateTime

import business.memory.SessionInMemoryDAO
import com.google.inject.ImplementedBy

import scala.concurrent.{ExecutionContext, Future}

case class Session(token: String, username: String, expiration: LocalDateTime)

@ImplementedBy(classOf[SessionInMemoryDAO])
trait SessionDAO {
  def getSession(token: String)(implicit c: ExecutionContext): Future[Option[Session]]
  def generateToken(username: String)(implicit c: ExecutionContext) : Future[String]
  def removeSession(maybeToken: Option[String])(implicit c: ExecutionContext)  : Future[Option[Session]]
}
