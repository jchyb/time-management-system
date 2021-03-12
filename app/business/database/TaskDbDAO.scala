package business.database

import javax.inject.Inject
import models.{Task, TaskDAO}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.db.NamedDatabase
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._
import slick.lifted.Tag

import scala.concurrent.{ExecutionContext, Future}

class Tasks(tag: Tag) extends Table[Task](tag, "TASKS") {
  def userID   = column[Long]("USER_ID", O.PrimaryKey)
  def taskname = column[String]("TASKNAME", O.Unique, O.PrimaryKey)
  def parent   = column[String]("PARENT")

  def * = (userID, taskname, parent) <> (Task.tupled,  Task.unapply)
}

class TaskDbDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(
                           implicit executionContext: ExecutionContext)
  extends TaskDAO with HasDatabaseConfigProvider[JdbcProfile]{

  val tasks = TableQuery[Tasks]

  override def listByUser(userID: Long): Future[List[Task]] = db.run{
    tasks.filter(_.userID === userID).result
  }.map(_.toList)

  override def create(userID: Long, name: String): Future[Option[Task]] = db.run{
    tasks += Task(userID, name, "Any")
  }.map(v => if(v>0) Some(Task(userID, name, "Any")) else None)

  override def get(userID: Long, name: String): Future[Option[Task]] = db.run{
    tasks.filter(t => t.userID === userID && t.taskname === name).take(1).result
  }.map(_.head).mapTo[Option[Task]]
  //TODO ?????

  override def delete(userID: Long, name: String): Future[Boolean] = db.run{
    tasks.filter(t => t.userID === userID && t.taskname === name ).delete
  }.map(_>0)
}
