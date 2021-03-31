package models

import scala.concurrent.Future

case class Task(username: String, name: String, parent: String)

trait TaskDAO {
  def listByUser(userID: String): Future[List[Task]]
  def create(username: String, name: String, parent: String): Future[Option[Task]]
  def get(userID: String, name: String): Future[Option[Task]]
  def delete(userID: String, name: String): Future[Boolean]
}

