import geotrellis.raster._
import geotrellis.vector._
import geotrellis.spark._

import geotrellis.spark.io.hadoop._
import org.apache.spark.SparkContext

trait Geoprocessing extends Utils {
  def getPANLCDCount(aoi: GeoJsonData, layerReader: HadoopLayerReader, sc: SparkContext): ResponseData = {
    val areaOfInterest = createAOIFromInput(aoi.geometry)
    val rasterLayer = fetchLocalCroppedPANLCDLayer(areaOfInterest, layerReader)
    ResponseData(rddCellCount(rasterLayer, areaOfInterest))
  }

  private def rddCellCount(
    rasterLayer: TileLayerRDD[SpatialKey],
    areaOfInterest: MultiPolygon
  ): Map[String, Int] = {

  var pixelCounts = Map[String, Int]()

  val hist = rasterLayer.polygonalHistogram(areaOfInterest)
                        .foreach({ case (k: Int, v: Long)  =>
                        //println(k, v)
                        pixelCounts += (k.toString -> v.toInt)
             })

  pixelCounts.toMap

  }
}
