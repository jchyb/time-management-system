package business.memory

import javax.inject.Singleton
import models.{User, UserDAO}

import scala.collection.mutable
import scala.concurrent.Future

@Singleton
class UserInMemoryDAO extends UserDAO {

  private val users = mutable.Map(
    "admin" -> User("admin", "pass")
  )

  def getUser(username: String): Future[Option[User]] = {
    Future.successful(users.get(username))
  }

  def addUser(username: String, password: String): Future[Option[User]] = {
    if(users.contains(username)) {
      Future.successful(Option.empty)
    } else {
      val user = User(username, password)
      users.put(username, user)
      Future.successful(Option(user))
    }
  }
  def deleteUser(username: String): Future[Int] = {
    Future.successful(users.remove(username) match{
      case None => 1
      case _ => 0
    })
  }
}
