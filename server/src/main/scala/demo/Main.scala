package demo

import geotrellis.spark.io.accumulo._
import geotrellis.spark.io.cassandra._
import geotrellis.spark.io.hbase._

import org.apache.spark._
import org.apache.accumulo.core.client.security.tokens._
import akka.actor._
import akka.io.IO

import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object AkkaSystem {
  implicit val system = ActorSystem("iaas-system")
  implicit val materializer = ActorMaterializer()

  trait LoggerExecutor {
    protected implicit val log = Logging(system, "app")
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    import AkkaSystem._

    val conf: SparkConf =
      new SparkConf()
        .setIfMissing("spark.master", "local[*]")
        .setAppName("Demo Server")
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .set("spark.kryo.registrator", "geotrellis.spark.io.kryo.KryoRegistrator")

    implicit val sc = new SparkContext(conf)

    val bucket = args(1)
    val prefix = args(2)
    val readerSet = new S3ReaderSet(bucket, prefix)

    val router = new Router(readerSet, sc)
    Http().bindAndHandle(router.routes, "0.0.0.0", 8899)
  }
}
