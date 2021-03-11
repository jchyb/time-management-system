package models

import java.time.LocalDateTime

import business.memory.SessionInMemoryDAO
import com.google.inject.ImplementedBy

import scala.concurrent.Future

case class Session(token: String, username: String, expiration: LocalDateTime)

@ImplementedBy(classOf[SessionInMemoryDAO])
trait SessionDAO {
  def getSession(token: String): Future[Option[Session]]
  def generateToken(username: String): Future[String]
  def removeSession(maybeToken: Option[String]): Future[Int]
}
