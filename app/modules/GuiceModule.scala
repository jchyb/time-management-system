package modules

import business.memory.{SessionInMemoryDAO, TaskInMemoryDAO, UserInMemoryDAO}
import com.google.inject.AbstractModule
import models.{SessionDAO, TaskDAO, UserDAO}
import play.api.{Configuration, Environment}

class GuiceModule (environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    bind(classOf[SessionDAO]).to(classOf[SessionInMemoryDAO])
    bind(classOf[TaskDAO]).to(classOf[TaskInMemoryDAO])
    bind(classOf[UserDAO]).to(classOf[UserInMemoryDAO])
  }
}
