package models

import scala.concurrent.Future

case class Task(userID: Long, name: String, parent: String)

trait TaskDAO {
  def listByUser(userID: Long): Future[List[Task]]
  def create(userID: Long, name: String): Future[Option[Task]]
  def get(userID: Long, name: String): Future[Option[Task]]
  def delete(userID: Long, name: String): Future[Boolean]
}

