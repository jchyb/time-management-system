package models

import business.memory.TaskInMemoryDAO
import com.google.inject.ImplementedBy

case class Task(userID: Long, name: String)

@ImplementedBy(classOf[TaskInMemoryDAO])
trait TaskDAO {
  def list(): List[Task]
  def listByUser(userID: Long): List[Task]
  def create(userID: Long, name: String): Option[Task]
  def get(userID: Long, name: String): Option[Task]
  def update(userID: Long, name: String): Option[Task]
  def delete(userID: Long, name: String): Boolean
}

