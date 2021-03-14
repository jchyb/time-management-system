package modules

import business.database.{SessionDbDAO, TaskDbDAO, UserDbDAO}
import business.memory.{SessionInMemoryDAO, TaskInMemoryDAO, UserInMemoryDAO}
import com.google.inject.AbstractModule
import models.{SessionDAO, TaskDAO, UserDAO}
import play.api.{Configuration, Environment, Mode}

class GuiceModule (environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    if(environment.mode == Mode.Dev) {
      bind(classOf[SessionDAO]).to(classOf[SessionInMemoryDAO])
      bind(classOf[TaskDAO]).to(classOf[TaskInMemoryDAO])
      bind(classOf[UserDAO]).to(classOf[UserInMemoryDAO])
    } else {
      bind(classOf[SessionDAO]).to(classOf[SessionDbDAO])
      bind(classOf[TaskDAO]).to(classOf[TaskDbDAO])
      bind(classOf[UserDAO]).to(classOf[UserDbDAO])

      //bind(classOf[ApplicationStart]).asEagerSingleton()
    }
  }
}
