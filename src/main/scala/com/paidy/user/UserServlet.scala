package com.paidy.user

import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._
import com.mongodb.casbah.Imports._

import com.paidy.user.domain._

class UserServlet(mongoColl: MongoCollection) extends ScalatraServlet with JacksonJsonSupport {

  // Sets up automatic case class to JSON output serialization, required by          
  // the JValueResult trait.                                                         
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format             
  before() {                                                                         
    contentType = formats("json")                                                
  }                                                                                  
                                         
  // an instance of UserMap to hold the collection                                                                                     
  var userMap = new UserMap()

  get("/") {
    views.html.hello()
  }

  get("/users") {
    userMap.map
  }

  get("/users/:id") {
    println("GET - Customer Id - arg. passed :: " + params("id"))
    userMap.get(params("id"))
  }

  put("/users/:id") {
    
  }

  delete("/users/:id") {
    
  }

  post("/users/signup") {
    val user = parsedBody.extract[User]
    user
  }

  get("/users/generate") {
    
  }

  post("/users/:id/block") {

  }

  post("/users/:id/unblock") {

  }

  post("/users/:id/reset-password") {

  }

}
