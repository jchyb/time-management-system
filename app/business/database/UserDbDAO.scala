package business.database

import javax.inject.Inject
import models.{User, UserDAO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def userID     = column[Long]("USER_ID", O.AutoInc, O.Unique, O.PrimaryKey)
  def username = column[String]("USERNAME", O.Unique)
  def password = column[String]("PASSWORD")

  def * = (username, password, userID) <> (User.tupled,  User.unapply)
}

class UserDbDAO @Inject()(@NamedDatabase("USERS") ordersDatabase: Database,
                         protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext)
  extends UserDAO with HasDatabaseConfigProvider[JdbcProfile]{

  val users = TableQuery[Users]

  override def getUser(username: String): Future[Option[User]] = db.run(
    users.filter(_.username === username).take(1).result).map(seq =>
      if(seq.isEmpty) None
      else Option(seq.head) ).mapTo[Option[User]]

  override def addUser(username: String, password: String): Future[Option[User]] = {
    db.run(users returning users.map(_.userID) += User(username, password)).map(id =>
      Option(User(username, password, id))
    )
  }

  override def deleteUser(username: String): Future[Int] = {
    db.run(users.filter(_.username === username).delete)
  }
}
