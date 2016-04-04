import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Flow, Source}

import scala.concurrent.{Future, Promise}

object Maybe extends App {

  implicit val system = ActorSystem("Demo")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val f1: Flow[Int, String, NotUsed] =
    Flow[Int]
//      .map { elem => println(elem); elem }
      .map(_.toString)

  val source: Source[Int, Promise[Option[Int]]] = Source.maybe[Int]
  val sink: Sink[String, Future[String]] = Sink.head[String]

//  val (promise, fut) = source.via(f1).toMat(sink)(Keep.both).run()

  val (promise, fut) = Source.maybe[Int].toMat(Sink.foreach(println))(Keep.both).run()

  promise.completeWith(fut.map { x => println("completed biatch"); Some(1) })
//  fut.map { x => println("Fut: " + x); x}

//  promise.success(Some(5))

}
