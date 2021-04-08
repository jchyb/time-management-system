package controllers

import javax.inject._
import models._
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LoginController @Inject()(cc: ControllerComponents, implicit val taskDAO: TaskDAO,
                                implicit val userDAO: UserDAO, implicit val sessionDAO: SessionDAO)
                              (implicit ex: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.LoginController.login())
  }

  def login(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(null, BasicForm.login, BasicForm.register))
  }

  def logout(): Action[AnyContent] = Action.async { implicit request =>
    sessionDAO.removeSession(request.session.get("sessionToken")).map(_ =>
      Redirect(routes.LoginController.login()).discardingCookies(DiscardingCookie("sessionToken")))
  }

  def loginPost(): Action[AnyContent] = Action.async { implicit request =>
    BasicForm.login.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.login(null,formWithErrors, BasicForm.register)))
      },
      formData => {
        userLogin(formData.username, formData.password)
      }
    )
  }

  def registerPost(): Action[AnyContent] = Action.async { implicit request =>
    BasicForm.register.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.login(null, BasicForm.login, formWithErrors)))
      },
      formData => {
        userDAO.getUser(formData.username).map {
          case Some(value) =>
            Future.successful(
              Ok(views.html.login(List("Username already exists. Please choose another."), BasicForm.login, BasicForm.register))
            )
          case None =>
            userDAO.addUser(formData.username, formData.password).map( _ =>
                Ok(views.html.login(List("Registration successful! Please login."), BasicForm.login, BasicForm.register))
              )
        }.flatten
      }
    )
  }

  val logger = Logger(this.getClass)

  def userLogin(username: String, password: String)(implicit request: Request[AnyContent]): Future[Result] = {
      userDAO.getUser(username).flatMap {
        case Some(user) =>
          if(user.password == password) {
            sessionDAO.generateToken(username).map { token =>
              logger.info("ok " + token)
              Redirect(routes.DeckController.home()).withSession("sessionToken" -> token)
            }
          } else {
            Future.successful(
              Ok(views.html.login(List("Incorrect username or password."), BasicForm.login, BasicForm.register))
            )
          }
        case None => Future {Ok(views.html.login(List("Incorrect username or password."), BasicForm.login, BasicForm.register))}
      }
  }

}
