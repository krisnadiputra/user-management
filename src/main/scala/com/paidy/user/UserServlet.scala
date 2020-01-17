package com.paidy.user

import scala.util.{Failure, Success}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContext$, Future, Promise, Await}

import slick.jdbc.SQLiteProfile.api._
import com.mchange.v2.c3p0.ComboPooledDataSource

import org.json4s.{DefaultFormats, Formats}
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
  )
  sealed case class UserWithoutPW(
    id: UserId,
    userName: UserName,
    emailAddress: EmailAddress,
    createdAt: String,
    updatedAt: String,
    blockedAt: Option[OffsetDateTime],
    version: Int
  )
  object UserWithoutPW {
    def from(user: User): UserWithoutPW = {
      // best practice?
      UserWithoutPW(
        user.id,
        user.userName,
        user.emailAddress,
        user.createdAt.toString(),
        user.updatedAt.toString(),
        user.blockedAt,
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

  // Sets up automatic case class to JSON output serialization
  // required by the JValueResult trait.
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  // Before every action runs, set the content type to be in JSON format             
  before() {                                                                         
    contentType = formats("json")                                                
  }

  notFound {
    NotFound("Not Found!")
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
    // try catch here wrong id / NotFound
    val id = UserId(Integer.parseInt(params("id")))
    val query = paidb.Tables.users.filter(_.id === id)
    db.run(query.result).map(_.headOption.map(UserWithoutPW.from))
  }

  put("/users/:id") {
    val id = UserId(Integer.parseInt(params("id")))
    val param = parsedBody.extract[UpdateData]

    val findUser = paidb.Tables.users.filter(_.id === id)

    val updated = for {
      user <- db.run(findUser.result).map(_.headOption)
    } yield {
      user.map(user => {
        val emailUpdated = param.emailAddress.map(
          it => user.updateEmailAddress(
            EmailAddress(it),
            OffsetDateTime.now
          )
        ) getOrElse user
        val pwUpdated = param.password.map(
          it => emailUpdated.updatePassword(
            Some(Password(it)),
            OffsetDateTime.now
          )
        ) getOrElse emailUpdated
        pwUpdated
      })
    }

    updated.map(user =>
      user.map(user => {
        val query = paidb.Tables.users.filter(_.id === id).update(user)
        db.run(query).map { _ => Ok(UserWithoutPW.from(user)) }
      }) getOrElse {
        Future(NotFound("User not found"))
      }
    )

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

  delete("/users/:id") {
    // try catch NotFound
    val id = UserId(Integer.parseInt(params("id")))
    val query = paidb.Tables.users.filter(_.id === id)
    val action = query.delete
    db.run(action).map(_ => Ok())
  }

  post("/users/signup") {
    // try catch here / BadRequest
    val signupData = parsedBody.extract[SignupData]
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
      } yield Ok(user)
    } recover {
      case cause => BadRequest(Map("error" -> cause.toString))
    }
  }

  post("/users/:id/block") {
    val id = UserId(Integer.parseInt(params("id")))
    val findUser = paidb.Tables.users.filter(_.id === id)

    val updated = for {
      user <- db.run(findUser.result).map(_.headOption)
    } yield {
      user.map(_.block(OffsetDateTime.now))
    }

    updated.map(user =>
      user.map(user => {
        val query = paidb.Tables.users.filter(_.id === id).update(user)
        db.run(query).map { _ => Ok(UserWithoutPW.from(user)) }
      }) getOrElse {
        Future(NotFound("User not found"))
      }
    )
  }

  post("/users/:id/unblock") {
    val id = UserId(Integer.parseInt(params("id")))
    val findUser = paidb.Tables.users.filter(_.id === id)

    val updated = for {
      user <- db.run(findUser.result).map(_.headOption)
    } yield {
      user.map(_.unblock(OffsetDateTime.now))
    }

    updated.map(user =>
      user.map(user => {
        val query = paidb.Tables.users.filter(_.id === id).update(user)
        db.run(query).map { _ => Ok(UserWithoutPW.from(user)) }
      }) getOrElse {
        Future(NotFound("User not found"))
      }
    )
  }

  post("/users/:id/reset-password") {
    // try catch NotFound
    val id = UserId(Integer.parseInt(params("id")))
    val query = for {
      user <- paidb.Tables.users if user.id === id
    } yield user.password
    val update = query.update(None)
    db.run(update).map(_ => Ok(Map("success" -> true)))
  }

}