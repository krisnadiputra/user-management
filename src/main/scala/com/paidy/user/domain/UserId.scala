package com.paidy.user.domain

import cats.kernel.Eq
import slick.driver.SQLiteDriver.api._

final case class UserId(value: Int) extends AnyVal with MappedTo[Int]