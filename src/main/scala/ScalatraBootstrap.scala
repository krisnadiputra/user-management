import com.mongodb.casbah.Imports._
import com.paidy.user._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

  // We're connecting with default settings - localhost on port 27017 -
  // by calling MongoClient() with no arguments.
  val mongoClient =  MongoClient()
  val mongoColl = mongoClient("userManagement")("users")

  // pass a reference to the Mongo collection into your servlet when you mount it at application start:
  context.mount(new UserServlet(mongoColl), "/*")

  }
}
