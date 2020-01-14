package com.paidy.db

import slick.driver.SQLiteDriver.api._

object Tables {
  class Users(tag: Tag) extends Table[(Int, String)](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey)
    def name = column[String]("name")
    def * = (id, name)
  }

  val users = TableQuery[Users]
}