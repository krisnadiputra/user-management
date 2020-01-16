package com.paidy.user.domain

import cats.kernel.Eq
import slick.driver.SQLiteDriver.api._

final case class UserName(value: String) extends AnyVal with MappedTo[String]

object UserName {
  implicit val eq: Eq[UserName] =
    Eq.fromUniversalEquals
}