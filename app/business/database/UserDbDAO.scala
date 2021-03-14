package business.database

import javax.inject.Inject
import models.{User, UserDAO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import javax.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def username = column[String]("LOGIN", O.Unique)
  def password = column[String]("PASSWORD")

  def * = (username, password) <> (User.tupled,  User.unapply)
}

@Singleton
class UserDbDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext)
  extends UserDAO with HasDatabaseConfigProvider[JdbcProfile]{

  val users = TableQuery[Users]

  override def getUser(username: String): Future[Option[User]] = db.run(
    users.filter(_.username === username).take(1).result).map(seq =>
      if(seq.isEmpty) None
      else Option(seq.head) ).mapTo[Option[User]]

  override def addUser(username: String, password: String): Future[Option[User]] = {
    db.run(users += User(username, password)).map(_ =>
      Option(User(username, password))
    )
  }

  override def deleteUser(username: String): Future[Int] = {
    db.run(users.filter(_.username === username).delete)
  }
}
