package demo

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ContentType, HttpEntity, HttpResponse, MediaTypes}
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import ch.megard.akka.http.cors.CorsDirectives._
import com.typesafe.config._
import geotrellis.proj4._
import geotrellis.raster._
import geotrellis.raster.interpolation._
import geotrellis.raster.render._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.tiling._
import geotrellis.raster.mapalgebra.focal._
import geotrellis.vector._
import geotrellis.vector.io._
import geotrellis.vector.io.json._
import org.apache.spark.SparkContext
import spray.json._

import java.lang.management.ManagementFactory
import java.time.format.DateTimeFormatter
import java.time.{ZonedDateTime, ZoneOffset}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

class Router(readerSet: ReaderSet, sc: SparkContext) extends Directives with AkkaSystem.LoggerExecutor {
  import scala.concurrent.ExecutionContext.Implicits.global

  val metadataReader = readerSet.metadataReader
  val attributeStore = readerSet.attributeStore

  def pngAsHttpResponse(png: Png): HttpResponse =
    HttpResponse(entity = HttpEntity(ContentType(MediaTypes.`image/png`), png.bytes))

  def routes =
    path("ping") { complete { "pong\n" } } ~
      pathPrefix("tiles") { tilesRoute } ~
      pathPrefix("focalmean") { focalMeanRoute } ~
      pathPrefix("focalsd") { focalSdRoute }


  /** Find the breaks for one layer */
  def tilesRoute =
  pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layer, zoom, x, y) =>
      complete {
        Future {
          val tileOpt =
            readerSet.readSinglebandTile(layer, zoom, x, y)

          tileOpt.map { tile =>
            val png = Render.ndvi(tile)
            pngAsHttpResponse(png)
          }
        }
      }
  }
  def focalMeanRoute =
    pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layer, zoom, x, y) =>
      complete {
        Future {
          val tileOpt =
            readerSet.readSinglebandTile(layer, zoom, x, y)

          tileOpt.map { tile =>
            val focal = tile.focalMean(Square(3))
            val png = Render.ndvi(focal)
            pngAsHttpResponse(png)
          }
        }
      }
    }
  def focalSdRoute =
    pathPrefix(Segment / IntNumber / IntNumber / IntNumber) { (layer, zoom, x, y) =>
      complete {
        Future {
          val tileOpt =
            readerSet.readSinglebandTile(layer, zoom, x, y)

          tileOpt.map { tile =>
            val focal = tile.focalStandardDeviation(Square(3))
            val png = Render.ndvi(focal)
            pngAsHttpResponse(png)
          }
        }
      }
    }
}
