package com.paidy.user.domain
 
//JSON related libraries
import org.json4s.{DefaultFormats, Formats}
 
//JSON handling support from Scalatra
import org.scalatra.json._
 
// JSON library for converting the POJO toString as Json
import org.json4s.native.Json
 
class User(val id:Int, var firstName:String, var lastName:String) {
  println("User - Constructor  BEGIN .... ")

  // overloaded or auxillary constructor that will invoke the 
  // increment method for having a new Id value
  def this(firstName:String, lastName:String) {
    this(User.inc, firstName, lastName)
  }

  //toString info with all fields to be printed in JSON format
  override def toString = Json(DefaultFormats).write(this)

  println("User - Constructor END .... ")
}
 
object User {
  private var id = 0

  // increment the id by 1 everytime and return the new value
  private def inc = {
    id += 1;
    id
  }
}