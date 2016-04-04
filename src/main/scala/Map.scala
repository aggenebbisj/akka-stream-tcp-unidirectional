import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Source, Flow}
import akka.util.ByteString

object Map extends App {

  implicit val system = ActorSystem("Demo")
  implicit val materializer = ActorMaterializer()

  val f1: Flow[Int, String, _] = Flow[Int].map(_.toString)
//  val f2: Flow[Int, Int, _] = f1.map(_.toInt)

  Source(1 to 100).via(f1).runForeach(x => println(x))
}
