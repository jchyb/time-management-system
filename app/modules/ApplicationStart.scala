package modules
import business.database.{Sessions, Tasks, TimesBegin, TimesEnd, Users}

import scala.concurrent.{ExecutionContext, Future}
import javax.inject._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.inject.ApplicationLifecycle
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api._

@Singleton
class ApplicationStart @Inject()(lifecycle: ApplicationLifecycle, protected val dbConfigProvider: DatabaseConfigProvider)(
  implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  //create table
  val sessions = TableQuery[Sessions]
  val users = TableQuery[Users]
  val tasks = TableQuery[Tasks]

  val timesBegin = TableQuery[TimesBegin]
  val timesEnd = TableQuery[TimesEnd]

  db.run(sessions.schema.createIfNotExists)
    .flatMap(_ => db.run(users.schema.createIfNotExists))
    .flatMap(_ => db.run(tasks.schema.createIfNotExists))
    .flatMap(_ => db.run(timesBegin.schema.createIfNotExists))
    .flatMap(_ => db.run(timesEnd.schema.createIfNotExists))


  // Shut-down hook
  lifecycle.addStopHook { () =>
    Future.successful(())
  }

}
