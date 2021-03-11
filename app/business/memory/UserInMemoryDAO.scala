package business.memory

import java.util.concurrent.atomic.AtomicLong

import models.{User, UserDAO}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}


class UserInMemoryDAO extends UserDAO {
  private val seq = new AtomicLong

  private val users = mutable.Map(
    "admin" -> User(0,"admin", "pass")
  )

  def getUser(username: String)(implicit c: ExecutionContext): Future[Option[User]] = {
    Future.successful(users.get(username))
  }

  def addUser(username: String, password: String): Future[Option[User]] = {
    val id = seq.incrementAndGet()
    if(users.contains(username)) {
      Future.successful(Option.empty)
    } else {
      val user = User(id, username, password)
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
