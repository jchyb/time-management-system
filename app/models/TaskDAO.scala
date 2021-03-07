package models
import scala.collection.concurrent.TrieMap
import java.util.concurrent.atomic.AtomicLong
/*
Business logic, will be replaced by a persistent solution.
 */
case class Task(name: String, userID: Int)
object TaskCollection {

    private val tasks = TrieMap.empty[String, Task]
    private val seq = new AtomicLong

    def list(): List[Task] = tasks.values.toList
    def create(name: String): Option[Task] = {
      val id = seq.incrementAndGet()
      val item = Task(name, 0)
      tasks.put(name, item)
      Some(item)
    }
    def get(name: String): Option[Task] = tasks.get(name)
    def update(userID: Int, name: String): Option[Task] = {
      val item = Task(name, 0)
      tasks.replace(name, item)
      Some(item)
    }
    def delete(name: String): Boolean = tasks.remove(name).isDefined
}
