import geotrellis.raster._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.file._
import geotrellis.vector._
import geotrellis.vector.io._
import geotrellis.spark.io.hadoop._
import spray.json._
import spray.json.DefaultJsonProtocol._
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.serializer.KryoSerializer

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import geotrellis.spark.io.hadoop._

trait Utils {

  val paNLCDLayerID = LayerId("glad", 0)

  def fetchLocalCroppedPANLCDLayer(
    shape: MultiPolygon,
    layerReader: HadoopLayerReader
  ): TileLayerRDD[SpatialKey] =
    layerReader
      .query[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](paNLCDLayerID)
      .where(Intersects(shape))
      .result

  def createAOIFromInput(polygon: String): MultiPolygon = parseGeometry(polygon)

  def parseGeometry(geoJson: String): MultiPolygon = {
    geoJson.parseJson.convertTo[Geometry] match {
      case p: Polygon => MultiPolygon(p)
      case _ => throw new Exception("Invalid shape")
    }
  }
}
