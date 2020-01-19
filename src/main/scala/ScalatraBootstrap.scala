import com.mchange.v2.c3p0.ComboPooledDataSource
import com.paidy.user._
import org.scalatra._
import javax.servlet.ServletContext

import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.SQLiteDriver.api._
import com.paidy.{db => paidb}

class ScalatraBootstrap extends LifeCycle {
  val cpds = new ComboPooledDataSource

  private def closeDbConnection() {
    cpds.close
  }

  override def init(context: ServletContext) {
    val db = Database.forURL("jdbc:sqlite:user-management.db", driver="org.sqlite.JDBC")
    db.run(paidb.Tables.users.schema.create)
    context.mount(new UserServlet(db), "/api/*")   // create and mount the Scalatra application
  }

  override def destroy(context: ServletContext) {
    super.destroy(context)
    closeDbConnection
  }
}
