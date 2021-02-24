package models

import play.api.data.Form
import play.api.data.Forms._

/*
  Supplying form data and constraints.
*/
case class TaskForm(name: String)
object BasicForm {
  val form: Form[TaskForm] = Form(
    mapping(
      "name" -> nonEmptyText
    )(TaskForm.apply)(TaskForm.unapply)
  )
}
