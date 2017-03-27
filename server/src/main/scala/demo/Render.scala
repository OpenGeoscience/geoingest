package demo

import geotrellis.raster._
import geotrellis.raster.render._

object Render {
  val ndviColorBreaks =
    ColorMap.fromStringDouble("0.05:ffffe5aa;0.1:f7fcb9ff;0.2:d9f0a3ff;0.3:addd8eff;0.4:78c679ff;0.5:41ab5dff;0.6:238443ff;0.7:006837ff;1:004529ff").get

  val demColorBreaks =
    ColorMap.fromStringDouble("0:C2FAB3ff;300:D4EA86ff;600:169B28ff;900:639231ff;1200:E9B50Dff;1500:BE4200ff;1800:770701ff;2100:6D2308ff;2400:7B4928ff;2700:A49288ff;3000:CCCCCCff").get

  val nlcdColorBreaks =
    ColorMap(
      Map(
        0  -> RGBA(0, 0, 0, 0),
        1  -> RGB(r = 0, g = 249, b = 0),
        11 -> RGB(r = 71, g = 107, b = 160),
        12 -> RGB(r = 209, g = 221, b = 249),
        21 -> RGB(r = 221, g = 201, b = 201),
        22 -> RGB(r = 216, g = 147, b = 130),
        23 -> RGB(r = 237, g = 0, b = 0),
        24 -> RGB(r = 170, g = 0, b = 0),
        31 -> RGB(r = 178, g = 173, b = 163),
        32 -> RGB(r = 249, g = 249, b = 249),
        41 -> RGB(r = 104, g = 170, b = 99),
        42 -> RGB(r = 28, g = 99, b = 48),
        43 -> RGB(r = 181, g = 201, b = 142),
        51 -> RGB(r = 165, g = 140, b = 48),
        52 -> RGB(r = 204, g = 186, b = 124),
        71 -> RGB(r = 226, g = 226, b = 193),
        72 -> RGB(r = 201, g = 201, b = 119),
        73 -> RGB(r = 153, g = 193, b = 71),
        74 -> RGB(r = 119, g = 173, b = 147),
        81 -> RGB(r = 219, g = 216, b = 61),
        82 -> RGB(r = 170, g = 112, b = 40),
        90 -> RGB(r = 186, g = 216, b = 234),
        91 -> RGB(r = 181, g = 211, b = 229),
        92 -> RGB(r = 181, g = 211, b = 229),
        93 -> RGB(r = 181, g = 211, b = 229),
        94 -> RGB(r = 181, g = 211, b = 229),
        95 -> RGB(r = 112, g = 163, b = 186)
      )
    ).withBoundaryType(Exact).withFallbackColor(0x00000000)

  val slopeColorBreaks = ColorRamps.BlueToOrange.toColorMap(0 to 90 by 10 toArray)

  val aspectColorBreaks = ColorMap(
    Map(
      0  -> RGB(r = 255, g = 51, b = 51),
      45  -> RGB(r = 255, g = 153, b = 51),
      90  -> RGB(r = 255, g = 255, b = 51),
      135  -> RGB(r = 51, g = 255, b = 51),
      180  -> RGB(r = 51, g = 255, b = 255),
      225  -> RGB(r = 51, g = 153, b = 255),
      270  -> RGB(r = 51, g = 51, b = 255),
      315  -> RGB(r = 255, g = 51, b = 255)
    )
  )

  val hillshadeColorBreaks =
    ColorMap.fromStringDouble("0:C2FAB3ff;12:D4EA86ff;24:169B28ff;36:639231ff;48:E9B50Dff;60:BE4200ff;72:770701ff;84:6D2308ff;96:7B4928ff;108:A49288ff;128:CCCCCCff").get

  def colors(layer: String): ColorMap = layer match {
    case "weld" => ndviColorBreaks
    case "landcover" => nlcdColorBreaks
    case "elevation" => demColorBreaks
    case "slope" => slopeColorBreaks
    case "aspect" => aspectColorBreaks
    case "hillshade" => hillshadeColorBreaks
  }

  def render(tile: Tile, layer: String): Png = {
    val colorBreak = colors(layer)
    tile.renderPng(colorBreak)

  }

}
