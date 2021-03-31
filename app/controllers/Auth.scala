package controllers

import java.time.{LocalDateTime, ZoneOffset}

import models.{SessionDAO, User, UserDAO}
import play.api.mvc.{AnyContent, Request, RequestHeader, Result}

import scala.concurrent.{ExecutionContext, Future}

object Auth {
  def withUser(block: User => Future[Result])(unauthorizedResult: => Future[Result])
                      (implicit request: Request[AnyContent],
                       sessionDAO : SessionDAO, userDAO : UserDAO,
                       executionContext : ExecutionContext): Future[Result] = {
    userFromRequest(request)
      .map {
        case Some(user) => block(user)
        case None => unauthorizedResult
      }.flatten
  }
  /*
  def withUserOrLogout(block: User => Future[Result])
                      (implicit request: Request[AnyContent],
                       sessionDAO : SessionDAO, userDAO : UserDAO,
                       executionContext : ExecutionContext)= {
    withUser(block){
      Future.successful(Redirect(routes.LoginController.login()).withSession("sessionToken" -> null))
    }
  }*/


  private def userFromRequest(request: RequestHeader)
                             (implicit sessionDAO : SessionDAO, userDAO : UserDAO,
                              executionContext: ExecutionContext): Future[Option[User]] ={
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
