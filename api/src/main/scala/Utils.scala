import geotrellis.proj4.{CRS, ConusAlbers, LatLng}
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


trait Utils {

    val conf = new SparkConf()
      .setAppName("Analyze PA NLCD Tif")
      .set("spark.serializer", classOf[KryoSerializer].getName)

    implicit val sc = new SparkContext(conf)

  val outputPath: Path = "/tmp/land-cover-data/catalog"
  val reader = HadoopCollectionLayerReader(outputPath)
  val paNLCDLayerID = LayerId("nlcd-pennsylvania", 0)

  def fetchLocalCroppedPANLCDLayer(
    shape: MultiPolygon
  ): TileLayerCollection[SpatialKey] =
    reader
      .query[SpatialKey, Tile, TileLayerMetadata[SpatialKey]](paNLCDLayerID)
      .where(Intersects(shape))
      .result

  def createAOIFromInput(polygon: String): MultiPolygon = parseGeometry(polygon)

  def parseGeometry(geoJson: String): MultiPolygon = {
    geoJson.parseJson.convertTo[Geometry] match {
      case p: Polygon => MultiPolygon(p.reproject(LatLng, ConusAlbers))
      case _ => throw new Exception("Invalid shape")
    }
  }
}
