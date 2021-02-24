package models
import scala.collection.concurrent.TrieMap
import java.util.concurrent.atomic.AtomicLong
/*
Business logic, will be replaced by a persistent solution.
 */
case class Task(name: String, id: Int)
object TaskCollection {

    private val tasks = TrieMap.empty[String, Task]
    private val seq = new AtomicLong

    def list(): List[Task] = tasks.values.toList
    def create(name: String): Option[Task] = {
      val id = seq.incrementAndGet()
      val item = Task(name, id.toInt)
      tasks.put(name, item)
      Some(item)
    }
    def get(name: String): Option[Task] = tasks.get(name)
    def update(id: Int, name: String, price: Double): Option[Task] = {
      val item = Task(name, id)
      tasks.replace(name, item)
      Some(item)
    }
    def delete(name: String): Boolean = tasks.remove(name).isDefined
}
