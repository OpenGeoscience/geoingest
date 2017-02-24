package demo

import geotrellis.proj4._
import geotrellis.raster._
import geotrellis.raster.resample._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.tiling._

import java.time.ZonedDateTime

trait ReaderSet {
  val layoutScheme = ZoomedLayoutScheme(WebMercator, 256)
  def attributeStore: AttributeStore
  def metadataReader: MetadataReader
  def layerReader: FilteringLayerReader[LayerId]
  def layerCReader: CollectionLayerReader[LayerId]
  def singleBandTileReader: TileReader[SpatialKey, Tile]

  /** Do "overzooming", where we resample lower zoom level tiles to serve out higher zoom level tiles. */
  def readSinglebandTile(layer: String, zoom: Int, x: Int, y: Int): Option[Tile] =
    try {
      val z = 13

      if(zoom > z) {
        val layerId = LayerId(layer, z)

        val meta = metadataReader.read(layerId)
        val rmd = meta.rasterMetaData

        val requestZoomMapTransform = layoutScheme.levelForZoom(zoom).layout.mapTransform
        val requestExtent = requestZoomMapTransform(x, y)
        val centerPoint = requestZoomMapTransform(x, y).center
        val SpatialKey(nx, ny) = rmd.mapTransform(centerPoint)
        val sourceExtent = rmd.mapTransform(nx, ny)


        val largerTile =
          singleBandTileReader.read(layerId, SpatialKey(nx, ny))

        Some(largerTile.resample(sourceExtent, RasterExtent(requestExtent, 256, 256), Bilinear))
      } else {
        Some(singleBandTileReader.read(LayerId(layer, zoom), SpatialKey(x, y)))
      }
    } catch {
      case e: ValueNotFoundError =>
        None
    }
}
