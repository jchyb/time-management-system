package business.database

import javax.inject.Inject
import models.{Task, TaskDAO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import javax.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

class Tasks(tag: Tag) extends Table[Task](tag, "TASKS") {
  def username   = column[String]("USERNAME")
  def taskname = column[String]("NAME")
  def parent   = column[String]("PARENT_NAME")

  def * = (username, taskname, parent) <> (Task.tupled,  Task.unapply)

  def pk = primaryKey("pk_a", (username, taskname))
}

@Singleton
class TaskDbDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
                           implicit executionContext: ExecutionContext)
  extends TaskDAO with HasDatabaseConfigProvider[JdbcProfile]{

  val tasks = TableQuery[Tasks]

  override def listByUser(username: String): Future[List[Task]] = db.run{
    tasks.filter(_.username === username).result
  }.map(_.toList)

  override def create(username: String, name: String, parent: String): Future[Option[Task]] = db.run{
    tasks += Task(username, name, parent)
  }.map(v => if(v>0) Some(Task(username, name, parent)) else None)

  override def get(username: String, name: String): Future[Option[Task]] = db.run{
    tasks.filter(t => t.username === username && t.taskname === name).take(1).result.headOption
  }

  override def delete(username: String, name: String): Future[Boolean] = db.run{
    tasks.filter(t => t.username === username && t.taskname === name ).delete
  }.map(_>0)
}
