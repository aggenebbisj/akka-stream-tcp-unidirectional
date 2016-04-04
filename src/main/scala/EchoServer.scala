import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.Future

object EchoServer extends App {

//  val binding: Future[ServerBinding] =
//    Tcp().bind("127.0.0.1", 8888).to(Sink.ignore).run()
//
//  binding.map { b =>
//    b.unbind() onComplete {
//      case _ => // ...
//    }
//  }

  implicit val system = ActorSystem("echo-server")
  implicit val materializer = ActorMaterializer()

  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind("127.0.0.1", 8888)

  connections runForeach { incomingConnection =>
    println(s"New connection from: ${incomingConnection.remoteAddress}")

    val echo = Flow[ByteString]
      .via(Framing.delimiter(
        ByteString("\n"),
        maximumFrameLength = 256,
        allowTruncation = true))
      .map(_.utf8String)
      .map(_ + "!!!\n")
      .map(ByteString(_))

    val echo2 = Flow[ByteString]
      .map(_.utf8String)
      .map("Echo " + _)
      .map(ByteString(_))

    incomingConnection.handleWith(echo2)
  }
}
