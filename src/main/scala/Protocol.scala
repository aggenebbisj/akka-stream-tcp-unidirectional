import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

object Protocol extends App {

  implicit val system = ActorSystem("unidirectional-tcp", ConfigFactory.defaultReference())
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val binding: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind("localhost", 8888)

  binding.runForeach { (incomingConnection: IncomingConnection) =>
    println(s"New connection from: ${incomingConnection.remoteAddress}")

    val (closePromise, doneFuture) =
      Source.maybe[ByteString]
        .via(incomingConnection.flow)
        .via(incoming)
        .toMat(Sink.foreach(println))(Keep.both).run()

    // How is this promise completed???
    closePromise.completeWith(doneFuture.map { _ =>
      Some(ByteString.empty)
    })
  }

  // Simple flow mapping ByteStrings to Strings
  val incoming: Flow[ByteString, String, _] =
    Flow[ByteString].map(_.utf8String)

}
