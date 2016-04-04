import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl._
import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Promise, Future}

/**
  * Basis for a unidirectional TCP protocol
  */
object Protocol extends App {

  implicit val system = ActorSystem("unidirectional-tcp", ConfigFactory.defaultReference())
  implicit val materializer = ActorMaterializer()

  val binding: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind("localhost", 8888)

  binding.runForeach { (incomingConnection: IncomingConnection) =>
    println(s"New connection from: ${incomingConnection.remoteAddress}")

    // Sink can be swapped by f.i. an ActorSubscriber
    val targetSink = Sink.foreach[String](e => println("Sink received: " + e))

    val completionSource: Source[ByteString, Promise[Option[ByteString]]] =
      Source.maybe[ByteString].drop(1) // Discard the element immediately, only used for closing connection

    val (closePromise, doneFuture) =
      completionSource
        .via(incomingConnection.flow)
        .via(protocol)
        .toMat(targetSink)(Keep.both) // We keep both, because we use the future to complete the promise
        .run()

    // Completing the promise closes the connection
    import system.dispatcher // Execution context for promise
    closePromise.completeWith(doneFuture.map { _ =>
      Some(ByteString.empty) // Dummy element, will be dropped anyway
    })
  }

  // Simple flow mapping ByteStrings to Strings
  val protocol: Flow[ByteString, String, _] =
    Flow[ByteString].map(_.utf8String)

}
