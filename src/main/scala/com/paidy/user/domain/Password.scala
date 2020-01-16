package com.paidy.user.domain

import slick.driver.SQLiteDriver.api._

final case class Password(value: String) extends AnyVal with MappedTo[String]