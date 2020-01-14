package com.paidy.user
 
import scala.collection._

import java.time.OffsetDateTime

import com.paidy.user.domain._
 
class UserMap {
 
  println("UserMap - Constructor BEGIN... ")

  var map:Map[User.Id, User] = Map()

  //method to intialize the map with an initial entry to start with
  init()

  // method to add one User instance to the Map
  def init() {
    this.add(UserName("krisnadiputra"), EmailAddress("krisnadiputra@yahoo.com.sg"), Some(Password("12345")))
  }

  def add(userName: UserName, emailAddress: EmailAddress, password: Option[Password]):User.Id = {
    var u:User = User(User.Id("1"), userName, emailAddress, password, OffsetDateTime.now())
    map += (u.id -> u)
    u.id
  }

  def add(u:User):User = {
    map += (u.id -> u)
    u
  }

  def get(id:String):User = {
    map(User.Id(id))
  }

  def remove(id:String) = {
    map = map - User.Id(id)
  }

  def update(id:String, c:User) {
    map += (User.Id(id) -> c)
  }

  override def toString = s"Map :: ${map}"

  println("UserMap - Constructor END....")
}