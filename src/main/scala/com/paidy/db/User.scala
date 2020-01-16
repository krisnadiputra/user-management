package com.paidy.db

import java.time.OffsetDateTime
import slick.driver.SQLiteDriver.api._

import com.paidy.user.domain._

object Tables {
  implicit val JavaLocalDateTimeMapper = MappedColumnType.base[OffsetDateTime, String](
    t => t.toString(),
    l => OffsetDateTime.parse(l)
  )

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[UserId]("id", O.PrimaryKey, O.AutoInc)
    def userName = column[UserName]("userName")
    def emailAddress = column[EmailAddress]("emailAddress")
    def password = column[Option[Password]]("password")
    def createdAt = column[OffsetDateTime]("createdAt")
    def * = (id, userName, emailAddress, password, createdAt) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]
}