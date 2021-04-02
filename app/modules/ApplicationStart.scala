package modules
import business.database.{Sessions, Tasks, TimesBegin, TimesEnd, Users}

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import play.api.Logger
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.inject.ApplicationLifecycle
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

@Singleton
class ApplicationStart @Inject()(lifecycle: ApplicationLifecycle, protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  val logger = Logger(this.getClass)

  //create table
  val sessions = TableQuery[Sessions]
  val users = TableQuery[Users]
  val tasks = TableQuery[Tasks]

  val timesBegin = TableQuery[TimesBegin]
  val timesEnd = TableQuery[TimesEnd]

  val createAll =
    (sessions.schema ++
    users.schema ++
    tasks.schema ++
    timesBegin.schema ++
    timesEnd.schema)
  createAll.createIfNotExistsStatements.foreach(logger.info(_))
  db.run(createAll.create)

  // Shut-down hook
  lifecycle.addStopHook { () =>
    Future.successful(())
  }

}
