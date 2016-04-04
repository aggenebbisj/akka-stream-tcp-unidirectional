import java.io.File

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ClosedShape, ThrottleMode, IOResult, ActorMaterializer}
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration._

object Demo extends App {

  implicit val system = ActorSystem("Demo")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)
  source.runForeach(i => println(i))

  val factorials = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[IOResult] = factorials
    .map(num => ByteString(s"$num\n"))
    .runWith(FileIO.toFile(new File("factorials.txt")))

  def lineSink(fileName: String): Sink[String, Future[IOResult]] = {
    Flow[String]
      .map(s => ByteString(s + "\n"))
      .toMat(FileIO.toFile(new File(fileName)))(Keep.right)
  }

  factorials.map(_.toString).runWith(lineSink("factorial2.txt"))

//  val done =
//   factorials
//    .zipWith(Source(0 to 100))((num, idx) => s"$idx! = $num")
//    .throttle(1, 1.second, 1, ThrottleMode.shaping)
//    .runForeach(println)

  val source2 = Source(1 to 10)
  val sink = Sink.fold[Int, Int](0)(_ + _)

  sink.runWith(source2)

  import system.dispatcher
  source2.runWith(Sink.head).onComplete(println(_))

  import akka.stream.Fusing

  val flow = Flow[Int].map(_ * 2).filter(_ > 500)
  val fused = Fusing.aggressive(flow)

  val fusedSource =
  Source.fromIterator { () => Iterator from 0 }
    .via(fused)
    .take(1000)

//  fusedSource.runForeach(i => println(i))


  val g = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val in = Source(1 to 10)
    val out: Sink[Any, Future[Done]] = Sink.foreach(println(_))

    val bcast = builder.add(Broadcast[Int](2))
    val merge = builder.add(Merge[Int](2))

    val f1 = Flow[Int].map(_ * 2)
    val f2 = Flow[Int].map(_ + 1)
    val f4 = Flow[Int].map(_ * 10)
    val f3 = Flow[Int].map(i => i)

    in ~> f1 ~> bcast ~> f2 ~> merge ~> f3 ~> out
                bcast ~> f4 ~> merge
    ClosedShape
  })

  g.run()
}
