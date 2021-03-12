package models

import scala.concurrent.Future

case class User(username: String, password: String, userID: Long = 0L)

trait UserDAO {
  def getUser(username: String): Future[Option[User]]
  def addUser(username: String, password: String): Future[Option[User]]
  def deleteUser(username: String): Future[Int]
}


