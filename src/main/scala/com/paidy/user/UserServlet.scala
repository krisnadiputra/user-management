package com.paidy.user

import org.scalatra._

class UserServlet extends ScalatraServlet {

  post("/users/:id/reset-password") {

  }

  post("/users/:id/unblock") {

  }

  post("/users/:id/block") {

  }

  get("/users/:id") {
    
  }

  put("/users/:id") {
    
  }

  delete("/users/:id") {
    
  }

  get("/users/generate") {
    
  }

  post("/users/signup") {

  }

  get("/users") {

  }

  get("/") {
    views.html.hello()
  }

}
