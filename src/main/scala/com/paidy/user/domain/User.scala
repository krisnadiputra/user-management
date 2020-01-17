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

// object User {
//   def apply(
//     id: User.Id,
//     userName: UserName,
//     emailAddress: EmailAddress,
//     password: Option[Password],
//     at: OffsetDateTime
//   ): User = User(id, userName, emailAddress, password, Metadata(1, at, at, None, None))
// 
//   def tupled(t: (User.Id, UserName, EmailAddress, Option[Password], OffsetDateTime)): User = {
//     null // FIXME
//   }
// 
//   final case class Id(value: String) extends AnyVal with MappedTo[String]
// 
//   final case class Metadata(
//     version: Int,
//     createdAt: OffsetDateTime,
//     updatedAt: OffsetDateTime,
//     blockedAt: Option[OffsetDateTime],
//     deletedAt: Option[OffsetDateTime]
//   )
// 
//   sealed trait Status
//   object Status {
//     final case object Active extends Status
//     final case object Blocked extends Status
//     final case object Deleted extends Status
// 
//     implicit val eq: Eq[Status] =
//       Eq.fromUniversalEquals
//   }
// 
//   final def status(user: User): Status =
//     if (user.metadata.deletedAt.isDefined) Status.Deleted
//     else if (user.metadata.blockedAt.isDefined) Status.Blocked
//     else Status.Active
// 
//   def resetPassword(user: User, at: OffsetDateTime): User =
//     user
//       .modify(_.password)
//       .setTo(None)
//       .modify(_.metadata.updatedAt)
//       .setTo(at)
//       .modify(_.metadata.version)
//       .using(_ + 1)
// 
//   def delete(user: User, at: OffsetDateTime): User =
//     user
//       .modify(_.metadata.deletedAt)
//       .setTo(Some(at))
//       .modify(_.metadata.updatedAt)
//       .setTo(at)
//       .modify(_.metadata.version)
//       .using(_ + 1)
// }