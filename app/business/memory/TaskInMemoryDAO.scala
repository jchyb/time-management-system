package business.memory

import scala.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import models.{Task, TaskDAO}
import scala.collection.concurrent.TrieMap

class TaskInMemoryDAO extends TaskDAO{

  private val tasks = TrieMap.empty[String, Task]
  private val seq = new AtomicLong

  def listByUser(userID: Long): Future[List[Task]] = {
    Future.successful(tasks.filter(_._2.userID == userID).values.toList)
  }
  def create(userID: Long, name: String): Future[Option[Task]] = {
    val item = Task(userID, name)
    tasks.put(name, item)
    Future.successful(Some(item))
  }
  def get(userID: Long, name: String): Future[Option[Task]] = Future.successful(tasks.get(name))
  def update(userID: Long, name: String): Future[Option[Task]] = {
    val item = Task(userID, name)
    tasks.replace(name, item)
    Future.successful(Some(item))
  }
  def delete(userID: Long, name: String): Future[Boolean] = Future.successful(tasks.remove(name).isDefined)
}
