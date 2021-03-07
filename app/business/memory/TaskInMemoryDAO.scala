package business.memory

import java.util.concurrent.atomic.AtomicLong
import models.{Task, TaskDAO}
import scala.collection.concurrent.TrieMap

class TaskInMemoryDAO extends TaskDAO{

  private val tasks = TrieMap.empty[String, Task]
  private val seq = new AtomicLong

  def list(): List[Task] = tasks.values.toList
  def listByUser(userID: Long): List[Task] = {
    tasks.filter(_._2.userID == userID).values.toList
  }
  def create(userID: Long, name: String): Option[Task] = {
    val item = Task(userID, name)
    tasks.put(name, item)
    Some(item)
  }
  def get(userID: Long, name: String): Option[Task] = tasks.get(name)
  def update(userID: Long, name: String): Option[Task] = {
    val item = Task(userID, name)
    tasks.replace(name, item)
    Some(item)
  }
  def delete(userID: Long, name: String): Boolean = tasks.remove(name).isDefined
}
