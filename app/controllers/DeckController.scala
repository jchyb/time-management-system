package controllers

import javax.inject.Inject
import models._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class DeckController @Inject()(cc: ControllerComponents,
                               implicit val taskDAO: TaskDAO,
                               implicit val userDAO: UserDAO,
                               implicit val sessionDAO: SessionDAO,
                               implicit val timeDAO: TimeDAO)
                              (implicit ex: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def home(): Action[AnyContent] = Action.async { implicit request =>
    Auth.withUser{user =>
      Future.successful(Ok(views.html.home(user.username)))
    }{
      Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
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
