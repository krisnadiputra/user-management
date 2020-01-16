package com.paidy.user

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json._
import slick.jdbc.SQLiteProfile.api._
import org.scalatra.FutureSupport
import java.time.OffsetDateTime
import scala.concurrent.Future

import com.paidy.user.domain._
import com.paidy.{db => paidb}

object UserServlet {
  sealed case class SignupData(
    userName: String,
    emailAddress: String,
    password: String
  )
  sealed case class UpdateData(
    emailAddress: Option[String],
    password: Option[String]
  )
  sealed case class UserWithoutPW(
    id: UserId,
    userName: UserName,
    emailAddress: EmailAddress
  )
  object UserWithoutPW {
    def from(user: User): UserWithoutPW =
      UserWithoutPW(user.id, user.userName, user.emailAddress)
  }
}

class UserServlet(
    val db: Database
  ) extends ScalatraServlet with JacksonJsonSupport with FutureSupport {
  import UserServlet._

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

  // Sets up automatic case class to JSON output serialization, required by          
  // the JValueResult trait.                                                         
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format             
  before() {                                                                         
    contentType = formats("json")                                                
  }

  notFound {
    <h1>Not found.</h1>
  }                                                                        
                                                                                  
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
    val id = UserId(Integer.parseInt(params("id")))
    val updateData = parsedBody.extract[UpdateData]
    if (updateData.emailAddress.isDefined) {
      val data = for {
        user <- paidb.Tables.users if user.id === id
      } yield user.emailAddress
      val update = data.update(EmailAddress(updateData.emailAddress.get))
      db.run(update)
    }
    if (updateData.password.isDefined) {
      val data = for {
        user <- paidb.Tables.users if user.id === id
      } yield user.password
      val update = data.update(Some(Password(updateData.password.get)))
      db.run(update)
    }
  }

  delete("/users/:id") {
    val id = UserId(Integer.parseInt(params("id")))
    val query = paidb.Tables.users.filter(_.id === id)
    val action = query.delete
    val affectedRowsCount: Future[Int] = db.run(action)
  }

  post("/users/signup") {
    val signupData = parsedBody.extract[SignupData]
    val insert = DBIO.seq(
      paidb.Tables.users += User(
        UserId(0),
        UserName(signupData.userName),
        EmailAddress(signupData.emailAddress),
        Some(Password(signupData.password)),
        OffsetDateTime.now
      )
    )
    db.run(insert)
  }

  post("/users/:id/block") {

  }

  post("/users/:id/unblock") {

  }

  post("/users/:id/reset-password") {
    val id = UserId(Integer.parseInt(params("id")))
    val data = for {
      user <- paidb.Tables.users if user.id === id
    } yield user.password
    val update = data.update(None)
    db.run(update)
  }

}