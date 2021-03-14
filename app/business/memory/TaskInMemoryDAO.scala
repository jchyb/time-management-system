package business.memory

import javax.inject.Singleton
import models.{Task, TaskDAO}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

@Singleton
class TaskInMemoryDAO extends TaskDAO{

  private val tasks = TrieMap.empty[String, Task]

  def listByUser(username: String): Future[List[Task]] = {
    Future.successful(tasks.filter(_._2.username == username).values.toList)
  }
  def create(username: String, name: String): Future[Option[Task]] = {
    val item = Task(username, name, "Any")
    tasks.put(name, item)
    Future.successful(Some(item))
  }
  def get(username: String, name: String): Future[Option[Task]] = Future.successful(tasks.get(name))
  def delete(username: String, name: String): Future[Boolean] = Future.successful(tasks.remove(name).isDefined)
}
