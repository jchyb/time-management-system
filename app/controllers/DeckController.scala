package controllers

import java.time.Instant

import javax.inject.Inject
import models._
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class DeckController @Inject()(cc: ControllerComponents, implicit val taskDAO: TaskDAO,
                               implicit val userDAO: UserDAO, implicit val sessionDAO: SessionDAO,
                               implicit val timeDAO: TimeDAO)
                              (implicit ex: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def simpleFormPost(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser { user =>
      BasicForm.task.bindFromRequest().fold({
        logger.info("hoh")
        formWithErrors =>
          taskDAO.listByUser(user.username).map( list =>
            BadRequest(views.html.task(formWithErrors, BasicForm.task, List(Task("Any", "Any", "Any")) ++ list))
          )}
        ,
        formData => {
          logger.info("heh")
          taskDAO.create(user.username, formData.name, formData.parentName).flatMap(_ =>
            taskDAO.listByUser(user.username)
          ).map(list =>
            Ok(views.html.task(BasicForm.task, BasicForm.task, List(Task("Any", "Any", "Any")) ++ list))
          )
        }
      )
    }{Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken" )))}
  }

  def simpleFormPostDel(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      BasicForm.task.bindFromRequest().fold(
        formWithErrors => {
          taskDAO.listByUser(user.username).map( list =>
            BadRequest(views.html.task(BasicForm.task, formWithErrors, List(Task("Any","Any","Any")) ++ list))
          )
        },
        formData => {
          taskDAO.delete(user.username, formData.name)
          .flatMap( _ =>
            taskDAO.listByUser(user.username).map( list =>
              Ok(views.html.task(BasicForm.task, BasicForm.task, List(Task("Any","Any","Any")) ++ list))
            )
          )
        }
      )
    }{Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))}
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
              Future.successful(Redirect(routes.DeckController.time()))
            )
        }
      )
    }{Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))}
  }
  val logger = Logger(this.getClass)
  def timerEndPost(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Auth.withUser { user =>
      BasicForm.timer.bindFromRequest().fold(
        formWithErrors => {
          logger.info("ehh")
          timeDAO.getLatestTime(user.username).map { time =>
            taskDAO.listByUser(user.username).map { list =>
              BadRequest(views.html.time(formWithErrors, List(Task("Any", "Any", "Any")) ++ list, time._1, time._2))
            }
          }.flatten
        },
        formData => {
          logger.info("cos")
          timeDAO.putTime(isBegin = false, Instant.now(), formData.taskname, user.username)
            .flatMap( _ =>
              Future.successful(Redirect(routes.DeckController.time()))
            )
        }
      )
    }{Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))}
  }

  //TODO improve/fix
  def timerCancelPost(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    Auth.withUser { user =>
        timeDAO.getLatestTime(user.username)
          .map(a => timeDAO.removeTimes(a._2.get.timeID))
        .flatMap(_ => Future.successful(Redirect(routes.DeckController.time())) )
    }{Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))}
  }

  def priv(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser(user => Future.successful(Ok(views.html.priv(user.username))))(
      Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    )
  }

  // GET Routes
  def time(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      taskDAO.listByUser(user.username).map { list =>
        timeDAO.getLatestTime(user.username).map { time =>
          Ok(views.html.time(BasicForm.timer, List(Task("Any","Any","Any")) ++ list, time._1, time._2))
        }
      }.flatten
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def task(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      taskDAO.listByUser(user.username).map( list =>
        Ok(views.html.task(BasicForm.task, BasicForm.task, List(Task("Any","Any","Any")) ++ list))
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }

  def stat(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{ user =>
      timeDAO.getAllTimes(user.username).map( spanList =>
        Ok(views.html.stat(spanList))
      )
    }{
      Future.successful(Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
    }
  }
}
