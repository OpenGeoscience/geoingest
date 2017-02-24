package demo

import geotrellis.raster._
import geotrellis.raster.render._

object Render {
  val ndviColorBreaks =
    ColorMap.fromStringDouble("0.05:ffffe5aa;0.1:f7fcb9ff;0.2:d9f0a3ff;0.3:addd8eff;0.4:78c679ff;0.5:41ab5dff;0.6:238443ff;0.7:006837ff;1:004529ff").get

  def ndvi(tile: Tile): Png =
    tile.renderPng(ndviColorBreaks)

}
