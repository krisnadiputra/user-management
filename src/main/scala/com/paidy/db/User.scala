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
    def updatedAt = column[OffsetDateTime]("updatedAt")
    def blockedAt = column[Option[OffsetDateTime]]("blockedAt")
    def version = column[Int]("version")
    def * = (
      id,
      userName,
      emailAddress,
      password,
      createdAt,
      updatedAt,
      blockedAt,
      version
    ) <> (User.tupled, User.unapply)

    def userNameIndex = index("userName", userName, unique = true)
    def emailAddressIndex = index("emailAddress", emailAddress, unique = true)
  }

  val users = TableQuery[Users]
}