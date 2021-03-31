package business.memory

import javax.inject.Singleton
import models.{Task, TaskDAO}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

@Singleton
class TaskInMemoryDAO extends TaskDAO{

  private val tasks = TrieMap.empty[String, Task]

  override def listByUser(username: String): Future[List[Task]] = {
    Future.successful(tasks.filter(_._2.username == username).values.toList)
  }
  override def create(username: String, name: String, parent: String): Future[Option[Task]] = {
    val item = Task(username, name, parent)
    tasks.put(name, item)
    Future.successful(Some(item))
  }
  override def get(username: String, name: String): Future[Option[Task]] = Future.successful(tasks.get(name))
  override def delete(username: String, name: String): Future[Boolean] = Future.successful(tasks.remove(name).isDefined)
}
