import scala.concurrent._
import scala.concurrent.duration._
import ExecutionContext.Implicits.global
import scalaj.http.{HttpException, Http, HttpOptions}

object HttpTest {

  def timing[A](x: => A): Int = {
    import com.github.nscala_time.time.Imports._
    import org.joda.time.PeriodType

    val then = LocalDateTime.now
    x
    new Period(then, LocalDateTime.now, PeriodType.millis).getValue(0)
  }




  def loopAndTime(http: Http.Request, loop: Int): String = {
    try {
      val times = Seq.fill(loop)(timing(http.asString))
      val total = times.foldLeft(0)(_ + _)
      s"${http.getUrl}: Average: ${total / loop} Total: $total"
    } catch {
      case e: HttpException =>
        println(s"Got error on ${http.getUrl}: ${e.body}")
        throw e
    }
  }

  def main(args: Array[String]) {
    val loop = 5
    val conn = HttpOptions.connTimeout(1000)
    val read = HttpOptions.readTimeout(5000)
    val f = for {
      google <- Future({loopAndTime(Http("http://google.com/#q=scala").option(read), loop)})
      bing <- Future({loopAndTime(Http("http://www.bing.com/?q=scala").option(read), loop)})
      duck <- Future({loopAndTime(Http("https://duckduckgo.com/?q=scala").option(read), loop)})
    } yield Seq(google, bing, duck).mkString("\n")
    val result = Await.result(f, 40 second)
    println(result)
  }
}

