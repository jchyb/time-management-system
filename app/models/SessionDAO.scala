package models

import java.time.LocalDateTime

import scala.concurrent.Future

case class Session(token: String, username: String, expiration: LocalDateTime)

trait SessionDAO {
  def getSession(token: String): Future[Option[Session]]
  def generateToken(username: String): Future[String]
  def removeSession(maybeToken: Option[String]): Future[Int]
}
