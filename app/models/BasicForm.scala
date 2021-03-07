package models

import play.api.data.Form
import play.api.data.Forms._

/*
  Supplying form data types and constraints.
*/
case class TaskForm(name: String)
case class LoginForm(username: String, password: String)
case class RegisterForm(username: String, password: String)
object BasicForm {
  val task: Form[TaskForm] = Form(
    mapping(
      "name" -> nonEmptyText
    )(TaskForm.apply)(TaskForm.unapply)
  )
  val login: Form[LoginForm] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )
  val register: Form[RegisterForm] = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(RegisterForm.apply)(RegisterForm.unapply)
  )
}
