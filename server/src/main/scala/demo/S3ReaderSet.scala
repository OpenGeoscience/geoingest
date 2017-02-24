package demo

import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.s3._

import org.apache.spark.SparkContext

class S3ReaderSet(bucket: String, prefix: String)(implicit sc: SparkContext) extends ReaderSet {
  val attributeStore = S3AttributeStore(bucket, prefix)
  val metadataReader = new MetadataReader(attributeStore)
  val layerReader = S3LayerReader(attributeStore)
  val layerCReader = S3CollectionLayerReader(attributeStore)
  val singleBandTileReader = new TileReader[SpatialKey, Tile](S3ValueReader(bucket, prefix))
}
