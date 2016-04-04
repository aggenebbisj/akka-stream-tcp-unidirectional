import java.io.File

import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.stream.{ActorMaterializer, ClosedShape, IOResult}
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future

object Join extends App {

  implicit val system = ActorSystem("Demo")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)

  val f1: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 2)
  val f2: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 5)

  val joined: RunnableGraph[NotUsed] = f1.join(f2)

//  joined.
    //.runForeach(i => println(i))


}
