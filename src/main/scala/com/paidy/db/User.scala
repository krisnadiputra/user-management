package com.paidy.db

import slick.driver.SQLiteDriver.api._

object Tables {
  class Users(tag: Tag) extends Table[(Int, String, String, String, Boolean)](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userName = column[String]("userName")
    def emailAddress = column[String]("emailAddress")
    def password = column[String]("password")
    def blocked = column[Boolean]("blocked")
    def * = (id, userName, emailAddress, password, blocked)
  }

  val users = TableQuery[Users]
}