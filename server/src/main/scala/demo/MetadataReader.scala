package demo


import geotrellis.spark._
import geotrellis.spark.io._

import spray.json._
import spray.json.DefaultJsonProtocol._

/** Aside from reading our metadata we also do some processing to figure out how many time stamps we have */
class MetadataReader(attributeStore: AttributeStore) {
  def read[K: SpatialComponent: JsonFormat](layer: LayerId) = {
    val md = attributeStore.readMetadata[TileLayerMetadata[K]](layer)
    LayerMetadata(md)
  }
}
