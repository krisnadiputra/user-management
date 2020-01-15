package com.paidy.user

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import slick.jdbc.SQLiteProfile.api._
import com.paidy.user.domain._
import com.paidy.{db => paidb}

object UserServlet {
  sealed case class NewUser(userName: String, emailAddress: String, password: String) {
  }
}

class UserServlet(val db: Database) extends ScalatraServlet with JacksonJsonSupport {
  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

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
    db.run(paidb.Tables.users.result)
  }

  get("/users/:id") {
    val id = Integer.parseInt(params("id"))
    println("GET - Customer Id - arg. passed :: " + params("id"))
    // userMap.get(params("id"))
    val query = paidb.Tables.users.filter(_.id === id)
    db.run(query.result)
  }

  put("/users/:id") {
    
  }

  delete("/users/:id") {
    
  }

  post("/users/signup") {
    println(parsedBody)
    val user = parsedBody.extract[UserServlet.NewUser]
    println(user)
    val insert = DBIO.seq(paidb.Tables.users += (0, user.userName, user.emailAddress, user.password, false))
    db.run(insert)
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