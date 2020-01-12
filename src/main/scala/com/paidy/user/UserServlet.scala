package com.paidy.user

import org.scalatra._

class UserServlet extends ScalatraServlet {

  get("/") {
    views.html.hello()
  }

}
