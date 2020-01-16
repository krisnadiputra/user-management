package com.paidy.user

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import slick.jdbc.SQLiteProfile.api._
import org.scalatra.FutureSupport
import java.time.OffsetDateTime

import com.paidy.user.domain._
import com.paidy.{db => paidb}

object UserServlet {
  sealed case class NewUser(userName: String, emailAddress: String, password: String)
  sealed case class UserWithoutPW(id: UserId, userName: UserName, emailAddress: EmailAddress)

  object UserWithoutPW {
    def from(user: User): UserWithoutPW =
      UserWithoutPW(user.id, user.userName, user.emailAddress)
  }
}

class UserServlet(val db: Database) extends ScalatraServlet with JacksonJsonSupport with FutureSupport {
  import UserServlet._

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

  // Sets up automatic case class to JSON output serialization, required by          
  // the JValueResult trait.                                                         
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format             
  before() {                                                                         
    contentType = formats("json")                                                
  }                                                                                  
                                         
  // an instance of UserMap to hold the collection                                                                                     
  get("/") {
    views.html.hello()
  }

  get("/users") {
    for {
      users <- db.run(paidb.Tables.users.result)
    } yield users.map(UserWithoutPW.from)
  }

  get("/users/:id") {
    val id = UserId(Integer.parseInt(params("id")))
    val query = paidb.Tables.users.filter(_.id === id)
    db.run(query.result).map(_.headOption.map(UserWithoutPW.from))
  }

  put("/users/:id") {
    
  }

  delete("/users/:id") {
    
  }

  post("/users/signup") {
    val user = parsedBody.extract[NewUser]
    val insert = DBIO.seq(paidb.Tables.users += User(UserId(0), UserName(user.userName), EmailAddress(user.emailAddress), Some(Password(user.password)), OffsetDateTime.now))
    db.run(insert)
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