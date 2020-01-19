package com.paidy.user.domain

import java.time.OffsetDateTime
import cats.kernel.Eq
import cats.implicits._
import com.softwaremill.quicklens._
import slick.driver.SQLiteDriver.api._

final case class User(
  id: UserId,
  userName: UserName,
  emailAddress: EmailAddress,
  password: Option[Password],
  createdAt: OffsetDateTime,
  updatedAt: OffsetDateTime,
  blockedAt: Option[OffsetDateTime],
  version: Int
) {

  def updateEmailAddress(emailAddress: EmailAddress, at: OffsetDateTime): User =
    this
      .modify(_.emailAddress)
      .setTo(emailAddress)
      .modify(_.updatedAt)
      .setTo(at)
      .modify(_.version)
      .using(_ + 1)

  def updatePassword(password: Option[Password], at: OffsetDateTime): User =
    this
      .modify(_.password)
      .setTo(password)
      .modify(_.updatedAt)
      .setTo(at)
      .modify(_.version)
      .using(_ + 1)

  def resetPassword(at: OffsetDateTime): User =
    this
      .modify(_.password)
      .setTo(None)
      .modify(_.updatedAt)
      .setTo(at)
      .modify(_.version)
      .using(_ + 1)

  def block(at: OffsetDateTime): User =
    this
      .modify(_.blockedAt)
      .setTo(Some(at))
      .modify(_.updatedAt)
      .setTo(at)
      .modify(_.version)
      .using(_ + 1)

  def unblock(at: OffsetDateTime): User =
    this
      .modify(_.blockedAt)
      .setTo(None)
      .modify(_.updatedAt)
      .setTo(at)
      .modify(_.version)
      .using(_ + 1)

}
