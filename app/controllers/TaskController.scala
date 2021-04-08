package controllers

import javax.inject.Inject
import models.{BasicForm, SessionDAO, Task, TaskDAO, TimeDAO, UserDAO}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, DiscardingCookie}

import scala.concurrent.{ExecutionContext, Future}

class TaskController @Inject()(cc: ControllerComponents,
                               implicit val taskDAO: TaskDAO,
                               implicit val userDAO: UserDAO,
                               implicit val sessionDAO: SessionDAO,
                               implicit val timeDAO: TimeDAO)
                              (implicit ex: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {


  def task(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      taskDAO.listByUser(user.username).map( list =>
        Ok(views.html.task(List(),BasicForm.task, BasicForm.taskDelete, List(Task("Any","Any","Any")) ++ list))
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def taskAddPost(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser { user =>
      BasicForm.task.bindFromRequest().fold({
        formWithErrors =>
          taskDAO.listByUser(user.username).map( list =>
            BadRequest(views.html.task(List(), formWithErrors, BasicForm.taskDelete, List(Task("Any", "Any", "Any")) ++ list))
          )
      },
        formData => {
          if(formData.name != "Any")
            taskDAO.create(user.username, formData.name, formData.parentName).flatMap( _ =>
              taskDAO.listByUser(user.username)
            ).map( list =>
              Ok(views.html.task(List(), BasicForm.task, BasicForm.taskDelete, List(Task("Any", "Any", "Any")) ++ list))
            )
          else {
            val infos = List("Illegal name: \"Any\".")
            taskDAO.listByUser(user.username).map( list =>
              Ok(views.html.task(infos, BasicForm.task, BasicForm.taskDelete, List(Task("Any", "Any", "Any")) ++ list))
            )
          }
        }
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def taskDelPost(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      BasicForm.taskDelete.bindFromRequest().fold(
        formWithErrors => {
          taskDAO.listByUser(user.username).map( list =>
            BadRequest(views.html.task(List(),BasicForm.task, formWithErrors, List(Task("Any","Any","Any")) ++ list))
          )
        },
        formData => {
          taskDAO.delete(user.username, formData.name)
            .flatMap( _ =>
              taskDAO.listByUser(user.username).map( list =>
                Ok(views.html.task(List(),BasicForm.task, BasicForm.taskDelete, List(Task("Any","Any","Any")) ++ list))
              )
            )
        }
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

}
