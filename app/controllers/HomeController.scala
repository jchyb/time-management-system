package controllers

import java.time.{LocalDateTime, ZoneOffset}

import javax.inject._
import models._
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, val taskDAO: TaskDAO, val userDAO: UserDAO, val sessionDAO: SessionDAO)
                              (implicit ex: ExecutionContext)
  extends AbstractController(cc) with play.api.i18n.I18nSupport {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  /**
   * No persistent data so far. This will be changed later.
   *
   */
  /////////////////////////// Forms - see routes on how to access

  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Redirect(routes.HomeController.login())
  }

  def simpleFormPost(): Action[AnyContent] = Action.async { implicit request =>
    withUser(user =>
      BasicForm.task.bindFromRequest().fold(
        formWithErrors =>
          taskDAO.listByUser(user.userID).map( list =>
            BadRequest(views.html.basicForm(formWithErrors, BasicForm.task, List(Task(0,"Any","Any")) ++ list))
          )
        ,
        formData => {
          taskDAO.create(user.userID,formData.name).flatMap( _ =>
            taskDAO.listByUser(user.userID)
          ).map( list =>
            Ok(views.html.basicForm(BasicForm.task, BasicForm.task, List(Task(0,"Any","Any")) ++ list))
          )
        }
      )
    )(Future.successful(Redirect(routes.HomeController.login()).withSession("sessionToken" -> null)))
  }
  def simpleFormPostDel(): Action[AnyContent] = Action.async { implicit request =>
    withUser(user =>
      BasicForm.task.bindFromRequest().fold(
        formWithErrors => {
          taskDAO.listByUser(user.userID).map( list =>
            BadRequest(views.html.basicForm(BasicForm.task, formWithErrors, List(Task(0,"Any","Any")) ++ list))
          )
        },
        formData => {
          taskDAO.delete(user.userID, formData.name)
          taskDAO.listByUser(user.userID).map( list =>
            Ok(views.html.basicForm(BasicForm.task, BasicForm.task, List(Task(0,"Any","Any")) ++ list))
          )
        }
      )
    )(Future.successful(Redirect(routes.HomeController.login()).withSession("sessionToken" -> null)))
  }
  def forms(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    withUser{user =>
      taskDAO.listByUser(user.userID).map( list =>
        Ok(views.html.basicForm(BasicForm.task, BasicForm.task, List(Task(0,"Any","Any")) ++ list ))
      )
    }{
      Future.successful(Redirect(routes.HomeController.login()).withSession("sessionToken" -> null))
    }
  }

  /////////////////////////////////////////////////// Login

  def login(): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.login(null, BasicForm.login, BasicForm.register))
  }

  def logout(): Action[AnyContent] = Action.async { implicit request =>
    sessionDAO.removeSession(request.session.get("sessionToken")).map(_ =>
      Redirect(routes.HomeController.login()).withSession("sessionToken" -> null))
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
        //TODO check if login exists!!!!!
        userDAO.addUser(formData.username, formData.password)
        Future.successful(
          Ok(views.html.login(List("Registration successful! Please login."), BasicForm.login, BasicForm.register)))
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
              Redirect(routes.HomeController.priv()).withSession("sessionToken" -> token)
            }
          } else {
            Future.successful(
              Ok(views.html.login(List("Incorrect username or password."), BasicForm.login, BasicForm.register))
            )
          }
        case None => Future {Ok(views.html.login(List("Incorrect username or password."), BasicForm.login, BasicForm.register))}
      }
  }

  def priv(): Action[AnyContent] = Action.async { implicit request =>
    withUser(user => Future.successful(Ok(views.html.priv(user.username))))(
      Future.successful(Unauthorized(views.html.defaultpages.unauthorized()))
    )
  }

  private def withUser(block: User => Future[Result])(unauthorizedResult: => Future[Result])
                         (implicit request: Request[AnyContent]): Future[Result] = {
    userFromRequest(request)
      .map {
        case Some(user) => block(user)
        case None => unauthorizedResult
      }.flatten
  }

  private def userFromRequest(request: RequestHeader): Future[Option[User]] ={
    request.session.get("sessionToken")
      .map(token => sessionDAO.getSession(token)
        .map {
          case Some(value) =>
            if (value.expiration.isAfter(LocalDateTime.now(ZoneOffset.UTC))) Some(value)
            else None
          case None => None
          }.map {
            case Some(value) => userDAO.getUser(value.username)
            case None => Future.successful(None)
          }
      ) match {
        case Some(future) => future.flatten
        case None => Future.successful(None)
      }
  }

}
