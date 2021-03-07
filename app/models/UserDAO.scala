package models

import business.memory.UserInMemoryDAO
import com.google.inject.ImplementedBy

import scala.concurrent.{ExecutionContext, Future}

case class User(userID: Long, username: String, password: String)

@ImplementedBy(classOf[UserInMemoryDAO])
trait UserDAO {
  def getUser(username: String)(implicit ex: ExecutionContext) : Future[Option[User]]
  def addUser(username: String, password: String): Option[User]
  def deleteUser(username: String): Option[User]
}


