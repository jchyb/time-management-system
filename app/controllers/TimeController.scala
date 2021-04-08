package controllers

import java.time.Instant

import javax.inject.Inject
import models.{BasicForm, SessionDAO, Task, TaskDAO, TimeDAO, UserDAO}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, DiscardingCookie, Request}

import scala.concurrent.{ExecutionContext, Future}

class TimeController @Inject()(cc: ControllerComponents,
                               implicit val taskDAO: TaskDAO,
                               implicit val userDAO: UserDAO,
                               implicit val sessionDAO: SessionDAO,
                               implicit val timeDAO: TimeDAO)
                              (implicit ex: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {


  def time(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      taskDAO.listByUser(user.username).flatMap { list =>
        timeDAO.getLatestTime(user.username).map { time =>
          Ok(views.html.time(BasicForm.timer, List(Task("Any","Any","Any")) ++ list, time._1, time._2))
        }
      }
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def timerBeginPost(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Auth.withUser { user =>
      BasicForm.timer.bindFromRequest().fold(
        formWithErrors => {
          timeDAO.getLatestTime(user.username).map { time =>
            taskDAO.listByUser(user.username).map { list =>
              BadRequest(views.html.time(formWithErrors, List(Task("Any", "Any", "Any")) ++ list, time._1, time._2))
            }
          }.flatten
        },
        formData => {
          timeDAO.putTime(isBegin = true, Instant.now(), formData.taskname, user.username)
            .flatMap( _ =>
              Future.successful(Redirect(routes.TimeController.time()))
            )
        }
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def timerEndPost(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Auth.withUser { user =>
      BasicForm.timer.bindFromRequest().fold(
        formWithErrors => {
          timeDAO.getLatestTime(user.username).map { time =>
            taskDAO.listByUser(user.username).map { list =>
              BadRequest(views.html.time(formWithErrors, List(Task("Any", "Any", "Any")) ++ list, time._1, time._2))
            }
          }.flatten
        },
        formData => {
          timeDAO.putTime(isBegin = false, Instant.now(), formData.taskname, user.username)
            .flatMap( _ =>
              Future.successful(Redirect(routes.TimeController.time()))
            )
        }
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def timerCancelPost(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Auth.withUser { user =>
      timeDAO.getLatestTime(user.username)
        .map(a => timeDAO.removeTimes(a._2.get.timeID))
        .flatMap(_ => Future.successful(Redirect(routes.TimeController.time())) )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

}
