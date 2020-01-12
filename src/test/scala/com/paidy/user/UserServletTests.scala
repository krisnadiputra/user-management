package com.paidy.user

import org.scalatra.test.scalatest._

class UserServletTests extends ScalatraFunSuite {

  addServlet(classOf[UserServlet], "/*")

  test("GET / on UserServlet should return status 200") {
    get("/") {
      status should equal (200)
    }
  }

}
