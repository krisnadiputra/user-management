package com.paidy.user

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContext$, Future, Promise, Await}
import scala.util.Try
import scala.util.{Failure, Success}

import slick.jdbc.SQLiteProfile.api._
import com.mchange.v2.c3p0.ComboPooledDataSource

import org.json4s.{DefaultFormats, Formats, JValue}
import org.scalatra._
import org.scalatra.json._
import org.scalatra.FutureSupport

import java.time.OffsetDateTime

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
  ) {
    def exist(): Boolean = {
      !(this.emailAddress.isEmpty && this.password.isEmpty)
    }
  }
  sealed case class ResMessage(
    message: String
  ) {
    def toMap(): Map[String, String] = {
      Map("message" -> this.message)
    }
  }
  sealed case class UserWithoutPW(
    id: UserId,
    userName: UserName,
    emailAddress: EmailAddress,
    createdAt: String,
    updatedAt: String,
    blockedAt: Option[String],
    version: Int
  )
  object UserWithoutPW {
    def from(user: User): UserWithoutPW = {
      UserWithoutPW(
        user.id,
        user.userName,
        user.emailAddress,
        user.createdAt.toString(),
        user.updatedAt.toString(),
        user.blockedAt.map(_.toString()),
        user.version
      )
    }
  }
}

class UserServlet(
    val db: Database
  ) extends ScalatraServlet with JacksonJsonSupport with FutureSupport {
  import UserServlet._

  protected implicit def executor = scala.concurrent.ExecutionContext.Implicits.global

  def withId(
    params: Params
  )(
    body: UserId => Future[ActionResult]
  ): Future[ActionResult] = {
    try {
      val id = UserId(Integer.parseInt(params("id")))
      body(id)
    } catch {
      case e: NumberFormatException => {
        Future(BadRequest(ResMessage("Invalid id!").toMap))
      }
    }
  }

  def withSignupData(
    parsedBody: JValue
  )(
    body: SignupData => Future[ActionResult]
  ): Future[ActionResult] = {
    try {
      val signupData = parsedBody.extract[SignupData]
      body(signupData)
    } catch {
      case e : Throwable => Future(BadRequest(ResMessage(e.toString).toMap))
    }
  }

  // Sets up automatic case class to JSON output serialization
  // required by the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  protected val prefix: String = "/api"

  // Before every action runs, set the content type to be in JSON format             
  before() {                                                                         
    contentType = formats("json")                                                
  }

  notFound {
    NotFound(ResMessage("This address does not exist.").toMap)
  }                                                                        
                                                                                  
  get("/") {
    views.html.hello()
  }

  get(prefix + "/users") {
    for {
      users <- db.run(paidb.Tables.users.result)
    } yield users.map(UserWithoutPW.from)
  }

  get(prefix + "/users/:id") {
    withId(params) { id =>
      val query = paidb.Tables.users.filter(_.id === id)
      for {
        users <- db.run(query.result)
      } yield {
        if (users.size > 0) {
          Ok(users.headOption.map(UserWithoutPW.from))
        } else {
          NotFound(ResMessage("User not found!").toMap)
        }
      }
    }
  }

  put(prefix + "/users/:id") {
    withId(params) { id =>
      val newData = parsedBody.extract[UpdateData]

      if (newData.exist()) {
        val findUser = paidb.Tables.users.filter(_.id === id)

        val updated = for {
          user <- db.run(findUser.result).map(_.headOption)
        } yield {
          user.map(user => {
            val emailUpdated = newData.emailAddress.map(
              it => user.updateEmailAddress(
                EmailAddress(it),
                OffsetDateTime.now
              )
            ) getOrElse user
            val pwUpdated = newData.password.map(
              it => emailUpdated.updatePassword(
                Some(Password(it)),
                OffsetDateTime.now
              )
            ) getOrElse emailUpdated
            pwUpdated
          })
        }

        updated.flatMap(user =>
          user.map(user => {
            val query = paidb.Tables.users.filter(_.id === id).update(user)
            db.run(query).map { x => Ok(UserWithoutPW.from(user)) } recover {
              case cause => BadRequest(ResMessage(cause.toString).toMap)
            }
          }) getOrElse {
            Future(NotFound(ResMessage("User not found!").toMap))
          }
        )
      } else {
        Future(BadRequest(ResMessage("There is no data to update!").toMap))
      }
    }

    // val queries: ArrayBuffer[slick.jdbc.SQLiteProfile.ProfileAction[Int,slick.dbio.NoStream,slick.dbio.Effect.Write]] = ArrayBuffer()

    // param.emailAddress match {
    //   case Some(emailAddress) =>
    //     queries += findUser.map(_.emailAddress).update(EmailAddress(emailAddress))
    //   case _ => ()
    // }

    // param.password match {
    //   case Some(password) =>
    //     queries += findUser.map(_.password).update(Some(Password(password)))
    //   case _ => ()
    // }

    // for {
    //   updates <- db.run(DBIO.seq(queries: _*).transactionally)
    // } yield {
    // }
  }

  delete(prefix + "/users/:id") {
    withId(params) { id => 
      val query = paidb.Tables.users.filter(_.id === id)
      val action = query.delete
      db.run(action).map(affectedRows => {
        if (affectedRows > 0) NoContent() else NotFound(ResMessage("User not found!").toMap)
      })
    }
  }

  post(prefix + "/users/signup") {
    withSignupData(parsedBody) { signupData =>
      val now = OffsetDateTime.now
      val addUser = DBIO.seq(
        paidb.Tables.users += User(
          UserId(0),
          UserName(signupData.userName),
          EmailAddress(signupData.emailAddress),
          Some(Password(signupData.password)),
          now,
          now,
          None,
          1
        )
      )
      val findUser = paidb.Tables.users.filter(
        _.emailAddress === EmailAddress(signupData.emailAddress)
      ).result

      {
        for {
          _ <- db.run(addUser)
          user <- db.run(findUser).map(_.headOption.map(UserWithoutPW.from))
        } yield Created(user)
      } recover {
        case cause => {
          // unique userName or emailAddress
          BadRequest(ResMessage(cause.toString).toMap)
        }
      }
    }
  }

  post(prefix + "/users/:id/block") {
    withId(params) { id =>
      val findUser = paidb.Tables.users.filter(_.id === id)

      val updated = for {
        user <- db.run(findUser.result).map(_.headOption)
      } yield {
        user.map(_.block(OffsetDateTime.now))
      }

      updated.flatMap(user =>
        user.map(user => {
          val query = paidb.Tables.users.filter(_.id === id).update(user)
          db.run(query).map { _ => Ok(UserWithoutPW.from(user)) }
        }) getOrElse {
          Future(NotFound(ResMessage("User not found!").toMap))
        }
      )
    }
  }

  post(prefix + "/users/:id/unblock") {
    withId(params) { id =>
      val findUser = paidb.Tables.users.filter(_.id === id)

      val updated = for {
        user <- db.run(findUser.result).map(_.headOption)
      } yield {
        user.map(_.unblock(OffsetDateTime.now))
      }

      updated.flatMap(user =>
        user.map(user => {
          val query = paidb.Tables.users.filter(_.id === id).update(user)
          db.run(query).map { _ => Ok(UserWithoutPW.from(user)) }
        }) getOrElse {
          Future(NotFound(ResMessage("User not found!").toMap))
        }
      )
    }
  }

  post(prefix + "/users/:id/reset-password") {
    withId(params) { id =>
      val findUser = paidb.Tables.users.filter(_.id === id)

      val updated = for {
        user <- db.run(findUser.result).map(_.headOption)
      } yield {
        user.map(_.resetPassword(OffsetDateTime.now))
      }

      updated.flatMap(user =>
        user.map(user => {
          val query = paidb.Tables.users.filter(_.id === id).update(user)
          db.run(query).map { _ => Ok(UserWithoutPW.from(user)) }
        }) getOrElse {
          Future(NotFound(ResMessage("User not found!").toMap))
        }
      )
    }
  }

}