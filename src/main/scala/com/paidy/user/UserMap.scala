package com.paidy.user
 
import scala.collection._

import com.paidy.user.domain._
 
class UserMap {
 
  println("UserMap - Constructor BEGIN... ")

  var map:Map[Int, User] = Map()

  //method to intialize the map with an initial entry to start with
  init()

  // method to add one User instance to the Map
  def init() {
    this.add("Raghavan", "Muthu")
  }

  def add(firstName:String, lastName:String):Int = {
    var u:User = new User(firstName, lastName)
    map += (u.id -> u)
    u.id
  }

  def add(u:User):User = {
    map += (u.id -> u)
    u
  }

  def get(id:Int):User = {
    map(id)
  }

  def remove(id:Int) = {
    map = map - id
  }

  def update(id:Int, c:User) {
    map += (id -> c)
  }

  override def toString = s"Map :: ${map}"

  println("UserMap - Constructor END....")
}